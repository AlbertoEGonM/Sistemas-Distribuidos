import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Locale;

/** GET /task?hashObjetivo,letra o POST /task con cuerpo hashObjetivo,letra. */
public class ServidorHash {
    private static void responder(HttpExchange ex, int codigo, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (java.io.OutputStream out = ex.getResponseBody()) { out.write(bytes); }
    }
    public static void main(String[] args) throws Exception {
        int puerto = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", puerto), 0);
        servidor.createContext("/task", ex -> {
            long inicio = System.nanoTime();
            int estado = 200;
            String respuesta;
            try {
                if (!ex.getRequestURI().getPath().equals("/task")) {
                    estado = 404; respuesta = "Ruta no encontrada";
                } else if (!ex.getRequestMethod().equals("GET") && !ex.getRequestMethod().equals("POST")) {
                    ex.getResponseHeaders().set("Allow", "GET, POST");
                    estado = 405; respuesta = "Use GET o POST";
                } else {
                    String entrada = ex.getRequestMethod().equals("GET")
                        ? ex.getRequestURI().getRawQuery()
                        : new String(ex.getRequestBody().readNBytes(1024), StandardCharsets.UTF_8);
                    if (entrada == null) throw new IllegalArgumentException("Falta hashObjetivo,letra");
                    entrada = URLDecoder.decode(entrada, StandardCharsets.UTF_8);
                    String[] partes = entrada.trim().split(",", -1);
                    if (partes.length != 2 || !partes[1].matches("[a-z]"))
                        throw new IllegalArgumentException("Formato: hashObjetivo,letra (a-z)");
                    char letra = partes[1].charAt(0);
                    FuerzaBrutaHash.Resultado r = FuerzaBrutaHash.buscar(partes[0], letra, letra);
                    respuesta = r.palabra == null ? "NULL" : r.palabra;
                }
            } catch (IllegalArgumentException e) {
                estado = 400; respuesta = e.getMessage();
            } catch (Exception e) {
                estado = 500; respuesta = "Error interno";
                e.printStackTrace();
            }
            double ms = (System.nanoTime() - inicio) / 1_000_000.0;
            ex.getResponseHeaders().set("X-debug-info",
                String.format(Locale.ROOT, "seconds=%.6f; milliseconds=%.3f", ms/1000, ms));
            responder(ex, estado, respuesta);
        });
        servidor.start();
        System.out.println("Servidor: http://127.0.0.1:" + puerto + "/task");
        System.out.println("Detener: Ctrl+C");
    }
}
