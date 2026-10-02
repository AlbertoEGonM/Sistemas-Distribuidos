public class CarreraSincronizada implements Runnable { // Clase que implementa una carrera de hilos sincronizados
    static int variable_compartida = 0;
    private final int n; 
    public CarreraSincronizada(int n) { this.n = n; } // Constructor que inicializa la cantidad de iteraciones que cada hilo realizará.
    public synchronized void modifica() { // Método sincronizado que modifica la variable compartida.
        String id = Thread.currentThread().getName(); // Obtiene el nombre del hilo actual.
        if (id.equals("hilo1")) variable_compartida++; // Si el hilo es "hilo1", incrementa la variable compartida.
        else if (id.equals("hilo2")) variable_compartida--; // Si el hilo es "hilo2", decrementa la variable compartida.
    }
    @Override public void run() {
        for (int i = 0; i < n; i++) modifica();
    }
    public static void main(String[] args) throws InterruptedException { // Método main que crea y ejecuta los hilos.
        if (args.length != 1) throw new IllegalArgumentException("Uso: java CarreraSincronizada n");
        int n = Integer.parseInt(args[0]); // Convierte el argumento a un entero.
        if (n < 1) throw new IllegalArgumentException("n debe ser positivo");
        CarreraSincronizada tarea = new CarreraSincronizada(n);
        Thread hilo1 = new Thread(tarea, "hilo1");
        Thread hilo2 = new Thread(tarea, "hilo2");
        hilo1.start(); hilo2.start();
        hilo1.join(); hilo2.join();
        System.out.println(variable_compartida);
    }
}
