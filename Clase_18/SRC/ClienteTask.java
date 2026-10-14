import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

public class ClienteTask {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Uso: java ClienteTask <enteroA> <enteroB> [URL de /task]");
            return;
        }
        BigInteger a, b;
        try {
            a = new BigInteger(args[0]);
            b = new BigInteger(args[1]);
        } catch (NumberFormatException ex) {
            System.out.println("Ambos argumentos deben ser enteros.");
            return;
        }
        String url = args.length == 3 ? args[2] : "http://127.0.0.1:8080/task";
        String cuerpo = "a=" + URLEncoder.encode(a.toString(), StandardCharsets.UTF_8)
                + "&b=" + URLEncoder.encode(b.toString(), StandardCharsets.UTF_8);
        HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest solicitud = HttpRequest.newBuilder().uri(URI.create(url))
                .header("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .timeout(Duration.ofSeconds(15))
                // Convierte el String en el cuerpo que se envia mediante POST.
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8)).build();
        long inicio = System.nanoTime();
        HttpResponse<String> respuesta = cliente.send(solicitud,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long tiempoTotal = System.nanoTime() - inicio;
        System.out.println("Codigo HTTP: " + respuesta.statusCode());
        System.out.println(respuesta.body());
        System.out.printf(Locale.ROOT, "Tiempo total del cliente: %.6f ms%n", tiempoTotal / 1_000_000.0);
    }
}
