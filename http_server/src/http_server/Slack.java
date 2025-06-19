package http_server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import http_server.Main.ServeFile;

public class Slack implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String url = exchange.getRequestURI().toString().replaceFirst("/", "");

		// System.out.println(url);
		// System.out.println(url.equals("slack/"));
		if (url.equals("slack/")) {
			// System.out.println("deault slack");
			new ServeFile("slack/message-page.html").handle(exchange);
			return;
		}

		File file = new File(url);

		if (file.exists()) {
			// exchange.getResponseHeaders().add("Content-type",
			// Util.getMIMIType(file.getName().split(".")[1]));
			exchange.sendResponseHeaders(200, 0);
			byte[] fileBytes = Files.readAllBytes(file.toPath());
			exchange.getResponseBody().write(fileBytes);
			exchange.getResponseBody().close();
		} else {
			String repo = "ERROR 404 not found";
			exchange.sendResponseHeaders(404, repo.getBytes().length);
			exchange.getResponseBody().write(repo.getBytes());
			exchange.getResponseBody().close();
		}

	}

}
