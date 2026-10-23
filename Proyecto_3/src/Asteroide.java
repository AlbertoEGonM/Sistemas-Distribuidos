// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/** Poligono local inmutable y movimiento calculado con tiempo real (segundos). */
public final class Asteroide {
    private final Path2D.Double forma;
    private final double radio, vx, vy, omega;
    private final String firma;
    private double x, y, angulo;
    private double tiempoHastaEntrada;
    private boolean enEspacio;

    Asteroide(Path2D.Double forma, String firma, double radio, double x, double y,
              double vx, double vy, double omega, double tiempoHastaEntrada) {
        this.forma = forma;
        this.firma = firma;
        this.radio = radio;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.omega = omega;
        this.tiempoHastaEntrada = tiempoHastaEntrada;
    }

    public void avanzar(double segundos) {
        x += vx * segundos;
        y += vy * segundos;
        angulo = modulo(angulo + omega * segundos, Math.PI * 2);
        // Antes de la primera entrada no dibujamos copias en el borde opuesto.
        if (!enEspacio) {
            tiempoHastaEntrada -= segundos;
            if (tiempoHastaEntrada <= 0) enEspacio = true;
        }
        if (enEspacio) {
            x = modulo(x, Simulacion.ANCHO);
            y = modulo(y, Simulacion.ALTO);
        }
    }

    static double modulo(double valor, double periodo) {
        return ((valor % periodo) + periodo) % periodo;
    }

    public void dibujar(Graphics2D g) {
        Shape poligono = new AffineTransform(Math.cos(angulo), Math.sin(angulo),
                -Math.sin(angulo), Math.cos(angulo), x, y).createTransformedShape(forma);
        g.draw(poligono);
        if (!enEspacio) return;
        // Las copias parciales producen continuidad incluso en las esquinas.
        int desdeX = x + radio >= Simulacion.ANCHO ? -1 : 0;
        int hastaX = x - radio <= 0 ? 1 : 0;
        int desdeY = y + radio >= Simulacion.ALTO ? -1 : 0;
        int hastaY = y - radio <= 0 ? 1 : 0;
        for (int i = desdeX; i <= hastaX; i++) {
            for (int j = desdeY; j <= hastaY; j++) {
                if (i != 0 || j != 0) {
                    g.draw(AffineTransform.getTranslateInstance(i * Simulacion.ANCHO,
                            j * Simulacion.ALTO).createTransformedShape(poligono));
                }
            }
        }
    }

    // Acceso de solo lectura para las pruebas del modelo.
    double getX() { return x; }
    double getY() { return y; }
    double getVx() { return vx; }
    double getVy() { return vy; }
    double getAngulo() { return angulo; }
    double getOmega() { return omega; }
    double getRadio() { return radio; }
    String getFirma() { return firma; }
    boolean estaEnEspacio() { return enEspacio; }
}
