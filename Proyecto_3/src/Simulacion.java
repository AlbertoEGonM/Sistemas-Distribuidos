// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** El monitor protege la lista compartida entre HTTP y el hilo de Swing. */
public final class Simulacion {
    public static final int ANCHO = 1000, ALTO = 1000;
    public static final int MAX_LOTE = 10000;
    private final List<Asteroide> asteroides = new ArrayList<>();
    private final FabricaAsteroides fabrica = new FabricaAsteroides();

    public synchronized int agregar(double v, double dx, double dy, double giro) {
        asteroides.add(fabrica.crear(v, dx, dy, giro));
        return asteroides.size();
    }

    public synchronized int agregarAleatorios(int n) {
        if (n < 1 || n > MAX_LOTE)
            throw new IllegalArgumentException("Cantidad: entre 1 y " + MAX_LOTE + ".");
        for (int i = 0; i < n; i++) asteroides.add(fabrica.crearAleatorio());
        return asteroides.size();
    }

    public synchronized int total() { return asteroides.size(); }

    public synchronized void avanzar(double segundos) {
        if (!Double.isFinite(segundos) || segundos < 0)
            throw new IllegalArgumentException("Tiempo invalido.");
        for (Asteroide a : asteroides) a.avanzar(segundos);
    }

    public synchronized void dibujar(Graphics2D g) {
        for (Asteroide a : asteroides) a.dibujar(g);
    }
}
