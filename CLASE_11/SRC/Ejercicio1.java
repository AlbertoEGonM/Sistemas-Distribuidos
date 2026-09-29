import java.util.ArrayList;
import java.util.Iterator;

public class Ejercicio1 { //Actividad Clase 11 - Programadores y On-line
    public static void main(String[] args) { //El segundo argumento de Ejercicio1 indica el sexo que se ELIMINA.
        if (args.length != 2) {
            System.out.println("Uso: java Ejercicio1 <cantidad> <H|M>"); // Se espera un argumento: la cantidad de CURPs a generar y el sexo a eliminar.
            return;
        }

        int n;
        // Validar que el argumento sea un número entero no negativo.
        try {
            n = Integer.parseInt(args[0]); // Convertir el argumento a un número entero.
        } catch (NumberFormatException e) {
            System.out.println("La cantidad debe ser un numero entero."); // Mostrar mensaje de error si el argumento no es un número entero.
            return;
        }

        if (n < 0) {
            System.out.println("La cantidad no puede ser negativa."); // Mostrar mensaje de error si la cantidad es negativa.
            return;
        }

        String sexo = args[1].toUpperCase(); // Convertir el argumento del sexo a mayúsculas para evitar problemas de comparación.

        if (!sexo.equals("H") && !sexo.equals("M")) { // Validar que el argumento del sexo sea H o M.
            System.out.println("El sexo debe ser H o M."); // Mostrar mensaje de error si el argumento del sexo no es H o M.
            return;
        }

        // Crear la lista y generar las CURP.
        ArrayList<String> curps = new ArrayList<>();
        // Generar n CURPs y agregarlas a la lista.
        for (int i = 0; i < n; i++) {
            curps.add(GeneradorCURP.generar());
        }

        // Mostrar la lista ANTES de eliminar.
        System.out.println("CURPs originales:");
        for (String curp : curps) {
            System.out.println(curp);
        }

        // Recorrer y eliminar mediante Iterator.
        Iterator<String> cursor = curps.iterator();

        while (cursor.hasNext()) {
            String curp = cursor.next();

            // El sexo está en la posición 10, contando desde cero.
            if (curp.charAt(10) == sexo.charAt(0)) {
                cursor.remove();
            }
        }

        // Mostrar la lista DESPUÉS de eliminar.
        System.out.println("\nCURPs restantes tras eliminar el sexo " + sexo + ":");
        for (String curp : curps) {
            System.out.println(curp);
        }
    }
}