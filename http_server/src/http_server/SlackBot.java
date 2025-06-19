package http_server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lib.Util;

public class SlackBot implements HttpHandler {

	private final String token = "";

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		switch (exchange.getRequestURI().toString()) {

			case "/api/slackbot/post-message" :
				System.out.println("you got this far");
				try {

					String text = Util.parseBody(exchange.getRequestBody());
					System.out.println("requestBody:" + text);

					text = JsonParser.parseString(text).getAsJsonObject().get("text").getAsString();
					text = text.substring(1, text.length() - 1);
					text = text.replace("'", "\\]");

					System.out.println("text:" + text);

					StringBuilder build = new StringBuilder();

					build.append("{");

					build.append("'channel':'C081WGJELSZ',");
					build.append("'text':'" + text + "',");
					build.append("'username':'Automated Message',");
					build.append("'icon_emoji':':robot_face:'");

					build.append("}");

					String requestBody = build.toString();
					System.out.println("body:" + requestBody);

					requestBody = requestBody.replaceAll("'", "\"");
					requestBody = requestBody.replace("\\]", "'");
					System.out.println("body:" + requestBody);

					URI uri = URI.create("https://slack.com/api/chat.postMessage");
					BodyPublisher body = HttpRequest.BodyPublishers.ofString(requestBody);

					HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body)
							.header("Content-Type", "application/json")
							.header("Authorization", "Bearer " + token).build();

					HttpResponse<?> response = Util.request(request);

					System.out.println("repo code: " + response.statusCode());
					System.out.println("repo body: " + response.body().toString());

					exchange.sendResponseHeaders(200, 0);

					exchange.getResponseBody().write(response.body().toString().getBytes());
					exchange.getResponseBody().close();
				} catch (Exception e) {
					System.out.println("slack bot-message");
					e.printStackTrace();
				}

				break;
			default :
				break;
		}

	}

}
