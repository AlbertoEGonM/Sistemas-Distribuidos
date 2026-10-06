import java.util.ArrayList;
// Genera listas de CURPS, las imprime y las ordena mediante el uso de pool de hilos. Se puede cambiar el numero de hilos y el metodo de ordenamiento.
public class Ejercicio2 {
    // Genera listas de CURPs, las imprime y las ordena mediante el uso de pool de hilos. Se puede cambiar el numero de hilos y el metodo de ordenamiento.
    public static void main(String[] args) throws Exception {
        if (args.length != 2) { // Se esperan los argumentos n y m.
            System.out.println("Uso: java Ejercicio2 <n CURPs por lista> <m listas>");
            return;
        }
        try {
            // Convierte los argumentos a enteros positivos y genera las listas.
            int n = UtilidadesCURP.positivo(args[0]);
            int m = UtilidadesCURP.positivo(args[1]);
            // Genera m listas y n curps por lista, imprime las listas originales y luego las ordena e imprime usando un pool de hilos.
            ArrayList<ArrayList<String>> listas = UtilidadesCURP.generarListas(n, m);
            UtilidadesCURP.imprimirOriginales(listas); // el .imprimirOriginales imprime las listas originales 
            UtilidadesCURP.ejecutarPool(listas, 2, true, false, false); // el .ejecutarPool ordena e imprime las listas usando un pool de hilos
        } catch (IllegalArgumentException e) {
            System.err.println("Error: usa numeros enteros positivos.");
            System.exit(1);
        }
    }
}
