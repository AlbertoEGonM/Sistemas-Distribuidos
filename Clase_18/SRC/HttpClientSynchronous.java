import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

// Ejercicio 1: /status. Ejercicio 4: /ipn.
public class HttpClientSynchronous {
        private static final HttpClient CLIENTE = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(10)).build();

        public static void main(String[] args) throws IOException, InterruptedException {
                String url = args.length > 0 ? args[0] : "http://127.0.0.1:8080/status";
                HttpRequest solicitud = HttpRequest.newBuilder().uri(URI.create(url))
                                .GET().setHeader("User-Agent", "Java 11 HttpClient Bot")
                                .timeout(Duration.ofSeconds(15)).build();
                HttpResponse<String> respuesta = CLIENTE.send(solicitud,
                                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                System.out.println("=== ENCABEZADOS ===");
                respuesta.headers().map().forEach((k, v) -> System.out.println(k + ": " + v));
                System.out.println("\nCodigo HTTP: " + respuesta.statusCode());
                System.out.println("\n=== CUERPO ===");
                System.out.println(respuesta.body());
        }
}
