import java.util.ArrayList;
import java.util.ListIterator;
 //Actividad Clase 11 - Programadores y On-line
 
public class Ejercicio2 {
    //El segundo argumento de Ejercicio1 indica el sexo que se ELIMINA.
    public static void main(String[] args) {
        if (args.length != 1) { // Se espera un argumento: la cantidad de CURPs a generar.
            System.out.println("Uso: java Ejercicio2 <cantidad>"); // Se espera un argumento: la cantidad de CURPs a generar.
            return;
        }

        int n;
        // Validar que el argumento sea un número entero no negativo.
        try {
            n = Integer.parseInt(args[0]); // Convertir el argumento a un número entero.
        } catch (NumberFormatException e) { // Capturar la excepción si el argumento no es un número entero.
            System.out.println("La cantidad debe ser un numero entero."); // Mostrar mensaje de error si el argumento no es un número entero.
            return;
        }
        // Validar que la cantidad no sea negativa.
        if (n < 0) {
            System.out.println("La cantidad no puede ser negativa."); // Mostrar mensaje de error si la cantidad es negativa.
            return;
        }
        // Crear la lista y generar las CURP.
        ArrayList<String> curps = new ArrayList<>();
        // Mostrar la lista ANTES de insertar.
        System.out.println("CURPs originales:");
        for (int i = 0; i < n; i++) { // Generar n CURPs y mostrarlas.
            String nueva = GeneradorCURP.generar(); // Generar una nueva CURP.
            ListIterator<String> cursor = curps.listIterator(); // Crear un ListIterator para recorrer la lista de CURPs.

            while (cursor.hasNext()) { // Mientras haya elementos en la lista, comparar la nueva CURP con la actual.
                String actual = cursor.next(); // Obtener la CURP actual de la lista.

                String primerasNueva = nueva.substring(0, 4); // Obtener las primeras 4 letras de la nueva CURP.
                String primerasActual = actual.substring(0, 4); // Obtener las primeras 4 letras de la CURP actual.

                if (primerasNueva.compareTo(primerasActual) < 0) { // Comparar las primeras 4 letras de la nueva CURP con las de la actual. Si la nueva es menor, retroceder el cursor y salir del bucle.
                    cursor.previous(); // Retroceder el cursor a la posición anterior.
                    break;
                }
            }

            cursor.add(nueva);

            System.out.println( // Mostrar la CURP insertada y la lista actualizada.
                "Insercion " + (i + 1) + " (" + nueva + "): " + curps
            );
        }
    }
}