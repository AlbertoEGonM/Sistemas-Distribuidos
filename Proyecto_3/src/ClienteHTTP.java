// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Todas las operaciones usan HttpClient.send: HTTP sincrono. */
public final class ClienteHTTP {
    private final URI base;
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4)).build();

    public ClienteHTTP(String direccion) {
        base = URI.create(direccion.endsWith("/") ? direccion : direccion + "/");
        if (!"http".equalsIgnoreCase(base.getScheme()) || base.getHost() == null
                || !base.getPath().equals("/") || base.getQuery() != null || base.getFragment() != null)
            throw new IllegalArgumentException("Use http://host:puerto sin rutas ni parametros.");
    }

    public int total() throws IOException, InterruptedException { return enviar("estado", null); }

    public int agregar(double v, double dx, double dy, double giro) throws IOException, InterruptedException {
        FabricaAsteroides.validar(v, dx, dy, giro);
        return enviar("asteroides", "v=" + codificar(v) + "&dx=" + codificar(dx)
                + "&dy=" + codificar(dy) + "&omega=" + codificar(giro));
    }

    public int aleatorios(int n) throws IOException, InterruptedException {
        if (n < 1 || n > Simulacion.MAX_LOTE)
            throw new IllegalArgumentException("Cantidad: 1 a " + Simulacion.MAX_LOTE);
        return enviar("aleatorios", "n=" + n);
    }

    private static String codificar(double valor) {
        return URLEncoder.encode(Double.toString(valor), StandardCharsets.UTF_8);
    }

    private int enviar(String ruta, String cuerpo) throws IOException, InterruptedException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(base.resolve(ruta))
                .timeout(Duration.ofSeconds(15));
        if (cuerpo == null) peticion.GET();
        else peticion.header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        HttpResponse<String> respuesta = cliente.send(peticion.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String texto = respuesta.body().trim();
        if (respuesta.statusCode() / 100 != 2)
            throw new IOException("HTTP " + respuesta.statusCode() + ": " + texto);
        if (!texto.startsWith("total=")) throw new IOException("Respuesta no reconocida del servidor.");
        try { return Integer.parseInt(texto.substring(6)); }
        catch (NumberFormatException ex) { throw new IOException("Contador invalido.", ex); }
    }
}
