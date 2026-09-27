//Datos del alumno y proyecto
//Nombre: Gonzalez Martinez Alberto Ezequiel
//Grupo 7CM3
//Proyecto 2 - Sistemas Distribuidos
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Ejecutar: java Proyecto2 <factor_de_velocidad> <numero_de_perseguidores> */
public class Proyecto2 {
    public static void main(String[] args) {
        if (args.length != 2) {
            uso();
            return;
        }
        final double factor;
        final int cantidad;
        try {
            factor = Double.parseDouble(args[0]);
            cantidad = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            uso();
            return;
        }
        if (!Double.isFinite(factor) || factor <= 1.0 || cantidad < 1 || cantidad > 5) {
            uso();
            return;
        }

        // Ejecutar la simulación en el hilo de despacho de eventos de Swing.
        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Proyecto 2 - Persecucion");
            PanelPersecucion panel = new PanelPersecucion(factor, cantidad);
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setContentPane(panel);
            ventana.pack(); // Area de dibujo exacta de 1280 x 720 pixeles.
            ventana.setResizable(false);
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
            panel.iniciar();
        });
    }

    private static void uso() {
        System.err.println("Uso: java Proyecto2 <factor_mayor_que_1> <perseguidores_1_a_5>");
        System.err.println("Ejemplo: java Proyecto2 1.5 3");
    }
}
