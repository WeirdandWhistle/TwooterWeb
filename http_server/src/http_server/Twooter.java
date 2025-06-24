package http_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
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

	public long lastSave = 0;
	public final long timeBewteenSaves = 10 * 1000; // time in mills

	public ConcurrentHashMap<String, Twoot> twootDB1Map = new ConcurrentHashMap<String, Twoot>();

	public Twooter() {
		try {
			new File(sessionDBPath).createNewFile();
			new File(personDBPath).createNewFile();
			new File(twootDB1Path).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.loadTwootCache();
		lastSave = System.currentTimeMillis();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if (System.currentTimeMillis() - this.lastSave >= this.timeBewteenSaves) {
			saveTwootCache();
		} else {
			System.out.println(((System.currentTimeMillis() - this.lastSave) / 1000)
					+ " seconds affter last save");
		}

		try {

			// System.out.println("twooter uri:" +
			// exchange.getRequestURI().toString());

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

			if (url.split("/")[0].equals("twooter") && url.split("/").length == 1) {

				// System.out.println(exchange.getRequestHeaders().getFirst("Cookie"));

				new ServeFile("twooter/twit.html").handle(exchange);
				return;
			} else if (url.equals("twooter/")) {
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

		try {

			switch (exchange.getRequestURI().getPath().split("/")[3]) {

				case "login" :
					LOGIN(exchange);
					return;
				case "twoot" :
					TWOOT(exchange);
					return;
				case "get-twoot" :
					getTWOOT(exchange);
					return;
				case "line-twoot" :
					lineTWOOT(exchange);
					return;
				case "twoot-length" :
					getTWOOTLength(exchange);
					return;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void LOGIN(HttpExchange e) {

		System.out.println("started login");

		if (Util.enforceMethod("PUT", e)) {
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

			String userID = Util.hex(Util.SHA256(name));

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

			synchronized (personDBLock) {

				BufferedReader read = new BufferedReader(new FileReader(pdb));
				String line = null;
				boolean found = false;
				while ((line = read.readLine()) != null) {
					if (line.substring(10, 53).equals(userID)) {
						System.out.println("person auth needed!");
						found = true;
						break;
					}
				}
				read.close();
				FileWriter write = new FileWriter(pdb);

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
		if (Util.enforceMethod("POST", e)) {
			return;
		}

		String session = enforceSession(e);

		if (session == null) {
			return;
		}

		JsonObject json = JsonParser.parseString(Util.parseBody(e.getRequestBody()))
				.getAsJsonObject();

		String name = this.getNameFromSession(session);
		String userID = this.hexSHA256(name);

		String message = json.get("message").getAsString();
		String title = json.get("title").getAsString();

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

		twootDB1Map.put(twoot.ID, twoot);

		try {
			byte[] repo = ("{ \"ID\" : \"" + ID + "\"}").getBytes();
			e.sendResponseHeaders(200, repo.length);
			e.getResponseBody().write(repo);
			e.getResponseBody().close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("twoot finished!");
	}
	public void getTWOOT(HttpExchange e) {

		if (Util.enforceMethod("GET", e)) {
			return;
		}

		String session = enforceSession(e);

		if (session == null) {
			return;
		}

		String ID = e.getRequestHeaders().getFirst("ID");

		Twoot json = twootDB1Map.get(ID);

		if (json == null) {
			System.out.println("no working");
			Util.HttpError("couldnt find twoot by ID", 400, e);
			return;
		}

		String repo = new Gson().toJson(json);

		try {
			e.getResponseHeaders().add("content-type", "text/json");
			e.sendResponseHeaders(200, repo.getBytes().length);
			e.getResponseBody().write(repo.getBytes());
			e.getResponseBody().close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	/**
	 * javadoc for lineTWOOt just a good way to get sorta random twoots <br>
	 * in url query index
	 * 
	 * <br>
	 * json out 'json of the twoot at "line" '
	 * 
	 * <br>
	 * 
	 * @apiNote indeing starts at 0
	 * @returns json using a error tag if line doesn't exsit
	 */
	public void lineTWOOT(HttpExchange e) {
		String session = enforceSession(e);

		if (session == null) {
			return;
		}

		HashMap<String, String> query = Util.parseQuery(e.getRequestURI().getQuery());

		int lineNum = Integer.valueOf(query.get("index"));
		// System.out.println("lineNum:" + lineNum);
		// boolean ok = true;

		Set<Map.Entry<String, Twoot>> set = twootDB1Map.entrySet();
		ArrayList<Map.Entry<String, Twoot>> arr = new ArrayList<>(set);

		if (set == null) {
			System.out.println("um set error");
		}

		if (set.size() < lineNum) {
			Util.HttpError("line does not exsit", 500, e);
			return;
		}

		byte[] repo = new Gson().toJson(arr.get(lineNum).getValue()).getBytes();

		try {
			e.getResponseHeaders().add("content-type", "text/json");
			e.sendResponseHeaders(200, repo.length);
			e.getResponseBody().write(repo);
			e.getResponseBody().close();
		} catch (IOException e2) {
			Util.HttpError("IOException e2", 500, e);
			e2.printStackTrace();
		}

	}
	public void getTWOOTLength(HttpExchange e) {
		// System.out.println("startted getTWOOTLength");
		String session = enforceSession(e);

		if (session == null) {
			Util.HttpError("no session", 401, e);
			return;
		}

		if (Util.enforceMethod("GET", e)) {
			return;
		}

		// System.out.println("made it passed starting");

		Set<Map.Entry<String, Twoot>> set = twootDB1Map.entrySet();

		if (set == null) {
			Util.HttpError("set is null", 500, e);
			return;
		}

		int lines = set.size();

		JsonObject json = new JsonObject();
		json.addProperty("length", lines);
		byte[] repo = new Gson().toJson(json).getBytes();

		try {
			e.getResponseHeaders().add("content-type", "text/json");
			e.sendResponseHeaders(200, repo.length);
			e.getResponseBody().write(repo);
			e.getResponseBody().close();
		} catch (IOException e1) {
			Util.HttpError("IOException", 500, e);
			e1.printStackTrace();
		}

	}
	public void likeTwoot(HttpExchange e) {
		if (Util.enforceMethod("PUT", e)) {
			return;
		}

		String session = enforceSession(e);
		if (session == null) {
			return;
		}
		String ID = JsonParser.parseString(Util.parseBody(e.getRequestBody())).getAsJsonObject()
				.get("ID").getAsString();

		synchronized (twootDB1Lock) {
			String twoots = Util.readFile(new File(twootDB1Path));
			Scanner scan = Util.getScan(twoots);
			int importLine = -1;
			int lines = -1;
			boolean rightLine = false;
			String same = "";
			try (BufferedWriter write = new BufferedWriter(new FileWriter(twootDB1Path))) {
				String line = null;
				while ((line = Util.nextScan(scan)) != null) {
					lines++;
					String subID = line.substring(7, 50);
					System.out.println("subID" + subID);
					if (subID.equals(ID)) {
						JsonObject json = JsonParser.parseString(line).getAsJsonObject();

						if (json.get("ID").getAsString().equals(ID)) {
							importLine = lines;
							rightLine = true;;
						}

					} else if (!rightLine) {
						same += line + "\n";
					}
					rightLine = false;

				}

			} catch (IOException e2) {
				Util.HttpError("IOException", 500, e);
				e2.printStackTrace();
				return;
			}
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
	public void loadTwootCache() {
		System.out.println("loading twoot cache...");
		synchronized (twootDB1Lock) {
			try (BufferedReader read = new BufferedReader(new FileReader(twootDB1Path))) {
				String line = null;
				twootDB1Map.clear();
				while ((line = read.readLine()) != null) {
					Twoot twoot = new Gson().fromJson(line, Twoot.class);
					twootDB1Map.put(line.substring(7, 50), twoot);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("finshed loading twoot cache");
	}
	public synchronized void saveTwootCache() {

		if (System.currentTimeMillis() - this.lastSave >= this.timeBewteenSaves) {
			System.out.println("savingTwoot cache");
			this.lastSave = System.currentTimeMillis();
			synchronized (twootDB1Lock) {
				this.lastSave = System.currentTimeMillis();

				try (BufferedWriter write = new BufferedWriter(new FileWriter(twootDB1Path))) {
					Set<Map.Entry<String, Twoot>> set = twootDB1Map.entrySet();
					ArrayList<Map.Entry<String, Twoot>> arr = new ArrayList<>(set);

					for (int i = 0; i < arr.size(); i++) {
						write.write(new GsonBuilder().serializeNulls().create()
								.toJson(arr.get(i).getValue(), Twoot.class));
						write.newLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.lastSave = System.currentTimeMillis();
			System.out.println("saved twoot cache");
		}
	}

}
