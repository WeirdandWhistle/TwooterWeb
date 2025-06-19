package http_server;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lib.Util;

public class API implements HttpHandler {

	private SlackBot slackBot = new SlackBot();
	private Twooter twooter = new Twooter();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// System.out.println("ok good");

		String[] url = exchange.getRequestURI().toString().split("/");

		System.out.println(url[2]);

		switch (url[2]) {
			case "slackbot" :
				slackBot.handle(exchange);
				return;
			case "twooter" :
				twooter.API(exchange);
				return;

		}

		switch (exchange.getRequestMethod()) {

			case "GET" :
				GET(exchange);
				return;
			case "POST" :
				POST(exchange);
				return;
			case "PUT" :
				PUT(exchange);
				return;
			case "DELETE" :
				DELETE(exchange);
				return;

		}
		exchange.getResponseBody().write("ERROR 404".getBytes());
		exchange.sendResponseHeaders(404, 0);
	}

	public void POST(HttpExchange exchange) {
		String URL = exchange.getRequestURI().getPath().replaceAll("/api/", "");
		String body;
		// System.out.println("better");
		switch (URL) {
			case "add_log" :
				body = Util.parseBody(exchange.getRequestBody());
				System.out.println(URL + " request-add_log: " + body);

				try {
					exchange.getResponseHeaders().add("Content-type", "application/json");
					exchange.sendResponseHeaders(200, 0);

					exchange.getResponseBody().write(" {\"ok\":true,\"error\":false} ".getBytes());
					exchange.getResponseBody().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
		}
	}
	public void GET(HttpExchange exchange) {

	}
	public void PUT(HttpExchange exchange) {

	}
	public void DELETE(HttpExchange exchange) {

	}

}
