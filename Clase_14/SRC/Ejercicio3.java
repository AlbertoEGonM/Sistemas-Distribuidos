import java.util.ArrayList;
import java.util.Locale;

public class Ejercicio3 {
    public static void main(String[] args) throws Exception {
        // Modo especial para scripts: si se ejecuta con el argumento "cpu",
        // devuelve la cantidad de núcleos disponibles del sistema.
        // Esto sirve para que otros scripts puedan decidir cuántos hilos usar.
        if (args.length == 1 && args[0].equals("cpu")) {
            System.out.println(Runtime.getRuntime().availableProcessors());
            return;
        }

        // El programa acepta entre 1 y 5 argumentos:
        // 1) hilos -> número de threads a usar
        // 2) metodo -> "insercion" o "rapido" (opcional)
        // 3) n -> tamaño de la entrada (opcional)
        // 4) m -> cantidad de listas o elementos a generar (opcional)
        // 5) verificar -> palabra clave para activar la validación (opcional)
        // Si se pasan menos o más argumentos de los permitidos, se muestra el uso correcto.
        if (args.length < 1 || args.length > 5) {
            System.out.println("Uso: java Ejercicio3 <hilos> [insercion|rapido] [n] [m] [verificar]");
            return;
        }

        try {
            // Convierte el primer argumento en un entero positivo.
            // Si no es válido, la utilidad lanza IllegalArgumentException.
            int hilos = UtilidadesCURP.positivo(args[0]);

            // Si no se indica método, se usa por defecto "insercion".
            String metodo = args.length >= 2 ? args[1] : "insercion";

            // Validación del algoritmo solicitado: solo acepta dos opciones.
            if (!metodo.equals("insercion") && !metodo.equals("rapido")) {
                throw new IllegalArgumentException("Metodo desconocido.");
            }

            // Si se pasa n, se toma ese valor; de lo contrario usa 50000 como valor base.
            int n = args.length >= 3 ? UtilidadesCURP.positivo(args[2]) : 50000;

            // Si se pasa m, se toma ese valor; de lo contrario usa 500 como valor base.
            int m = args.length >= 4 ? UtilidadesCURP.positivo(args[3]) : 500;

            // La bandera verificar solo se activa si el quinto argumento es exactamente "verificar".
            boolean verificar = args.length == 5;
            if (verificar && !args[4].equals("verificar")) {
                throw new IllegalArgumentException("Opcion desconocida.");
            }

            // Genera las listas de cadenas que serán procesadas por el pool de hilos.
            // Se usa la misma semilla para que cada proceso genere los mismos datos,
            // permitiendo comparar resultados de manera reproducible.
            ArrayList<ArrayList<String>> listas = UtilidadesCURP.generarListas(n, m);

            // Guarda el tiempo antes de ejecutar el algoritmo paralelo.
            long inicio = System.nanoTime();

            // Ejecuta la tarea sobre un pool de hilos, indicando:
            // - la colección de listas a procesar
            // - la cantidad de hilos
            // - si se debe usar un comportamiento especial (false aquí)
            // - si se ejecuta la variante rápida
            // - si se activa la verificación de resultados.
            UtilidadesCURP.ejecutarPool(listas, hilos, false,
                    metodo.equals("rapido"), verificar);

            // Calcula el tiempo transcurrido en milisegundos para reportarlo.
            double milisegundos = (System.nanoTime() - inicio) / 1_000_000.0;

            // Imprime un resumen con el número de hilos y el tiempo total de ejecución.
            // El formato "d,%.3f" genera una salida tipo: 8,123.456
            // y sirve para que scripts externos procesen el resultado fácilmente.
            System.out.printf(Locale.ROOT, "%d,%.3f%n", hilos, milisegundos);
        } catch (IllegalArgumentException e) {
            // Cualquier dato inválido en los argumentos se reporta en stderr y
            // termina la ejecución con código de error 1.
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
