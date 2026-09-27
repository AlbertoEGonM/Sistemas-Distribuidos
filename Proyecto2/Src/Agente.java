//Datos del alumno y proyecto
//Nombre: Gonzalez Martinez Alberto Ezequiel
//Grupo 7CM3
//Proyecto 2 - Sistemas Distribuidos
import java.awt.Color;
import java.awt.geom.Path2D;

/** Posicion, movimiento con giro limitado y trayectoria de un objeto. */
public class Agente {
    private double x, y, angulo, distancia;
    private final double velocidad, giroMaximo;
    private final Color color;
    private final Path2D.Double trayectoria = new Path2D.Double();

    public Agente(double x, double y, double angulo, double velocidad,
                  double giroMaximo, Color color) {
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.velocidad = velocidad;
        this.giroMaximo = giroMaximo;
        this.color = color;
        trayectoria.moveTo(x, y);
    }

    /** Avanza el agente hacia la dirección deseada durante un tiempo dado, respetando el giro máximo y los límites del área. */
    public void avanzar(double direccionDeseada, double segundos, int ancho, int alto) {
        double diferencia = Math.atan2(Math.sin(direccionDeseada - angulo),
                                      Math.cos(direccionDeseada - angulo));
        double giro = giroMaximo * segundos;
        angulo += Math.max(-giro, Math.min(giro, diferencia));

        // El margen coincide con la mitad de la diagonal del rectangulo.
        double margen = 12.0;
        double nuevoX = Math.max(margen, Math.min(ancho - margen,
                         x + Math.cos(angulo) * velocidad * segundos));
        double nuevoY = Math.max(margen, Math.min(alto - margen,
                         y + Math.sin(angulo) * velocidad * segundos));
        distancia += Math.hypot(nuevoX - x, nuevoY - y);
        x = nuevoX;
        y = nuevoY;
        trayectoria.lineTo(x, y);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngulo() { return angulo; }
    public double getDistancia() { return distancia; }
    public Color getColor() { return color; }
    public Path2D.Double getTrayectoria() { return trayectoria; }
}
