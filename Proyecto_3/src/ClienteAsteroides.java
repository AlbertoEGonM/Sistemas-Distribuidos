// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Teclado en Lanterna; un trabajador hace HTTP sin bloquear la interfaz. */
public final class ClienteAsteroides {
    private final ClienteHTTP http;
    private final ExecutorService red = Executors.newSingleThreadExecutor();
    private MultiWindowTextGUI gui;
    private final Label contador = new Label("Asteroides creados en el servidor: --");
    private final Label estado = new Label("Conectando...");
    private boolean ocupado;

    public ClienteAsteroides(String direccion) { http = new ClienteHTTP(direccion); }

    @FunctionalInterface private interface Operacion { int ejecutar() throws Exception; }

    private void solicitar(Operacion operacion) {
        if (ocupado) { estado.setText("Espera a que termine la solicitud actual."); return; }
        ocupado = true;
        estado.setText("Esperando respuesta HTTP...");
        red.submit(() -> {
            String mensaje;
            int total = -1;
            try { total = operacion.ejecutar(); mensaje = "Solicitud completada."; }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); mensaje = "Solicitud interrumpida."; }
            catch (Exception ex) { mensaje = "Error: " + ex.getMessage(); }
            final int resultado = total;
            final String aviso = mensaje;
            gui.getGUIThread().invokeLater(() -> {
                if (resultado >= 0) contador.setText("Asteroides creados en el servidor: " + resultado);
                estado.setText(aviso);
                ocupado = false;
            });
        });
    }

    private void ejecutar(boolean terminalTexto) throws Exception {
        DefaultTerminalFactory fabrica = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(92, 30))
                .setTerminalEmulatorTitle("Proyecto 3 - Cliente Lanterna");
        if (terminalTexto) fabrica.setForceTextTerminal(true);
        ejecutarEnPantalla(fabrica.createScreen());
    }

    // Permite probar la interfaz con una terminal virtual, sin abrir ventanas.
    void ejecutarEnPantalla(Screen pantallaCreada) throws Exception {
        try (Screen pantalla = pantallaCreada) {
            pantalla.startScreen();
            gui = new MultiWindowTextGUI(pantalla);
            BasicWindow ventana = new BasicWindow("Asteroides / control HTTP");
            Panel principal = new Panel();
            principal.addComponent(new Label("TAB: cambiar campo | ENTER: activar boton | escribir con teclado"));
            principal.addComponent(contador);
            principal.addComponent(new Label(" "));
            principal.addComponent(new Label("Crear un asteroide (x crece a la derecha, y hacia abajo)"));
            Panel campos = new Panel(new GridLayout(2));
            TextBox rapidez = campo(campos, "Rapidez (px/s, >0):", "100");
            TextBox dx = campo(campos, "Direccion dx:", "1");
            TextBox dy = campo(campos, "Direccion dy:", "1");
            TextBox giro = campo(campos, "Giro (grados/s, signo +/-):", "45");
            principal.addComponent(campos);
            principal.addComponent(new Label("Giro +: horario | Giro -: antihorario | Giro 0: sin rotacion"));
            principal.addComponent(new Button("Crear asteroide", () -> {
                try {
                    double v = decimal(rapidez), x = decimal(dx), y = decimal(dy), w = decimal(giro);
                    FabricaAsteroides.validar(v, x, y, w);
                    solicitar(() -> http.agregar(v, x, y, w));
                } catch (IllegalArgumentException ex) { estado.setText("Dato invalido: " + ex.getMessage()); }
            }));
            principal.addComponent(new Label(" "));
            Panel lote = new Panel(new GridLayout(2));
            TextBox cantidad = campo(lote, "Cantidad aleatoria (1-10000):", "5");
            principal.addComponent(lote);
            principal.addComponent(new Button("Crear n aleatorios", () -> {
                try {
                    int n = Integer.parseInt(cantidad.getText().trim());
                    solicitar(() -> http.aleatorios(n));
                } catch (NumberFormatException ex) { estado.setText("Cantidad: escribe un numero entero."); }
            }));
            principal.addComponent(new Label("Aleatorios: 40-180 px/s; giro +/-15 a +/-180 grados/s."));
            principal.addComponent(new Label(" "));
            principal.addComponent(estado);
            principal.addComponent(new Button("Actualizar contador / reconectar", () -> solicitar(http::total)));
            principal.addComponent(new Button("Salir del cliente", ventana::close));
            ventana.setComponent(principal);
            solicitar(http::total);
            gui.addWindowAndWait(ventana);
        } finally { red.shutdownNow(); }
    }

    private static TextBox campo(Panel panel, String titulo, String inicial) {
        panel.addComponent(new Label(titulo));
        TextBox entrada = new TextBox(new TerminalSize(22, 1), inicial);
        panel.addComponent(entrada);
        return entrada;
    }

    private static double decimal(TextBox campo) {
        return Double.parseDouble(campo.getText().trim().replace(',', '.'));
    }

    public static void main(String[] args) throws Exception {
        String direccion = "http://localhost:8080";
        boolean texto = false;
        for (String arg : args) {
            if (arg.equals("--terminal")) texto = true; else direccion = arg;
        }
        new ClienteAsteroides(direccion).ejecutar(texto);
    }
}
