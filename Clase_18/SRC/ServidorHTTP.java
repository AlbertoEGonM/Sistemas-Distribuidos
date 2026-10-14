import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.Executors;

// Ejecutar desde la carpeta que contiene resources.
public class ServidorHTTP {
    private static final Path RECURSOS = Path.of("resources");

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        // 0.0.0.0 permite conexiones desde otros dispositivos de la red.
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", puerto), 0);
        servidor.createContext("/status", e -> manejar(e, "/status", "GET"));
        servidor.createContext("/task", e -> manejar(e, "/task", "POST"));
        servidor.createContext("/ipn", e -> manejar(e, "/ipn", "GET"));
        servidor.createContext("/", e -> {
            try {
                texto(e, 404, "Endpoint inexistente\n");
            } finally {
                e.close();
            }
        });
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        servidor.start();
        System.out.println("Servidor iniciado: http://127.0.0.1:" + puerto);
        System.out.println("Endpoints: GET /status, POST /task, GET /ipn");
        System.out.println("Detener con Ctrl+C.");
    }

    private static void manejar(HttpExchange e, String ruta, String metodo) throws IOException {
        try {
            System.out.println(e.getRemoteAddress() + " " + e.getRequestMethod() + " " + e.getRequestURI());
            if (!e.getRequestURI().getPath().equals(ruta)) {
                texto(e, 404, "Endpoint inexistente\n");
                return;
            }
            if (!e.getRequestMethod().equals(metodo)) {
                e.getResponseHeaders().set("Allow", metodo);
                texto(e, 405, "405 Method Not Allowed: use " + metodo + "\n");
                return;
            }
            switch (ruta) {
                case "/status":
                    texto(e, 200, "Servidor activo y disponible\n");
                    break;
                case "/task":
                    multiplicar(e);
                    break;
                case "/ipn":
                    enviarIPN(e);
                    break;
                default:
                    texto(e, 404, "Endpoint inexistente\n");
            }
        } finally {
            e.close();
        }
    }

    private static void multiplicar(HttpExchange e) throws IOException {
        long inicio = System.nanoTime();
        String tipo = e.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.split(";", 2)[0].trim().equalsIgnoreCase("application/x-www-form-urlencoded")) {
            texto(e, 415, "Use Content-Type: application/x-www-form-urlencoded\n");
            return;
        }
        // Limita el cuerpo para evitar solicitudes excesivas.
        byte[] cuerpo = e.getRequestBody().readNBytes(4097);
        if (cuerpo.length > 4096) {
            texto(e, 413, "Cuerpo demasiado grande\n");
            return;
        }
        try {
            Map<String, String> datos = new HashMap<>();
            for (String campo : new String(cuerpo, StandardCharsets.UTF_8).split("&")) {
                String[] par = campo.split("=", 2);
                if (par.length != 2)
                    throw new IllegalArgumentException();
                String clave = URLDecoder.decode(par[0], StandardCharsets.UTF_8);
                String valor = URLDecoder.decode(par[1], StandardCharsets.UTF_8);
                if (datos.putIfAbsent(clave, valor) != null)
                    throw new IllegalArgumentException();
            }
            if (datos.size() != 2 || !datos.containsKey("a") || !datos.containsKey("b")) {
                throw new IllegalArgumentException();
            }
            BigInteger a = new BigInteger(datos.get("a"));
            BigInteger b = new BigInteger(datos.get("b"));
            BigInteger resultado = a.multiply(b);
            // Tiempo interno: lectura, validacion y multiplicacion; no incluye la red.
            long tiempoNs = System.nanoTime() - inicio;
            String respuesta = String.format(Locale.ROOT,
                    "a=%s\nb=%s\nresultado=%s\ntiempo_servidor_ns=%d\ntiempo_servidor_ms=%.6f\n",
                    a, b, resultado, tiempoNs, tiempoNs / 1_000_000.0);
            texto(e, 200, respuesta);
        } catch (IllegalArgumentException ex) {
            texto(e, 400, "Envie exactamente dos enteros con formato a=6&b=7\n");
        }
    }

    private static void enviarIPN(HttpExchange e) throws IOException {
        String agente = e.getRequestHeaders().getFirst("User-Agent");
        boolean java = agente != null &&
                (agente.startsWith("Java 11 HttpClient Bot") || agente.startsWith("Java-http-client/"));
        Path archivo = RECURSOS.resolve(java ? "ascii-ipn.txt" : "ipn.png");
        if (!Files.isRegularFile(archivo)) {
            texto(e, 500, "Falta el archivo: " + archivo + ". Ejecute desde la carpeta del proyecto.\n");
            return;
        }
        e.getResponseHeaders().set("Vary", "User-Agent");
        if (java) {
            String ascii = Files.readString(archivo, StandardCharsets.UTF_8);
            texto(e, 200, ascii);
        } else {
            byte[] imagen = Files.readAllBytes(archivo);
            responder(e, 200, "image/png", imagen);
        }
    }

    private static void texto(HttpExchange e, int codigo, String mensaje) throws IOException {
        responder(e, codigo, "text/plain; charset=UTF-8", mensaje.getBytes(StandardCharsets.UTF_8));
    }

    private static void responder(HttpExchange e, int codigo, String tipo, byte[] cuerpo) throws IOException {
        e.getResponseHeaders().set("Content-Type", tipo);
        e.sendResponseHeaders(codigo, cuerpo.length);
        e.getResponseBody().write(cuerpo);
    }
}
