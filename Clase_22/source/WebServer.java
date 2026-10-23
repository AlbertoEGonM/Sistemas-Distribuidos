import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

public class WebServer {
    private static void reply(HttpExchange e, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        e.sendResponseHeaders(status, bytes.length);
        e.getResponseBody().write(bytes);
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        List<String> workers = args.length > 1 ? Arrays.asList(Arrays.copyOfRange(args, 1, args.length))
                : List.of("http://localhost:8081/task", "http://localhost:8082/task");
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/dispatcher", e -> {
            try {
                if (!e.getRequestURI().getPath().equals("/dispatcher")) {
                    reply(e, 404, "Ruta inexistente"); return;
                }
                String input;
                if (e.getRequestMethod().equals("POST")) {
                    byte[] bytes = e.getRequestBody().readNBytes(4097);
                    if (bytes.length > 4096) { reply(e, 413, "Cuerpo demasiado grande"); return; }
                    input = new String(bytes, StandardCharsets.UTF_8).trim();
                } else if (e.getRequestMethod().equals("GET")) {
                    input = e.getRequestURI().getRawQuery();
                    if (input == null) input = "";
                    input = URLDecoder.decode(input, StandardCharsets.UTF_8);
                    if (input.startsWith("hash=")) input = input.substring(5);
                } else {
                    e.getResponseHeaders().set("Allow", "GET, POST");
                    reply(e, 405, "Use GET o POST"); return;
                }
                List<String> tasks = Aggregator.createTasks(input);
                System.out.println("Nueva consulta: " + input);
                long start = System.nanoTime();
                List<Aggregator.Result> results = new Aggregator().sendTasksToWorkers(workers, tasks);
                double seconds = (System.nanoTime() - start) / 1e9;
                e.getResponseHeaders().set("X-debug-info", String.format(Locale.ROOT, "seconds=%.6f", seconds));
                String report = Aggregator.report(results, seconds);
                System.out.print(report);
                reply(e, 200, report);
            } catch (IllegalArgumentException ex) {
                reply(e, 400, ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                reply(e, 502, "No fue posible completar la consulta con los workers");
            } finally { e.close(); }
        });
        server.createContext("/", e -> {
            try { reply(e, 404, "Use /dispatcher"); } finally { e.close(); }
        });
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Dispatcher en http://localhost:" + port + "/dispatcher");
        System.out.println("Workers: " + workers);
    }
}
