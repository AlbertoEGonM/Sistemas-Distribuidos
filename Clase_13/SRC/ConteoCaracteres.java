import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ConteoCaracteres {

    public static void main(String[] args) {
        // Se puede indicar la ruta como argumento al ejecutar el programa.
        Path archivo = Paths.get(args.length > 0
                ? args[0] : "El_viejo_y_el_mar.txt");

        Map<Character, Integer> frecuencias = new HashMap<>();
        long total = 0;

        // UTF-8 permite leer correctamente los acentos del archivo adjunto.
        // El recurso se cierra automaticamente al terminar el bloque try.
        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {
            int valor;
            // read() devuelve -1 al llegar al final del archivo.
            while ((valor = lector.read()) != -1) {
                char caracter = (char) valor;
                int cantidad = frecuencias.getOrDefault(caracter, 0);
                frecuencias.put(caracter, cantidad + 1);
                total++;
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer: " + archivo.toAbsolutePath());
            System.err.println("Detalle: " + e.getMessage());
            System.exit(1);
            return;
        }

        System.out.println("EJERCICIO 1: CONTEO DE CARACTERES");
        System.out.println("Total de caracteres: " + total);
        System.out.println("Caracteres distintos: " + frecuencias.size());
        System.out.println("Map original: " + frecuencias);

        // Una entrada contiene una clave (caracter) y un valor (frecuencia).
        ArrayList<Map.Entry<Character, Integer>> lista =
                new ArrayList<>(frecuencias.entrySet());

        // Comparator ordena primero por frecuencia, de menor a mayor.
        Comparator<Map.Entry<Character, Integer>> comparador =
                new Comparator<Map.Entry<Character, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Character, Integer> primero,
                                       Map.Entry<Character, Integer> segundo) {
                        int resultado = Integer.compare(
                                primero.getValue(), segundo.getValue());
                        if (resultado != 0) {
                            return resultado;
                        }
                        // Si hay empate, se ordena por el valor del caracter.
                        return Character.compare(
                                primero.getKey(), segundo.getKey());
                    }
                };

        lista.sort(comparador);

        System.out.println("\nEJERCICIO 2: DE MENOR A MAYOR OCURRENCIA");
        System.out.printf("%-24s %s%n", "Caracter", "Ocurrencias");
        for (Map.Entry<Character, Integer> entrada : lista) {
            System.out.printf("%-24s %d%n",
                    representar(entrada.getKey()), entrada.getValue());
        }
    }

    // Hace visibles los caracteres que normalmente no se ven en pantalla.
    private static String representar(char caracter) {
        switch (caracter) {
            case ' ': return "[espacio]";
            case '\n': return "[salto de linea \\n]";
            case '\r': return "[retorno de carro \\r]";
            case '\t': return "[tabulacion \\t]";
            default:
                if (Character.isISOControl(caracter)
                        || Character.isWhitespace(caracter)
                        || Character.isSpaceChar(caracter)) {
                    return String.format("[U+%04X]", (int) caracter);
                }
                return "'" + caracter + "'";
        }
    }
}
