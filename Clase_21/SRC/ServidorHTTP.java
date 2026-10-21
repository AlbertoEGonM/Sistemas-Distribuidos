import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    buscarHash(e);
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

    /** POST /task: cuerpo sin claves ni formulario, en formato hashObjetivo,letra. */
    private static void buscarHash(HttpExchange e) throws IOException {
        long inicio = System.nanoTime();
        int estado = 200;
        String respuesta;
        try {
            byte[] cuerpo = e.getRequestBody().readNBytes(4097);
            if (cuerpo.length > 4096) {
                estado = 413;
                respuesta = "Cuerpo demasiado grande\n";
            } else {
                String[] datos = new String(cuerpo, StandardCharsets.UTF_8).trim().split(",", -1);
                if (datos.length != 2 || !datos[1].trim().matches("[a-z]")) {
                    throw new IllegalArgumentException("Formato esperado: hashObjetivo,letra (a-z)");
                }
                char letra = datos[1].trim().charAt(0);
                MotorHash.Resultado resultado = MotorHash.buscar(datos[0].trim(), letra, letra);
                respuesta = resultado.palabra == null ? "NULL" : resultado.palabra;
            }
        } catch (IllegalArgumentException ex) {
            estado = 400;
            respuesta = ex.getMessage() + "\n";
        } catch (Exception ex) {
            estado = 500;
            respuesta = "Error interno\n";
            ex.printStackTrace();
        }
        long tiempoNs = System.nanoTime() - inicio;
        e.getResponseHeaders().set("X-debug-info", String.format(Locale.ROOT,
                "seconds=%.6f; milliseconds=%.3f", tiempoNs / 1_000_000_000.0, tiempoNs / 1_000_000.0));
        texto(e, estado, respuesta);
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

    // Cada solicitud usa su propio MessageDigest.
private static class MotorHash {
    public static String sha256(String texto) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
            .digest(texto.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(64);
        for (byte b : digest) hex.append(String.format("%02x", b & 255));
        return hex.toString();
    }
    public static byte[] decodificar(String hash) {
        if (!hash.matches("[0-9a-fA-F]{64}"))
            throw new IllegalArgumentException("El hash debe tener 64 caracteres hexadecimales");
        byte[] bytes = new byte[32];
        for (int i = 0; i < 32; i++)
            bytes[i] = (byte) Integer.parseInt(hash.substring(i*2, i*2+2), 16);
        return bytes;
    }
    public static class Resultado {
        public final String palabra;
        public final long intentos;
        Resultado(String palabra, long intentos) { this.palabra = palabra; this.intentos = intentos; }
    }
    public static Resultado buscar(String hash, char desde, char hasta) throws Exception {
        if (desde < 'a' || hasta > 'z' || desde > hasta)
            throw new IllegalArgumentException("Rango invalido");
        byte[] objetivo = decodificar(hash);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] candidato = new byte[5];
        long intentos = 0;
        for (char a = desde; a <= hasta; a++) {
            candidato[0] = (byte) a;
            for (char b = 'a'; b <= 'z'; b++) {
                candidato[1] = (byte) b;
                for (char c = 'a'; c <= 'z'; c++) {
                    candidato[2] = (byte) c;
                    for (char d = 'a'; d <= 'z'; d++) {
                        candidato[3] = (byte) d;
                        for (char e = 'a'; e <= 'z'; e++) {
                            candidato[4] = (byte) e;
                            intentos++;
                            if (Arrays.equals(md.digest(candidato), objetivo))
                                return new Resultado(new String(candidato, StandardCharsets.UTF_8), intentos);
                        }
                    }
                }
            }
        }
        return new Resultado(null, intentos);
    }
    }
}
