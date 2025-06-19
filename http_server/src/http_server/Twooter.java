package http_server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import http_server.Main.DefaultHandler;
import http_server.Main.ServeFile;
import lib.JSON;
import lib.Util;
import objects.Twoot;

public class Twooter implements HttpHandler {

	public final String acceptedChars = "qwertyuiopasdfghjklzxcvbnm";
	public final static String sessionDBPath = "twooter\\DB\\sessionDB.txt";
	public final static String personDBPath = "twooter\\DB\\personDB.txt";
	public final static String twootDB1Path = "twooter\\DB\\twootsDB1.txt";

	public Object sessionDBLock = new Object();
	public Object personDBLock = new Object();
	public Object twootDB1Lock = new Object();

	public Twooter() {
		try {
			new File(sessionDBPath).createNewFile();
			new File(personDBPath).createNewFile();
			new File(twootDB1Path).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		try {

			// System.out.println("uri:" + exchange.getRequestURI().toString());

			String url = exchange.getRequestURI().toString().replaceFirst("/", "");

			// System.out.println(url);
			// System.out.println(url.equals("slack/"));

			String session = null;

			session = Util.getCookie(exchange, "session");

			if (url.equals("twooter/login/")) {

				// exchange.getResponseHeaders().add("Set-Cookie",
				// "session=login; path=/");

				new ServeFile("twooter/login/login.html").handle(exchange);
				return;
			}

			else if (!inSessionDB(session)) {
				if (url.split("/").length >= 3) {
					if (url.split("/")[1].equals("login")) {
						new ServeFile(url).handle(exchange);
						return;
					}
				}
				System.out.println("no session");
				Util.redirect(exchange, "/twooter/login/");
				return;
			}

			if (url.equals("twooter/")) {

				System.out.println(exchange.getRequestHeaders().getFirst("Cookie"));

				new ServeFile("twooter/twit.html").handle(exchange);
				return;
			}

			File file = new File(url);

			new DefaultHandler().handle(exchange);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void API(HttpExchange exchange) {

		System.out.println("twooter api request:" + exchange.getRequestURI().toString());

		switch (exchange.getRequestURI().toString().split("/")[3]) {

			case "login" :
				LOGIN(exchange);
				return;
			case "twoot" :
				try {
					TWOOT(exchange);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;

		}
	}

	public synchronized void LOGIN(HttpExchange e) {

		System.out.println("started login");

		if (!e.getRequestMethod().equals("PUT")) {
			System.out.println("no a 'get' request'");
			return;
		}

		try {
			File pdb = new File(personDBPath);
			File sdb = new File(sessionDBPath);

			String parsedJson = Util.parseBody(e.getRequestBody());
			// System.out.println("parser:" + parsedJson);
			String name = JsonParser.parseString(parsedJson).getAsJsonObject().get("username")
					.getAsString();

			name = name.toLowerCase();

			String userID = hexSHA256(name);

			for (int i = 0; i < name.length(); i++) {
				if (!Util.contains(acceptedChars, name.charAt(i))) {
					String repo = JSON.make("'error':'username'");
					// System.out.println(
					// "bad username:" + name + ", error on:'" + name.charAt(i)
					// + "'");
					e.sendResponseHeaders(400, repo.getBytes().length);
					e.getResponseBody().write(repo.getBytes());
					e.getResponseBody().close();
					return;
				}
			}

			String fileString = getPersonDB();
			Scanner scan = Util.getScan(fileString);
			BufferedWriter write;
			synchronized (personDBLock) {
				write = new BufferedWriter(new FileWriter(pdb));

				boolean foundPerson = false;
				String line;

				while ((line = Util.nextScan(scan)) != null) {
					String[] data = line.split(" ");
					if (data[0].equals(userID)) {
						foundPerson = true;
						System.out.println("have aother person");
						break;
					}
					if (data[1].equals(name)) {
						System.out.println("name found!");
					}
				}

				if (!foundPerson) {
					write.write(userID + " " + name);
					write.newLine();
					System.out.println("no name found!");
				}

				write.write(fileString);
				scan.close();
				write.close();
			}

			fileString = getSessionDB();
			scan = Util.getScan(fileString);
			BigInteger session = null;

			String sessionString = sessionDBName(name);
			System.out.println("sessionString:" + sessionString);
			synchronized (sessionDBLock) {
				write = new BufferedWriter(new FileWriter(sdb));

				if (sessionString == null) {
					session = Util.random(100);
					write.write(userID + " " + session.toString() + " " + name);
					write.newLine();
					System.out.println("made session token for:" + name);
				} else {
					session = new BigInteger(sessionString);
					System.out.println("found session token for:" + name);
				}

				write.write(fileString);
				write.close();
				scan.close();
			}

			String repo = "{\"userID\":\"" + userID + "\",\"name\":\"" + name
					+ "\",\"location\":\"\"}";

			System.out.println("session:" + session);
			e.getResponseHeaders().add("set-cookie",
					"session=" + session.toString() + "; SameSite=Strict; path=/");
			e.getResponseHeaders().add("content-type", "text/json");
			e.sendResponseHeaders(200, repo.getBytes().length);

			e.getResponseBody().write(repo.getBytes());

			e.getResponseBody().close();

			System.out.println("done!");
		} catch (Exception e1) {
			System.out.println("ERROR shot crab god dammit no!!!!!!!!! please help!");
			e1.printStackTrace();
		}

	}
	public void TWOOT(HttpExchange e) {
		System.out.println("started twoot");
		if (!e.getRequestMethod().equals("POST")) {
			JsonObject json = new JsonObject();
			json.addProperty("error", "wrong method");

			try {
				e.getResponseHeaders().add("content-type", "text/json");
				e.sendResponseHeaders(401, json.getAsString().getBytes().length);
				e.getResponseBody().write(json.getAsString().getBytes());
				e.getResponseBody().close();
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		String session = enforceSession(e);

		if (session == null) {
			return;
		}

		JsonObject json = JsonParser.parseString(Util.parseBody(e.getRequestBody()))
				.getAsJsonObject();

		String name = this.getNameFromSession(session);
		String userID = this.hexSHA256(name);

		String message = json.get("message").toString();
		String title = json.get("title").toString();

		long timestamp = System.currentTimeMillis();
		String ID = Util.Base64(Util.SHA256((hexSHA256(message) + timestamp + userID)));

		// JsonArray like = new JsonArray();
		// JsonArray dislike = new JsonArray();
		// JsonArray comments = new JsonArray();

		// encode divs to a escaped charecter '\<div>'
		message = message.replace("<div>", "__START_DIV__").replace("</div>", "__CLOSE_DIV__");
		// encode all '<' '>' to the relitive html entity to prvent xss
		message = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&apos;");

		Twoot twoot = new Twoot();
		twoot.ID = ID;
		twoot.timestamp = timestamp;
		twoot.name = name;
		twoot.userID = userID;
		twoot.title = title;
		twoot.message = message;
		twoot.likeCount = 0;
		twoot.dislikeCount = 0;
		// twoot.like = null;
		// twoot.dislike = null;
		// twoot.comments = null;

		String JSON = new GsonBuilder().serializeNulls().create().toJson(twoot);

		synchronized (twootDB1Lock) {

			try (FileWriter write = new FileWriter(twootDB1Path, true)) {
				write.write(JSON + "\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		try {
			e.sendResponseHeaders(200, 0);
			e.getResponseBody().close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("twoot finished!");
	}
	public void getTWOOT(HttpExchange e) {

		String session = enforceSession(e);

		if (session == null) {
			return;
		}

		String ID = JsonParser.parseString(Util.parseBody(e.getRequestBody())).getAsJsonObject()
				.get("ID").getAsString();

		JsonObject json = getTwootByID(ID);

		if (json == null) {
			Util.HttpError("couldnt find twoot by ID", 400, e);
		}

	}
	public String getSessionDB() {
		synchronized (sessionDBLock) {
			// System.out.println("sessionDB:" + Util.readFile(new
			// File(sessionDBPath)));
			return Util.readFile(new File(sessionDBPath));
		}
	}
	public String getPersonDB() {
		synchronized (personDBLock) {
			return Util.readFile(new File(personDBPath));
		}
	}
	public String getTweetDB1() {
		synchronized (twootDB1Lock) {
			return Util.readFile(new File(twootDB1Path));
		}
	}

	public String sessionDBName(String name) {

		String sessionString = getSessionDB();
		Scanner scan = Util.getScan(sessionString);
		String userID = hexSHA256(name);
		System.out.println("sdbn userid:" + userID);
		System.out.println("sdbn sessionString:" + sessionString);

		String line = null;

		while ((line = Util.nextScan(scan)) != null) {
			String[] data = line.split(" ");
			if (data[0].equals(userID)) {
				return data[1];
			}
		}
		return null;
	}
	public boolean inSessionDB(String id) {

		String sessionString = getSessionDB();
		Scanner scan = Util.getScan(sessionString);

		String line = null;

		while ((line = Util.nextScan(scan)) != null) {
			String[] data = line.split(" ");
			if (data[1].equals(id)) {
				return true;
			}
		}
		return false;
	}
	public String getNameFromSession(String session) {
		String sessionString = getSessionDB();
		Scanner scan = Util.getScan(sessionString);

		String line = null;

		while ((line = Util.nextScan(scan)) != null) {
			String[] data = line.split(" ");
			if (data[1].equals(session)) {
				return data[2];
			}
		}
		return null;
	}
	public String hexSHA256(String name) {
		try {
			MessageDigest hash = MessageDigest.getInstance("SHA-256");
			return Util.hex(hash.digest(name.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
	public String enforceSession(HttpExchange e) {
		String session = Util.getCookie(e, "session");
		if (session == null) {
			JsonObject json = new JsonObject();
			json.addProperty("error", "no session");

			try {
				e.getResponseHeaders().add("content-type", "text/json");
				e.sendResponseHeaders(401, json.getAsString().getBytes().length);
				e.getResponseBody().write(json.getAsString().getBytes());
				e.getResponseBody().close();
				return null;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (!this.inSessionDB(session)) {
			System.out.println("wrong session:'" + session);
			JsonObject json = new JsonObject();
			json.addProperty("error", "wrong session");
			String repo = json.getAsString();
			try {
				e.getResponseHeaders().add("content-type", "text/json");
				e.sendResponseHeaders(401, repo.getBytes().length);
				e.getResponseBody().write(repo.getBytes());
				e.getResponseBody().close();
				return null;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return session;
	}

	public JsonObject getTwootByID(String ID) {

		String db = getSessionDB();

		String line = "";
		Scanner scan = Util.getScan(db);

		JsonObject json = null;
		while ((line = Util.nextScan(scan)) != null) {
			json = JsonParser.parseString(line).getAsJsonObject();
			if (json.get("ID").getAsString().equals(ID)) {
				return json;
			}
		}

		return null;
	}

}
