import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/** Adaptacion del servidor HTTP de las clases anteriores para REST parte 1. */
public class WebServer {
    private final Map<Integer, Alumno> alumnos = new HashMap<>();
    private int siguienteId = 1;

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new WebServer().iniciar(puerto);
    }

    private void iniciar(int puerto) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/status", exchange -> {
            try {
                if (!exchange.getRequestURI().getPath().equals("/status")) {
                    reply(exchange, 404, "Ruta inexistente");
                } else if (!exchange.getRequestMethod().equals("GET")) {
                    exchange.getResponseHeaders().set("Allow", "GET");
                    reply(exchange, 405, "Use GET");
                } else {
                    reply(exchange, 200, "Servidor activo y disponible");
                }
            } finally { exchange.close(); }
        });
        servidor.createContext("/alumnos", this::handleAlumnosRequest);
        servidor.createContext("/", exchange -> {
            try { reply(exchange, 404, "Ruta inexistente"); }
            finally { exchange.close(); }
        });
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        servidor.start();
        System.out.println("micro-SAES en http://localhost:" + puerto);
        System.out.println("GET /status | POST /alumnos | GET, PUT, DELETE /alumnos/{id}");
        System.out.println("Datos en memoria. Detener con Ctrl+C.");
    }

    private void handleAlumnosRequest(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        String metodo = exchange.getRequestMethod();
        System.out.println("Ruta solicitada: " + ruta);
        System.out.println("Metodo solicitado: " + metodo);
        try {
            boolean coleccion = ruta.equals("/alumnos");
            if (!coleccion && !ruta.matches("/alumnos/[1-9][0-9]*")) {
                reply(exchange, 404, "Ruta inexistente");
                return;
            }
            int id = coleccion ? 0 : Integer.parseInt(ruta.split("/")[2]);
            if (metodo.equals("POST") && coleccion) {
                Alumno alumno = leerAlumno(exchange, 0);
                // El bloqueo mantiene consistente el HashMap y la asignacion del ID.
                synchronized (alumnos) {
                    alumno.id = siguienteId++;
                    alumnos.put(alumno.id, alumno);
                }
                exchange.getResponseHeaders().set("Location", "/alumnos/" + alumno.id);
                reply(exchange, 201, alumno.toString());
            } else if (metodo.equals("GET") && !coleccion) {
                Alumno alumno;
                synchronized (alumnos) { alumno = alumnos.get(id); }
                reply(exchange, alumno == null ? 404 : 200,
                        alumno == null ? "Alumno no encontrado" : alumno.toString());
            } else if (metodo.equals("PUT") && !coleccion) {
                Alumno actualizado = leerAlumno(exchange, id);
                boolean existe;
                synchronized (alumnos) {
                    existe = alumnos.containsKey(id);
                    if (existe) alumnos.put(id, actualizado);
                }
                reply(exchange, existe ? 200 : 404,
                        existe ? actualizado.toString() : "Alumno no encontrado");
            } else if (metodo.equals("DELETE") && !coleccion) {
                Alumno eliminado;
                synchronized (alumnos) { eliminado = alumnos.remove(id); }
                reply(exchange, eliminado == null ? 404 : 200, eliminado == null
                        ? "Alumno no encontrado" : "Alumno con ID: " + id + " removido de la lista de alumnos.");
            } else {
                exchange.getResponseHeaders().set("Allow", coleccion ? "POST" : "GET, PUT, DELETE");
                reply(exchange, 405, coleccion ? "Use POST /alumnos" : "Use GET, PUT o DELETE /alumnos/{id}");
            }
        } catch (IllegalArgumentException ex) {
            reply(exchange, 400, "Datos invalidos. Use nombre,apellido1,apellido2,promedio");
        } finally { exchange.close(); }
    }

    private static Alumno leerAlumno(HttpExchange exchange, int id) throws IOException {
        String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String[] datos = cuerpo.strip().split(",", -1);
        if (datos.length != 4) throw new IllegalArgumentException();
        for (int i = 0; i < datos.length; i++) datos[i] = datos[i].strip();
        if (datos[0].isEmpty() || datos[1].isEmpty()) throw new IllegalArgumentException();
        double promedio = Double.parseDouble(datos[3]);
        if (!Double.isFinite(promedio)) throw new IllegalArgumentException();
        return new Alumno(id, datos[0], datos[1], datos[2], promedio);
    }

    private static void reply(HttpExchange exchange, int status, String texto) throws IOException {
        byte[] bytes = (texto + "\n").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
