import java.util.concurrent.ThreadLocalRandom;
// Clase que coordina una pila compartida entre productor, consumidor y visor.
public class PilaConcurrente {
    private static final char[] pila = new char[10];
    // -1 indica que la pila esta vacia; 9 indica que esta llena.
    private static int tope = -1; 
    private boolean pendiente = true; 
    private boolean terminado = false;
    private int cambio = 0;
    private String ultimaOperacion = "Inicio: pila vacia";
    private enum Operacion { PRODUCIR, CONSUMIR, MOSTRAR, TERMINAR }

    // Los tres hilos usan el mismo objeto y synchronized protege el estado compartido.
    private synchronized boolean acceder(Operacion op, char elemento) {
        if (op == Operacion.PRODUCIR) { // El productor intenta agregar un elemento.
            // Devuelve false para que el hilo reintente si la pila esta llena o falta mostrar un cambio.
            if (tope == pila.length - 1 || pendiente) {
                return false;
            }
            pila[++tope] = elemento;
            ultimaOperacion = "Productor introduce: " + elemento;
            cambio++;
            pendiente = true;
            return true;
        } else if (op == Operacion.CONSUMIR) { // El consumidor intenta retirar el elemento del tope.
            // Devuelve false para reintentar si esta vacia o el visor aun no mostro el ultimo cambio.
            if (tope == -1 || pendiente) {
                return false;
            }
            char retirado = pila[tope];
            pila[tope--] = '\0';
            ultimaOperacion = "Consumidor retira: " + retirado;
            cambio++;
            pendiente = true;
            return true;
        } else if (op == Operacion.MOSTRAR) { // El visor imprime el estado solo cuando hay un cambio pendiente.
            if (pendiente) {
                // ANSI: limpia la pantalla y coloca el cursor al inicio.
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
                pendiente = false; // El productor o consumidor ya puede intentar el siguiente cambio.
            }
            return !terminado; // El visor sigue activo hasta que productor y consumidor terminan.
        } else { // TERMINAR indica al visor que finalice despues de mostrar el estado pendiente.
            terminado = true;
            return true;
        }
    }

    // main valida parametros, crea los hilos y espera su finalizacion.
    public static void main(String[] args) throws InterruptedException {
        if (args.length > 3) throw new IllegalArgumentException(
                "Uso: java PilaConcurrente [cantidad [tpMax [tcMax]]]");
        final int cantidad = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        final int tpMax = args.length > 1 ? Integer.parseInt(args[1]) : 500;
        final int tcMax = args.length > 2 ? Integer.parseInt(args[2]) : 900;
        if (cantidad < 1 || tpMax < 1 || tcMax < 1
                || tpMax == Integer.MAX_VALUE || tcMax == Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                    "Cantidad debe ser positiva y los tiempos deben estar entre 1 y 2147483646");
        PilaConcurrente compartida = new PilaConcurrente(); // Objeto compartido por los tres hilos.
        Thread productor = new Thread(() -> { // Genera elementos y reintenta si no puede agregarlos.
            try {
                for (int i = 0; i < cantidad; i++) {
                    Thread.sleep(
                            ThreadLocalRandom.current()
                                    .nextInt(1, tpMax + 1)
                    );
                    char elemento = (char) ('A' + i % 26);
                    while (!compartida.acceder(
                            Operacion.PRODUCIR, elemento)) {
                        Thread.sleep(10); // Pausa breve antes de volver a intentar.
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "productor");
        Thread consumidor = new Thread(() -> { // Retira la misma cantidad de elementos que produce el productor.
            try {
                for (int i = 0; i < cantidad; i++) {
                    Thread.sleep(
                            ThreadLocalRandom.current()
                                    .nextInt(1, tcMax + 1)
                    );
                    while (!compartida.acceder(
                            Operacion.CONSUMIR, '\0')) {
                        Thread.sleep(10); // Evita una espera activa continua.
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "consumidor");
        Thread visor = new Thread(() -> { // Muestra cada cambio realizado en la pila.
            try {
                while (compartida.acceder(
                        Operacion.MOSTRAR, '\0')) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "visor");
        visor.start(); productor.start(); consumidor.start(); // Se inician los tres hilos.
        productor.join(); consumidor.join(); // Se espera a que productor y consumidor terminen.
        compartida.acceder(Operacion.TERMINAR, '\0');
        visor.join();
    }
}