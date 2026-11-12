import java.net.URI;
import java.net.http.*;
import java.util.*;
import com.google.gson.Gson;

public class ClienteSincrono {
    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "http://localhost:8080";
        HttpClient cliente = HttpClient.newHttpClient();
        Random random = new Random(12345);
        Gson gson = new Gson();
        var nombres = CombinacionesArchivos.leerArchivo("nombres.txt");
        var apellidos = CombinacionesArchivos.leerArchivo("apellidos.txt");
        if (nombres.isEmpty() || apellidos.isEmpty()) throw new IllegalStateException("Faltan nombres o apellidos");
        int total = 0;
        for (String nombre : nombres) for (String ap1 : apellidos) for (String ap2 : apellidos) {
            double promedio = Math.round(random.nextDouble() * 100 * 10) / 100.0;
            String json = String.format(Locale.ROOT,
                "{\"nombre\":%s,\"apellido1\":%s,\"apellido2\":%s,\"promedio\":%.2f}",
                gson.toJson(nombre), gson.toJson(ap1), gson.toJson(ap2), promedio);
            var peticion = HttpRequest.newBuilder(URI.create(base + "/alumnos"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            System.out.println("POST " + peticion.uri() + " " + json);
            var respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            System.out.println("Respuesta: " + respuesta.statusCode() + " " + respuesta.body());
            if (respuesta.statusCode() != 201) throw new IllegalStateException("Error al insertar");
            total++;
        }
        System.out.println("Insertados: " + total);
        var respuesta = cliente.send(HttpRequest.newBuilder(URI.create(base + "/alumnos")).GET().build(), HttpResponse.BodyHandlers.ofString());
        System.out.println("GET /alumnos -> " + respuesta.statusCode() + " " + respuesta.body());
    }
}
