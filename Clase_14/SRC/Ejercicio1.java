import java.util.ArrayList;

public class Ejercicio1 {
    // Genera listas de CURPs, las imprime y las ordena.
    public static void main(String[] args) {
        if (args.length != 2) { // Se esperan dos argumentos: n y m
            System.out.println("Uso: java Ejercicio1 <n CURPs por lista> <m listas>");
            return;
        }
        // Convierte los argumentos a enteros positivos y genera las listas.
        try {
            int n = UtilidadesCURP.positivo(args[0]); // n CURPs por lista
            int m = UtilidadesCURP.positivo(args[1]); // m listas
            ArrayList<ArrayList<String>> listas = UtilidadesCURP.generarListas(n, m); // Genera m listas de n CURPs cada una
            UtilidadesCURP.imprimirOriginales(listas); // Imprime las listas originales
            for (int i = 0; i < listas.size(); i++) {// Itera sobre cada lista y la ordena e imprime
                UtilidadesCURP.ordenarEImprimir(listas.get(i), i + 1); // Ordena e imprime la lista i + 1
            }
            // Ordena e imprime la lista i + 1
        } catch (IllegalArgumentException e) {
            System.err.println("Error: usa numeros enteros positivos.");
            System.exit(1);
        }
    }
}
