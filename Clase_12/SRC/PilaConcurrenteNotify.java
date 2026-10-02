import java.util.concurrent.ThreadLocalRandom;
// Clase que implementa una pila concurrente con tres hilos: productor, consumidor y visor.
public class PilaConcurrente {
    private static final char[] pila = new char[10];
    // -1 significa pila vacia; 9 significa pila llena.
    private static int tope = -1; 
    private boolean pendiente = false; 
    private boolean terminado = false; 
    private int cambio = 0; 
    private String ultimaOperacion = "Inicio: pila vacia";
    private enum Operacion { PRODUCIR, CONSUMIR, MOSTRAR, TERMINAR }

    // Los tres hilos usan este metodo SOBRE EL MISMO OBJETO.
    private synchronized boolean acceder(Operacion op, char elemento) // Se sincroniza para que solo un hilo pueda acceder a la pila a la vez.
            throws InterruptedException { // Se lanza InterruptedException para manejar interrupciones de hilos.
        if (op == Operacion.PRODUCIR) { // Si la operacion es producir, el hilo productor intenta agregar un elemento a la pila.
            while (tope == pila.length - 1 || pendiente) wait(); // Si la pila esta llena o hay un cambio pendiente, el hilo productor espera.
            pila[++tope] = elemento;
            ultimaOperacion = "Productor introduce: " + elemento;
            cambio++;
            pendiente = true;
            notifyAll(); // Notifica a todos los hilos que estan esperando que la pila ha cambiado.
        } else if (op == Operacion.CONSUMIR) { // Si la operacion es consumir, el hilo consumidor intenta retirar un elemento de la pila.
            while (tope == -1 || pendiente) wait(); // Si la pila esta vacia o hay un cambio pendiente, el hilo consumidor espera.
            char retirado = pila[tope];
            pila[tope--] = '\0'; 
            ultimaOperacion = "Consumidor retira: " + retirado;
            cambio++;
            pendiente = true;
            notifyAll(); 
        } else if (op == Operacion.MOSTRAR) { // Si la operacion es mostrar, el hilo visor intenta mostrar el estado de la pila.
            while (!pendiente && !terminado) wait(); // Si no hay cambios pendientes y no se ha terminado, el hilo visor espera.
            if (!pendiente && terminado) return false; 
            // ANSI: limpia la pantalla y coloca el cursor en el inicio.
            System.out.print("\033[H\033[2J"); 
            System.out.println("PILA CONCURRENTE - capacidad: 10");
            System.out.println("Cambio: " + cambio + " | " + ultimaOperacion);
            System.out.println("Tope: " + tope + " | Elementos: " + (tope + 1));
            for (int i = pila.length - 1; i >= 0; i--) {
                String contenido = pila[i] == '\0' ? " " : String.valueOf(pila[i]);
                System.out.printf("[%d] | %s |%s%n", i, contenido,
                        i == tope ? " <-- TOPE" : "");
            }
            System.out.flush();
            pendiente = false;
            notifyAll();
        } else {
            terminado = true;
            notifyAll();
        }
        return true;
    }
    // Metodo que simula una pausa aleatoria entre 1 y maximo milisegundos.
    private static void pausa(int maximo) throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(1, maximo + 1));
    }
    // Metodo main que crea y ejecuta los tres hilos.
    public static void main(String[] args) throws InterruptedException {
        if (args.length > 3) throw new IllegalArgumentException(
                "Uso: java PilaConcurrente [cantidad [tpMax [tcMax]]]");
        final int cantidad = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        final int tpMax = args.length > 1 ? Integer.parseInt(args[1]) : 500;
        final int tcMax = args.length > 2 ? Integer.parseInt(args[2]) : 900;
        if (cantidad < 1 || tpMax < 1 || tcMax < 1)
            throw new IllegalArgumentException("Los parametros deben ser positivos");
        PilaConcurrente compartida = new PilaConcurrente(); // Se crea un objeto compartido entre los tres hilos.
        Thread productor = new Thread(() -> { // Hilo productor que produce elementos y los agrega a la pila.
            try {
                for (int i = 0; i < cantidad; i++) {
                    pausa(tpMax);
                    compartida.acceder(Operacion.PRODUCIR, (char) ('A' + i % 26));
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "productor");
        Thread consumidor = new Thread(() -> { // Hilo consumidor que retira elementos de la pila.
            try {
                for (int i = 0; i < cantidad; i++) {
                    pausa(tcMax);
                    compartida.acceder(Operacion.CONSUMIR, '\0');  // El caracter '\0' indica que no se necesita un elemento para consumir.
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "consumidor");
        Thread visor = new Thread(() -> { // Hilo visor que muestra el estado de la pila.
            try {
                while (compartida.acceder(Operacion.MOSTRAR, '\0')) { } 
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "visor");
        // Se muestra el estado inicial antes de permitir modificaciones.
        compartida.pendiente = true;
        visor.start(); productor.start(); consumidor.start(); // Se inician los tres hilos.
        productor.join(); consumidor.join(); // Se espera a que los hilos productor y consumidor terminen.
        compartida.acceder(Operacion.TERMINAR, '\0'); 
        visor.join();
    }
}
