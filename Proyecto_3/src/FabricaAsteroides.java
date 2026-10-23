// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Genera formas estrelladas simples, sin cruces, con siluetas no repetidas. */
public final class FabricaAsteroides {
    private final Random azar;
    private final Set<String> firmas = new HashSet<>();

    public FabricaAsteroides() { this(new Random()); }
    FabricaAsteroides(Random azar) { this.azar = azar; }

    public Asteroide crear(double rapidez, double dx, double dy, double grados) {
        validar(rapidez, dx, dy, grados);
        // Escalar antes de normalizar evita desbordar con componentes grandes.
        double escala = Math.max(Math.abs(dx), Math.abs(dy));
        double norma = Math.hypot(dx / escala, dy / escala);
        double vx = rapidez * (dx / escala) / norma;
        double vy = rapidez * (dy / escala) / norma;
        int vertices;
        double[] radios;
        String firma;
        do {
            vertices = 9 + azar.nextInt(8);
            radios = new double[vertices];
            radios[0] = 1;
            StringBuilder clave = new StringBuilder(vertices + ":");
            for (int i = 1; i < vertices; i++) {
                radios[i] = 0.50 + azar.nextInt(450001) / 1000000.0;
                clave.append(Double.toHexString(radios[i])).append(';');
            }
            firma = clave.toString();
        } while (!firmas.add(firma));
        // El vertice de radio maximo es unico: fija la orientacion de la firma.
        double radio = 12 + azar.nextDouble() * 22;
        Path2D.Double forma = new Path2D.Double();
        for (int i = 0; i < vertices; i++) {
            double a = 2 * Math.PI * i / vertices;
            double px = radio * radios[i] * Math.cos(a);
            double py = radio * radios[i] * Math.sin(a);
            if (i == 0) forma.moveTo(px, py); else forma.lineTo(px, py);
        }
        forma.closePath();

        // Escoger un borde por el que el vector apunte hacia adentro.
        // La ponderacion evita entradas casi tangentes excesivamente lentas.
        double x, y, tiempo;
        double margen = radio + 1;
        if (azar.nextDouble() * (Math.abs(vx) + Math.abs(vy)) < Math.abs(vx)) {
            tiempo = margen / Math.abs(vx);
            x = vx > 0 ? -margen : Simulacion.ANCHO + margen;
            y = radio + azar.nextDouble() * (Simulacion.ALTO - 2 * radio) - vy * tiempo;
        } else {
            tiempo = margen / Math.abs(vy);
            y = vy > 0 ? -margen : Simulacion.ALTO + margen;
            x = radio + azar.nextDouble() * (Simulacion.ANCHO - 2 * radio) - vx * tiempo;
        }
        return new Asteroide(forma, firma, radio, x, y, vx, vy, Math.toRadians(grados), tiempo);
    }

    public Asteroide crearAleatorio() {
        double direccion = azar.nextDouble() * 2 * Math.PI;
        double giro = 15 + azar.nextDouble() * 165;
        return crear(40 + azar.nextDouble() * 140, Math.cos(direccion),
                Math.sin(direccion), azar.nextBoolean() ? giro : -giro);
    }

    public static void validar(double rapidez, double dx, double dy, double grados) {
        if (!Double.isFinite(rapidez) || rapidez <= 0 || rapidez > 5000)
            throw new IllegalArgumentException("Rapidez: mayor que 0 y hasta 5000 px/s.");
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || (dx == 0 && dy == 0))
            throw new IllegalArgumentException("Direccion: dos numeros finitos, distintos de (0,0).");
        if (!Double.isFinite(grados) || Math.abs(grados) > 3600)
            throw new IllegalArgumentException("Giro: entre -3600 y 3600 grados/s.");
    }
}
