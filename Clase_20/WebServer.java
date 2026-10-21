import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servidor independiente para los cinco ejercicios de la Clase 16. Java 11+.
 */
public class WebServer {
    public static void main(String[] args) throws IOException {
        // Los argumentos permiten repetir el experimento sin editar el archivo.
        int hilos = args.length > 0 ? Integer.parseInt(args[0]) : 8;
        long pausaMs = args.length > 1 ? Long.parseLong(args[1]) : 0;
	int puerto = args.length > 2 ? Integer.parseInt(args[2]) : 8080;
        if (hilos < 1 || pausaMs < 0) {
            throw new IllegalArgumentException("Hilos positivos y pausa no negativa.");
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);
	server.createContext("/status", exchange -> {
    byte[] respuesta = (
        "El servidor esta vivo en el puerto " + puerto + "\n"
    ).getBytes(StandardCharsets.UTF_8);

    exchange.getResponseHeaders().set(
        "Content-Type", "text/plain; charset=UTF-8"
    );

    try {
        exchange.sendResponseHeaders(200, respuesta.length);
        exchange.getResponseBody().write(respuesta);
    } finally {
        exchange.close();
    }
});
        server.createContext("/task", exchange -> atender(exchange, false, pausaMs));
        server.createContext("/searchtoken", exchange -> atender(exchange, true, 0));
        // Ejercicio 1: hasta 'hilos' solicitudes se procesan simultaneamente.
        server.setExecutor(Executors.newFixedThreadPool(hilos));
        server.start();
        System.out.printf("Servidor en http://localhost:8080 | hilos=%d | pausa /task=%d ms%n",
                hilos, pausaMs);
    }

    private static void atender(HttpExchange exchange, boolean buscar, long pausaMs)
            throws IOException {
        // nanoTime sirve para medir intervalos, no la fecha actual.
        long inicio = System.nanoTime();
        int estado = 200;
        String respuesta;
        try {
            String ruta = buscar ? "/searchtoken" : "/task";
            if (!exchange.getRequestURI().getPath().equals(ruta)) {
                estado = 404;
                respuesta = "Endpoint inexistente\n";
            } else if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                estado = 405;
                exchange.getResponseHeaders().set("Allow", "POST");
                respuesta = "Utiliza POST\n";
            } else {
                // Ejercicio 2: se cuentan bytes antes de decodificar UTF-8.
                byte[] cuerpo = exchange.getRequestBody().readAllBytes();
                String texto = new String(cuerpo, StandardCharsets.UTF_8);
                if (!buscar) {
                    imprimirSolicitud(exchange, cuerpo.length, texto);
                    // La pausa simula una tarea lenta; esta dentro del handler.
                    Thread.sleep(pausaMs);
                    respuesta = "Solicitud procesada\n";
                } else {
                    respuesta = Long.toString(buscarTokens(texto));
                }
            }
        } catch (IllegalArgumentException e) {
            estado = 400;
            respuesta = "Solicitud invalida: " + e.getMessage() + "\n";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            estado = 503;
            respuesta = "Procesamiento interrumpido\n";
        }
        byte[] salida = respuesta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        // Ejercicio 4: el header solo se agrega si el cliente activa X-Debug.
        if ("true".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-Debug"))) {
            long ns = System.nanoTime() - inicio;
            exchange.getResponseHeaders().set("X-Debug-info", formatearTiempo(ns));
        }
        try {
            exchange.sendResponseHeaders(estado, salida.length);
            exchange.getResponseBody().write(salida);
        } finally {
            exchange.close();
        }
    }

    private static void imprimirSolicitud(HttpExchange exchange, int bytes, String texto) {
        Headers headers = exchange.getRequestHeaders();
        StringBuilder registro = new StringBuilder();
        registro.append("\n--- Solicitud /task ---\n")
                .append("Hilo: ").append(Thread.currentThread().getName()).append('\n')
                .append("Bytes del cuerpo: ").append(bytes).append('\n')
                .append("Contenido: ").append(texto).append('\n')
                .append("Cantidad de nombres de headers: ").append(headers.size()).append('\n');
        int valores = 0;
        // Ejercicio 3: un nombre puede estar asociado con varios valores.
        for (Map.Entry<String, List<String>> entrada : headers.entrySet()) {
            for (String valor : entrada.getValue()) {
                registro.append(entrada.getKey()).append(" - ").append(valor).append('\n');
                valores++;
            }
        }
        registro.append("Cantidad de pares key - value: ").append(valores).append('\n');
        // Un solo println evita mezclar las lineas de dos solicitudes concurrentes.
        System.out.println(registro);
    }

    private static String formatearTiempo(long ns) {
        long segundos = ns / 1_000_000_000L;
        long milisegundos = (ns % 1_000_000_000L) / 1_000_000L;
        return "La operacion tomo " + ns + " nanosegundos = "
                + segundos + " segundos con " + milisegundos + " milisegundos.";
    }

    private static long buscarTokens(String entrada) {
        // Ejercicio 5: formato del cuerpo: numeroDeTokens,SUB.
        String[] datos = entrada.trim().split(",", -1);
        if (datos.length != 2) {
            throw new IllegalArgumentException("Formato esperado: numeroDeTokens,SUB");
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(datos[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El numero de tokens debe ser entero.");
        }
        String subcadena = datos[1].trim();
        if (cantidad < 0 || cantidad > (Integer.MAX_VALUE - 8) / 4) {
            throw new IllegalArgumentException("Cantidad fuera del rango permitido.");
        }
        if (!subcadena.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("La subcadena debe contener tres letras A-Z.");
        }
        // Supuesto de esta version: tokens de tres letras mayusculas separados
        // por un espacio. Sustituir por el generador de Clase 7 si difiere.
        StringBuilder cadenota = new StringBuilder(cantidad * 4);
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        for (int i = 0; i < cantidad; i++) {
            for (int j = 0; j < 3; j++) {
                cadenota.append((char) ('A' + azar.nextInt(26)));
            }
            cadenota.append(' ');
        }
        long coincidencias = 0;
        int posicion = 0;
        while ((posicion = cadenota.indexOf(subcadena, posicion)) != -1) {
            coincidencias++;
            posicion++; // Permite contar coincidencias superpuestas.
        }
        return coincidencias;
    }
}
