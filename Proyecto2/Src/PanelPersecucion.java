//Datos del alumno y proyecto
//Nombre: Gonzalez Martinez Alberto Ezequiel
//Grupo 7CM3
//Proyecto 2 - Sistemas Distribuidos
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Actualiza la simulacion y dibuja los objetos y sus trayectorias. */
public class PanelPersecucion extends JPanel {
    public static final int ANCHO = 1280;
    public static final int ALTO = 720;
    private static final double VELOCIDAD_PRESA = 95.0; // pixeles por segundo
    private static final double RADIO_COLISION = 14.0;
    private static final Color[] COLORES = {
        new Color(239, 83, 80), new Color(255, 183, 77),
        new Color(129, 199, 132), new Color(186, 104, 200),
        new Color(79, 195, 247)
    };

    // Variables de instancia
    private final Agente presa;
    private final List<Agente> perseguidores = new ArrayList<>();
    private final Timer reloj;
    private long tiempoAnterior;
    private double tiempoTotal;
    private boolean terminado;

    /** Crea un panel con una presa y varios perseguidores. */
    public PanelPersecucion(double factor, int cantidad) {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(17, 24, 39));
        Random azar = new Random();
        presa = new Agente(ANCHO / 2.0, ALTO / 2.0, azar.nextDouble() * 2 * Math.PI,
                           VELOCIDAD_PRESA, 4.5, new Color(110, 231, 255));

        // Los puntos de inicio son aleatorios, alejados de la presa.
        for (int i = 0; i < cantidad; i++) {
            double x, y;
            int intentos = 0;
            do {
                x = 80 + azar.nextDouble() * (ANCHO - 160);
                y = 80 + azar.nextDouble() * (ALTO - 160);
                intentos++;
            } while (intentos < 1000 && !inicioValido(x, y));
            perseguidores.add(new Agente(x, y,
                Math.atan2(presa.getY() - y, presa.getX() - x),
                VELOCIDAD_PRESA * factor, 2.1, COLORES[i]));
        }
        reloj = new Timer(16, e -> actualizar());
    }

    /** Verifica que la posición inicial no esté demasiado cerca de la presa ni de otros perseguidores. */
    private boolean inicioValido(double x, double y) {
        if (Math.hypot(x - presa.getX(), y - presa.getY()) < 230) return false;
        for (Agente otro : perseguidores) {
            if (Math.hypot(x - otro.getX(), y - otro.getY()) < 90) return false;
        }
        return true;
    }

    /** Inicia la simulación y el temporizador. */
    public void iniciar() {
        tiempoAnterior = System.nanoTime();
        reloj.start();
    }

    /** Actualiza la posición de la presa y los perseguidores, y verifica colisiones. */
    private void actualizar() {
        if (terminado) return;
        long ahora = System.nanoTime();
        double dt = Math.min((ahora - tiempoAnterior) / 1_000_000_000.0, 0.04);
        tiempoAnterior = ahora;
        tiempoTotal += dt;

        Agente cercano = perseguidores.get(0);
        double minimo = Double.POSITIVE_INFINITY;
        for (Agente p : perseguidores) {
            double d = Math.hypot(p.getX() - presa.getX(), p.getY() - presa.getY());
            if (d < minimo) { minimo = d; cercano = p; }
        }
        double huidaX = presa.getX() - cercano.getX();
        double huidaY = presa.getY() - cercano.getY();
        double magnitud = Math.max(1.0, Math.hypot(huidaX, huidaY));
        // Una oscilacion moderada impide que la presa siga siempre una recta.
        double vx = huidaX / magnitud + 0.22 * Math.cos(tiempoTotal * 0.9);
        double vy = huidaY / magnitud + 0.22 * Math.sin(tiempoTotal * 0.9);
        presa.avanzar(direccionSegura(presa, vx, vy), dt, ANCHO, ALTO);

        for (int i = 0; i < perseguidores.size(); i++) {
            Agente p = perseguidores.get(i);
            double dx = presa.getX() - p.getX();
            double dy = presa.getY() - p.getY();
            p.avanzar(direccionSegura(p, dx, dy), dt, ANCHO, ALTO);
            if (Math.hypot(p.getX() - presa.getX(), p.getY() - presa.getY())
                    <= RADIO_COLISION) {
                terminado = true;
                reloj.stop();
                repaint();
                System.out.printf(Locale.US,
                    "Colision con perseguidor %d en (%.2f, %.2f) pixeles.%n",
                    i + 1, presa.getX(), presa.getY());
                System.out.printf(Locale.US, "Distancia de la presa: %.2f pixeles.%n",
                                  presa.getDistancia());
                for (int j = 0; j < perseguidores.size(); j++) {
                    System.out.printf(Locale.US, "Distancia del perseguidor %d: %.2f pixeles.%n",
                        j + 1, perseguidores.get(j).getDistancia());
                }
                javax.swing.SwingUtilities.invokeLater(() -> System.exit(0));
                return;
            }
        }
        repaint();
    }

    /** Combina la direccion buscada con una fuerza gradual hacia el interior. */
    private double direccionSegura(Agente a, double vx, double vy) {
        double longitud = Math.max(1.0, Math.hypot(vx, vy));
        vx /= longitud;
        vy /= longitud;
        double zona = 175.0;
        vx += fuerzaPared(a.getX() - 12, zona);
        vx -= fuerzaPared(ANCHO - 12 - a.getX(), zona);
        vy += fuerzaPared(a.getY() - 12, zona);
        vy -= fuerzaPared(ALTO - 12 - a.getY(), zona);
        return Math.atan2(vy, vx);
    }

    /** Calcula una fuerza que empuja hacia el interior cuando se acerca a los bordes. */
    private double fuerzaPared(double distancia, double zona) {
        double nivel = Math.max(0.0, (zona - distancia) / zona);
        return 5.0 * nivel * nivel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.6f));
        dibujarTrayectoria(g2, presa);
        for (Agente p : perseguidores) dibujarTrayectoria(g2, p);
        dibujarAgente(g2, presa);
        for (Agente p : perseguidores) dibujarAgente(g2, p);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2.drawString(String.format(Locale.US, "Tiempo: %.1f s  |  Perseguidores: %d",
                      tiempoTotal, perseguidores.size()), 20, 28);
        g2.dispose();
    }

    private void dibujarTrayectoria(Graphics2D g2, Agente a) {
        Color c = a.getColor();
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 170));
        g2.draw(a.getTrayectoria());
    }

    private void dibujarAgente(Graphics2D g2, Agente a) {
        AffineTransform anterior = g2.getTransform();
        g2.translate(a.getX(), a.getY());
        g2.rotate(a.getAngulo());
        g2.setColor(a.getColor());
        g2.fillRect(-9, -6, 18, 12);
        g2.setColor(Color.WHITE);
        g2.drawRect(-9, -6, 18, 12);
        g2.setTransform(anterior);
    }
}
