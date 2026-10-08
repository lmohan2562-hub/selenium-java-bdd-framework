package io.github.lehamohan.bdd.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Serves the synthetic patient portal ({@code src/test/resources/webapp}) from the classpath using
 * the JDK's built-in HTTP server, so the UI suite needs no external site and no extra dependency.
 */
public final class DemoAppServer {

    private static final Logger LOG = LoggerFactory.getLogger(DemoAppServer.class);
    private static final String ROOT = "webapp/";
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "js", "application/javascript; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "svg", "image/svg+xml",
            "ico", "image/x-icon");

    private final HttpServer server;
    private final String host;

    public DemoAppServer(String bindAddress, int port, String host) throws IOException {
        this.host = host;
        this.server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
        this.server.setExecutor(Executors.newFixedThreadPool(8));
        this.server.createContext("/", this::handle);
    }

    public DemoAppServer start() {
        server.start();
        LOG.info("event=demo_app_started url={}", baseUrl());
        return this;
    }

    public void stop() {
        server.stop(0);
        LOG.info("event=demo_app_stopped");
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public String baseUrl() {
        return "http://" + host + ":" + port() + "/";
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"GET".equals(exchange.getRequestMethod()) && !"HEAD".equals(exchange.getRequestMethod())) {
                send(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            // Reject traversal attempts; only flat files under webapp/ are served.
            if (path.contains("..") || path.indexOf('/', 1) != -1) {
                send(exchange, 404, "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String resource = ROOT + path.substring(1);
            try (InputStream in = DemoAppServer.class.getClassLoader().getResourceAsStream(resource)) {
                if (in == null) {
                    send(exchange, 404, "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                String ext = resource.substring(resource.lastIndexOf('.') + 1);
                send(exchange, 200, CONTENT_TYPES.getOrDefault(ext, "application/octet-stream"), in.readAllBytes());
            }
        }
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        boolean head = "HEAD".equals(exchange.getRequestMethod());
        exchange.sendResponseHeaders(status, head ? -1 : body.length);
        if (!head) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }
}
