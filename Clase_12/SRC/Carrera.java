public class Carrera implements Runnable {
    // Variable compartida entre los hilos.
    static int variable_compartida = 0;
    private final int n;
    public Carrera(int n) { this.n = n; } // Constructor que inicializa la cantidad de iteraciones que cada hilo realizará.
    // Metodo que modifica la variable compartida.
    public static void modifica() {
        String id = Thread.currentThread().getName(); // Obtiene el nombre del hilo actual.
        if (id.equals("hilo1")) variable_compartida++; // Si el hilo es "hilo1", incrementa la variable compartida.
        else if (id.equals("hilo2")) variable_compartida--; // Si el hilo es "hilo2", decrementa la variable compartida.
    }
    @Override public void run() {
        for (int i = 0; i < n; i++) modifica();
    }
    // Metodo main que crea y ejecuta los hilos.
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) throw new IllegalArgumentException("Uso: java Carrera n"); // Verifica que se haya proporcionado un argumento.
        int n = Integer.parseInt(args[0]); // Convierte el argumento a un entero.
        if (n < 1) throw new IllegalArgumentException("n debe ser positivo"); // Verifica que n sea positivo.
        Carrera tarea = new Carrera(n); // Crea una instancia de la clase Carrera con el valor de n.
        Thread hilo1 = new Thread(tarea, "hilo1");
        Thread hilo2 = new Thread(tarea, "hilo2");
        hilo1.start(); hilo2.start();
        hilo1.join(); hilo2.join();
        System.out.println(variable_compartida);
    }
}
