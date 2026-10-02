public class EjemploRunnable implements Runnable {

    @Override
    // Metodo que se ejecuta cuando se inicia el hilo.
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(
                Thread.currentThread().getName()
                + " ejecuta la vuelta " + i
            );
        }
    }

    public static void main(String[] args)
            throws InterruptedException { // Metodo main que crea y ejecuta los hilos.

        // Trabajo que ejecutarán ambos hilos.
        EjemploRunnable tarea = new EjemploRunnable();

        // Creamos dos hilos y les asignamos nombres.
        Thread hilo1 = new Thread(tarea, "hilo1");
        Thread hilo2 = new Thread(tarea, "hilo2");

        // Iniciamos ambos hilos.
        hilo1.start();
        hilo2.start();

        // Esperamos a que los dos terminen.
        hilo1.join();
        hilo2.join();

        System.out.println("Los dos hilos terminaron.");
    }
}