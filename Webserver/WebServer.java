package Webserver;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class WebServer {

    // List to store registered peers
    private static final List<Peer> peers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Create an HTTP server that listens on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        // Define the "/register" endpoint
        server.createContext("/register", (exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Parse request body
                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                String body = new String(requestBody);

                // Expecting JSON-like: "ip=192.168.1.100&port=6881"
                String[] parts = body.split("&");
                String ip = parts[0].split("=")[1];
                int port = Integer.parseInt(parts[1].split("=")[1]);

                Peer peer = new Peer(ip, port);
                if (!peers.contains(peer)) {
                    peers.add(peer);
                    System.out.println("Peer adicionado: " + peer);
                }

                String response = "Peer registered: " + peer;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }));

        // Define the "/peers" endpoint
        server.createContext("/peers", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder response = new StringBuilder();
                for (Peer peer : peers) {
                    response.append(peer).append("\n");
                }

                exchange.sendResponseHeaders(200, response.toString().getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }));

        // Set an executor for the server
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Start the server
        System.out.println("Server started on port 8080");
        server.start();
    }
}
