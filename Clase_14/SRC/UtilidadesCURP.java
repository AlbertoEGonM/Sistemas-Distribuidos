import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
// Clase de utilidades para los ejercicios 1, 2 y 3, esto se usa para separar la logica de generacion y ordenamiento de CURPs de la logica de los ejercicios.
// Se genera un nuevo archivo para reutilizar los metodos y no repetir codigo en cada ejercicio. Esto permite mantener el codigo mas limpio y organizado.
public class UtilidadesCURP {
    // Convierte un texto a entero positivo; lanza IllegalArgumentException si no es valido.
    public static int positivo(String texto) {
        int numero = Integer.parseInt(texto);
        if (numero <= 0) {
            throw new IllegalArgumentException("Los numeros deben ser positivos.");
        }
        return numero;
    }
z
    // Genera m listas de n CURPs cada una, usando GeneradorCURP.
    public static ArrayList<ArrayList<String>> generarListas(int n, int m) {
        ArrayList<ArrayList<String>> listas = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            ArrayList<String> lista = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
                lista.add(GeneradorCURP.generar());
            }
            listas.add(lista);
        }
        return listas;
    }

    // Ordenamiento por insercion de la clase 11, sobre una lista temporal.
    public static ArrayList<String> ordenar(ArrayList<String> original) {
        ArrayList<String> temporal = new ArrayList<>(original.size());
        for (String nueva : original) {
            ListIterator<String> cursor = temporal.listIterator();
            while (cursor.hasNext()) {
                String actual = cursor.next();
                if (nueva.substring(0, 4).compareTo(actual.substring(0, 4)) < 0) {
                    cursor.previous();
                    break;
                }
            }
            cursor.add(nueva);
        }
        return temporal;
    }

    // El metodo pedido: recibe una lista, la ordena temporalmente y la imprime.
    public static void ordenarEImprimir(ArrayList<String> original, int numero) {
        ArrayList<String> temporal = ordenar(original);
        // Un println por tarea evita mezclar los caracteres de dos listas.
        System.out.println("Lista " + numero + " ordenada por "
                + Thread.currentThread().getName() + ": " + temporal);
    }

    public static void imprimirOriginales(ArrayList<ArrayList<String>> listas) {
        for (int i = 0; i < listas.size(); i++) {
            System.out.println("Lista " + (i + 1) + " original: " + listas.get(i));
        }
    }

    // Opcional: mismo criterio de comparacion, algoritmo mas rapido.
    public static ArrayList<String> ordenarRapido(ArrayList<String> original) {
        ArrayList<String> temporal = new ArrayList<>(original);
        temporal.sort(Comparator.comparing(curp -> curp.substring(0, 4)));
        return temporal;
    }

    public static void comprobar(ArrayList<String> lista) {
        for (int i = 1; i < lista.size(); i++) {
            if (lista.get(i - 1).substring(0, 4)
                    .compareTo(lista.get(i).substring(0, 4)) > 0) {
                throw new IllegalStateException("La lista no esta ordenada.");
            }
        }
    }

    // Una tarea por lista. Es concurrente, pero no paralela: cada tarea ordena e imprime su lista.
    // Se puede cambiar el numero de hilos y el metodo de ordenamiento.
    public static void ejecutarPool(ArrayList<ArrayList<String>> listas,
            int hilos, boolean imprimir, boolean rapido, boolean verificar)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        ArrayList<Future<?>> tareas = new ArrayList<>();
        try {
            for (int i = 0; i < listas.size(); i++) {
                final int numero = i + 1;
                final ArrayList<String> lista = listas.get(i);
                tareas.add(pool.submit(() -> {
                    if (imprimir) {
                        ordenarEImprimir(lista, numero);
                    } else {
                        ArrayList<String> temporal = rapido
                                ? ordenarRapido(lista) : ordenar(lista);
                        if (verificar) {
                            comprobar(temporal);
                            if (temporal.size() != lista.size()) {
                                throw new IllegalStateException("Faltan elementos.");
                            }
                        }
                    }
                }));
            }
            pool.shutdown();
            // get espera y comunica cualquier error ocurrido dentro de la tarea.
            for (Future<?> tarea : tareas) {
                tarea.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
