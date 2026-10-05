package Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebHttpServer {
    private static final Path WEB_ROOT = Path.of("src", "main", "java", "Server", "Webpage");
    private static final Path IMAGE_ROOT = Path.of("Images");
    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile(
            "\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\""
    );

    public static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", WebHttpServer::handleRoute);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        return server;
    }

    private static void handleRoute(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();

            if ("/api/login".equals(path)) {
                handleLogin(exchange);
                return;
            }

            if ("/".equals(path) || "/login".equals(path)) {
                sendFile(exchange, WEB_ROOT.resolve("loginView.html"));
                return;
            }

            if ("/dashboard".equals(path)) {
                sendFile(exchange, WEB_ROOT.resolve("dashboardView.html"));
                return;
            }

            if (path.startsWith("/icons/")) {
                sendFile(exchange, resolveStaticPath(WEB_ROOT, path.substring(1)));
                return;
            }

            if (path.startsWith("/Images/")) {
                sendFile(exchange, resolveStaticPath(Path.of("."), path.substring(1)));
                return;
            }

            sendText(exchange, 404, "Not found", "text/plain");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "POST");
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseBody(body, exchange.getRequestHeaders());

        String username = firstNonBlank(params.get("usernameOrEmail"), params.get("email"), params.get("username"));
        String password = firstNonBlank(params.get("password"), params.get("authTokenOrHash"));
        String sessionToken = AuthenticationController.authenticateUser(username, password);

        if (sessionToken == null) {
            sendJson(exchange, 401, "{\"authenticated\":false,\"message\":\"Invalid credentials\"}");
            return;
        }

        String escapedUser = escapeJson(username);
        String escapedSession = escapeJson(sessionToken);
        sendJson(exchange, 200, "{"
                + "\"authenticated\":true,"
                + "\"userId\":\"" + escapedUser + "\","
                + "\"sessionToken\":\"" + escapedSession + "\","
                + "\"isAdmin\":true"
                + "}");
    }

    private static Map<String, String> parseBody(String body, Headers headers) {
        String contentType = headers.getFirst("Content-Type");
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            return parseFlatJson(body);
        }
        return parseFormBody(body);
    }

    private static Map<String, String> parseFlatJson(String json) {
        Map<String, String> result = new HashMap<>();
        Matcher matcher = JSON_FIELD_PATTERN.matcher(json);
        while (matcher.find()) {
            result.put(matcher.group(1), unescapeJson(matcher.group(2)));
        }
        return result;
    }

    private static Map<String, String> parseFormBody(String body) {
        Map<String, String> result = new HashMap<>();
        if (body == null || body.isBlank()) {
            return result;
        }

        for (String pair : body.split("&")) {
            int separator = pair.indexOf('=');
            if (separator < 0) {
                continue;
            }
            String key = urlDecode(pair.substring(0, separator));
            String value = urlDecode(pair.substring(separator + 1));
            result.put(key, value);
        }
        return result;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static Path resolveStaticPath(Path root, String relativePath) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(relativePath).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IOException("Invalid path");
        }
        return resolved;
    }

    private static void sendFile(HttpExchange exchange, Path path) throws IOException {
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath)) {
            sendText(exchange, 404, "Not found", "text/plain");
            return;
        }

        byte[] content = Files.readAllBytes(normalizedPath);
        sendBytes(exchange, 200, content, contentType(normalizedPath));
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        sendText(exchange, statusCode, json, "application/json");
    }

    private static void sendText(HttpExchange exchange, int statusCode, String text, String contentType) throws IOException {
        sendBytes(exchange, statusCode, text.getBytes(StandardCharsets.UTF_8), contentType + "; charset=utf-8");
    }

    private static void sendBytes(HttpExchange exchange, int statusCode, byte[] body, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private static String contentType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
