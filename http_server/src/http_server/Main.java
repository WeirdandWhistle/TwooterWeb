package http_server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Scanner;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
	public final static int port = 8000;
	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		API api = new API();

		InetSocketAddress address = new InetSocketAddress("localhost", port);

		try {
			HttpServer server = HttpServer.create(address, 0);
			// System.out.

			server.createContext("/x", (handle) -> {
				String repo = "defualt";
				System.out.println("someone visited '/x'!");
				// String repo = in.nextLine();

				handle.sendResponseHeaders(404, repo.getBytes().length);
				handle.getResponseBody().write(repo.getBytes());
				handle.getResponseBody().close();
			});
			server.createContext("/api/", api);
			server.createContext("/slack/", new Slack());
			server.createContext("/twooter", new Twooter());
			server.createContext("/", new DefaultHandler());

			server.start();
			System.out.println("started server at port: " + server.getAddress().getPort());
			System.out.println("accces at: localhost:" + server.getAddress().getPort());
		} catch (IOException e) {
			System.out.println("server somthing");
			e.printStackTrace();
		}

		String json = "{\"name\":\"dude cool\"}";

		System.out
				.println(JsonParser.parseString(json).getAsJsonObject().get("name").getAsString());

	}

	public static class ServeFile implements HttpHandler {
		private final String filename;

		public ServeFile(String filename) {
			this.filename = filename;
		}

		@Override
		public void handle(HttpExchange exchange) {
			File file = new File(filename);
			String response = "Error 204 No Content";
			if (!file.exists()) {
				System.out.println("file doesn't exsit!");
				try {
					exchange.sendResponseHeaders(204, response.getBytes().length);

					exchange.getResponseBody().write(response.getBytes());
					exchange.getResponseBody().close();
				} catch (IOException e) {
					System.out.println("uuuuuuuummm! thats not my fault!");
					e.printStackTrace();
				}
			} else {
				try {
					byte[] filebytes = Files.readAllBytes(file.toPath());
					exchange.sendResponseHeaders(200, 0);
					// System.out.println("file might work");
					exchange.getResponseBody().write(filebytes);
					exchange.getResponseBody().close();
					// System.out.println("sent file: " + filename);
				} catch (IOException e) {
					System.out.println("serving files");
					e.printStackTrace();
				}
			}
		}
	}
	public static class DefaultHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("defalut:" + exchange.getRequestURI().toString());

			if (exchange.getRequestURI().toString().equals("/")) {
				System.out.println("default");
				new ServeFile("index.html").handle(exchange);

				return;
			}

			if (exchange.getRequestURI().toString().split("/").length > 2
					&& exchange.getRequestURI().toString().split("/")[2].equals("DB")) {
				String repo = "ERROR 401 not authorized\nfrom server";
				exchange.sendResponseHeaders(401, repo.getBytes().length);
				exchange.getResponseBody().write(repo.getBytes());
				exchange.getResponseBody().close();
				return;
			}

			File file = new File(exchange.getRequestURI().toString().replaceFirst("/", ""));
			if (file.exists()) {
				// exchange.getResponseHeaders().add("Content-type",
				// Util.getMIMIType(file.getName().split(".")[1]));
				exchange.sendResponseHeaders(200, 0);
				byte[] fileBytes = Files.readAllBytes(file.toPath());
				exchange.getResponseBody().write(fileBytes);
				exchange.getResponseBody().close();
			} else {
				String repo = "ERROR 404 not found\nfrom server";
				exchange.sendResponseHeaders(404, repo.getBytes().length);
				exchange.getResponseBody().write(repo.getBytes());
				exchange.getResponseBody().close();
			}

		}

	}
	static class PostHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("somthin!");
			String response = "ERROR 400 Bad Request!";

			if ("POST".equals(exchange.getRequestMethod())) {
				InputStream requestBody = exchange.getRequestBody();
				StringBuilder textBuilder = new StringBuilder();
				int byteData;
				while ((byteData = requestBody.read()) != -1) {
					textBuilder.append((char) byteData);
				}
				requestBody.close();

				String requestData = textBuilder.toString();
				response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
						+ "<message> sussce valided and fulfilled </message>";
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);

				OutputStream outputStream = exchange.getResponseBody();
				outputStream.write(response.getBytes());
				outputStream.close();

				System.out.println(requestData + " from url: " + exchange.getRequestURI());
				System.out.println("headers: " + exchange.getRequestHeaders().values());

				// System.out.println("body: " + exchange.getRequestBody().);
			} else {
				exchange.sendResponseHeaders(400, response.getBytes().length);
				exchange.getResponseBody().write(response.getBytes());
				exchange.getRequestBody().close();
			}
		}
	}

}
