package lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;

public abstract class Util implements FileRunnables {

	public static String parseBody(InputStream is) {

		StringBuilder textBuilder = new StringBuilder();
		int byteData;
		try {
			while ((byteData = is.read()) != -1) {
				textBuilder.append((char) byteData);
			}
			String requestData = textBuilder.toString();
			return requestData;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "well that sucks man. good luck. sry there wasn't a error.";

	}

	public static HttpResponse<?> request(HttpRequest request) {

		HttpClient client = HttpClient.newBuilder().build();

		try {
			HttpResponse<?> repo = client.send(request, BodyHandlers.ofString());

			return repo;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}
	public static String getMIMIType(String extension) {
		System.out.println("extenstion" + extension);
		switch (extension) {
			case "js" :
				return "text/javascript";
			case "css" :
				System.out.println("css file header");
				return "text/css";
			case "html" :
				return "text/html";
			default :
				return null;

		}
	}

	public static String getCookie(HttpExchange exchange, String key) {
		// System.out.println("util cookie:" +
		// exchange.getRequestHeaders().getFirst("Cookie"));
		if (exchange.getRequestHeaders().getFirst("Cookie") == null) {
			// System.out.println("util cookie no values in cookie");
			return null;
		}

		List<HttpCookie> cookies = HttpCookie
				.parse(exchange.getRequestHeaders().getFirst("Cookie"));

		if (cookies == null) {
			// System.out.println("util cookie null");
			return null;
		}

		for (int i = 0; i < cookies.size(); i++) {
			// System.out.println("util cookie name:" +
			// cookies.get(i).getName());
			if (cookies.get(i).getName().equals(key)) {
				return cookies.get(i).getValue();
			}
		}

		return null;

	}
	public static void redirect(HttpExchange exchange, String url, int code) {
		try {
			exchange.getResponseHeaders().add("location", url);
			exchange.sendResponseHeaders(code, 0);
			exchange.getResponseBody().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void redirect(HttpExchange exchange, String url) {
		redirect(exchange, url, 307);
	}
	public static BigInteger random(int digits) {
		String num = "";

		for (int i = 0; i < digits; i++) {
			num += Math.round(Math.random() * 9);
		}

		return new BigInteger(num);
	}

	public static String hex(byte[] arr) {

		StringBuilder sb = new StringBuilder();
		for (byte b : arr) {
			sb.append(String.format("%02X", b));
		}

		return sb.toString();
	}
	public static String Base64(byte[] arr) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(arr);
	}

	public static String readFile(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			String fileString = "";
			while ((line = reader.readLine()) != null) {
				fileString += line + "\n";
				// System.out.println("readFile line:" + line);
			}
			// System.out.println("readFile fileString:" + fileString);
			reader.close();
			return fileString;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Scanner getScan(String string) {
		return new Scanner(string).useDelimiter("\n");
	}

	public static void forLine(File file, FileRunnables run) {
		Scanner read = Util.getScan(readFile(file));

		boolean canRead = read.hasNextLine();
		String line = null;

		while ((canRead = read.hasNextLine()) != false) {
			line = read.nextLine();
			run.fileLoopRun(line);
		}
	}

	public static String nextScan(Scanner scan) {

		if (!scan.hasNextLine()) {
			return null;
		}

		return scan.nextLine();

	}

	public static boolean contains(String a, char b) {
		for (int i = 0; i < a.length(); i++) {
			if (a.charAt(i) == b) {
				return true;
			}
		}
		return false;
	}
	public static byte[] SHA256(String name) {
		try {
			MessageDigest hash = MessageDigest.getInstance("SHA-256");
			return hash.digest(name.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
	public static void HttpError(String error, int errorCode, HttpExchange e) {

		try {
			String repo = "{ \"error\" : \"" + error + " \" }";
			e.sendResponseHeaders(errorCode, repo.getBytes().length);
			e.getResponseBody().write(repo.getBytes());
			e.getResponseBody().close();
			return;
		} catch (IOException e1) {
			String repoString = "{\"error\":\"IOException\", \"info\":" + e1.getMessage() + "}";
			try {
				e.sendResponseHeaders(500, repoString.getBytes().length);
				e.getResponseBody().write(repoString.getBytes());
				e.getResponseBody().close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			e1.printStackTrace();
		}
	}

	public static boolean enforceMethod(String method, HttpExchange e) {
		if (e.getRequestMethod().equals(method)) {
			return false;
		}
		HttpError("wrong http method", 405, e);
		return true;
	}
	public static HashMap<String, String> parseQuery(String query) {

		String[] allQuery = query.split("&");
		HashMap<String, String> map = new HashMap<>();

		for (String pair : allQuery) {
			String[] pear = pair.split("=");
			map.put(pear[0], pear[1]);
		}

		return map;
	}

}
