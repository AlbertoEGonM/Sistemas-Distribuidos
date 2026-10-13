package com.mkyong.java11.jep321;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpClientSynchronous {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://httpbin.org/get"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        HttpHeaders headers = response.headers(); // Obtiene los encabezados de la respuesta 
        Map<String, List<String>> original = headers.map(); //Ejercicio 1

        // Ejercicio 1: recorrer las entradas con un iterador.
        System.out.println(" EJERCICIO 1: HEADERS ORIGINALES ");
        Iterator<Map.Entry<String, List<String>>> iterator = //original.entrySet().iterator();
                original.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next(); // Obtiene la entrada actual
            String name = entry.getKey(); // Obtiene el nombre del encabezado
            List<String> values = entry.getValue(); // Obtiene los valores del encabezado
            for (String value : values) { // Itera sobre los valores del encabezado
                System.out.println(name + ": " + value); // Imprime el nombre y valor del encabezado
            }
        }

        // Ejercicio 2: Copiar el mapa original y agregar un nuevo encabezado 
        Map<String, List<String>> copy = new LinkedHashMap<>(); // Crea un nuevo mapa para copiar los encabezados
        original.forEach((name, values) -> // Itera sobre las entradas del mapa original
                copy.put(name, new ArrayList<>(values))); // Copia los valores del encabezado al nuevo mapa

        
        List<String> cookies = copy.computeIfAbsent( // Agrega un nuevo encabezado "set-cookie" al mapa copiado
                "set-cookie", name -> new ArrayList<>());
        cookies.add("Max-Age=0");
        cookies.add("id=123"); 
        cookies.add("theme=dark");

        System.out.println("\n EJERCICIO 2: COPIA CON SET-COOKIE");
        copy.forEach((name, values) ->
                values.forEach(value -> System.out.println(name + ": " + value)));

        // Ejercicio 3: ordenar las entradas por su llave e imprimirlas.
        System.out.println("\n EJERCICIO 3: HEADERS ORDENADOS ");
        copy.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Ordena las entradas del mapa copiado por su llave
                .forEach(entry -> entry.getValue().forEach(value -> //Con el for each se imprime cada valor de la entrada ordenada
                        System.out.println(entry.getKey() + ": " + value)));

        System.out.println("\n CODIGO DE ESTADO ");
        System.out.println(response.statusCode());
        System.out.println("\n CUERPO DE LA RESPUESTA ");
        System.out.println(response.body());
    }
}
