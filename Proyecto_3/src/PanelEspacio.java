// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Solo Swing y AWT para graficar. El negro ocupa exactamente 1000x1000. */
public final class PanelEspacio extends JPanel {
    private final Simulacion simulacion;
    private final Timer reloj;
    private long anterior;

    public PanelEspacio(Simulacion simulacion) {
        this.simulacion = simulacion;
        setPreferredSize(new Dimension(Simulacion.ANCHO, Simulacion.ALTO));
        setBackground(Color.BLACK);
        reloj = new Timer(16, evento -> {
            long ahora = System.nanoTime();
            simulacion.avanzar((ahora - anterior) / 1_000_000_000.0);
            anterior = ahora;
            repaint();
        });
        reloj.setCoalesce(true);
    }

    public void iniciar() { anterior = System.nanoTime(); reloj.start(); }
    public void detener() { reloj.stop(); }

    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        try {
            g.setColor(Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            simulacion.dibujar(g);
        } finally { g.dispose(); }
    }
}
