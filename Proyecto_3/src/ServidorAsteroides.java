// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Servidor HTTP del JDK; ninguna biblioteca grafica externa. */
public final class ServidorAsteroides implements AutoCloseable {
    private final Simulacion simulacion;
    private final HttpServer servidor;
    private final ExecutorService trabajadores = Executors.newFixedThreadPool(4);
    private final AtomicBoolean cerrado = new AtomicBoolean();

    public ServidorAsteroides(int puerto, Simulacion simulacion) throws IOException {
        this.simulacion = simulacion;
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.setExecutor(trabajadores);
        servidor.createContext("/", this::atender);
    }

    public void iniciar() { servidor.start(); }
    public int getPuerto() { return servidor.getAddress().getPort(); }

    private void atender(HttpExchange e) throws IOException {
        try {
            String ruta = e.getRequestURI().getPath();
            String metodo;
            if (ruta.equals("/estado")) metodo = "GET";
            else if (ruta.equals("/asteroides") || ruta.equals("/aleatorios")) metodo = "POST";
            else { responder(e, 404, "error=Ruta inexistente\n"); return; }
            if (!metodo.equals(e.getRequestMethod())) {
                e.getResponseHeaders().set("Allow", metodo);
                responder(e, 405, "error=Metodo no permitido\n"); return;
            }
            int total;
            if (ruta.equals("/estado")) total = simulacion.total();
            else {
                String tipo = e.getRequestHeaders().getFirst("Content-Type");
                if (tipo == null || !tipo.split(";", 2)[0].trim()
                        .equalsIgnoreCase("application/x-www-form-urlencoded")) {
                    responder(e, 415, "error=Use application/x-www-form-urlencoded\n"); return;
                }
                byte[] cuerpo = e.getRequestBody().readNBytes(4097);
                if (cuerpo.length > 4096) {
                    responder(e, 413, "error=Cuerpo demasiado grande\n"); return;
                }
                Map<String, String> campos = formulario(new String(cuerpo, StandardCharsets.UTF_8));
                if (ruta.equals("/aleatorios")) {
                    total = simulacion.agregarAleatorios(Integer.parseInt(requerido(campos, "n")));
                } else {
                    total = simulacion.agregar(numero(campos, "v"), numero(campos, "dx"),
                            numero(campos, "dy"), numero(campos, "omega"));
                }
            }
            responder(e, metodo.equals("POST") ? 201 : 200, "total=" + total + "\n");
        } catch (IllegalArgumentException ex) {
            responder(e, 400, "error=" + ex.getMessage().replace('\n', ' ') + "\n");
        } finally { e.close(); }
    }

    private static Map<String, String> formulario(String texto) {
        Map<String, String> resultado = new HashMap<>();
        for (String par : texto.split("&")) {
            String[] partes = par.split("=", 2);
            if (partes.length != 2) throw new IllegalArgumentException("Formulario invalido.");
            String clave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
            if (resultado.putIfAbsent(clave, valor) != null)
                throw new IllegalArgumentException("Campo repetido: " + clave);
        }
        return resultado;
    }

    private static String requerido(Map<String, String> campos, String clave) {
        String valor = campos.get(clave);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Falta " + clave);
        return valor;
    }

    private static double numero(Map<String, String> campos, String clave) {
        return Double.parseDouble(requerido(campos, clave));
    }

    private static void responder(HttpExchange e, int codigo, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        e.getResponseHeaders().set("Cache-Control", "no-store");
        e.sendResponseHeaders(codigo, bytes.length);
        e.getResponseBody().write(bytes);
    }

    @Override public void close() {
        if (cerrado.compareAndSet(false, true)) {
            servidor.stop(0);
            trabajadores.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        int puerto = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        if (puerto < 1 || puerto > 65535) throw new IllegalArgumentException("Puerto: 1 a 65535.");
        Simulacion modelo = new Simulacion();
        ServidorAsteroides http = new ServidorAsteroides(puerto, modelo);
        Runtime.getRuntime().addShutdownHook(new Thread(http::close));
        SwingUtilities.invokeAndWait(() -> {
            PanelEspacio panel = new PanelEspacio(modelo);
            JFrame ventana = new JFrame("Proyecto 3 - Servidor de asteroides - HTTP " + puerto);
            ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventana.setContentPane(panel);
            ventana.pack();
            ventana.setResizable(false);
            ventana.setLocationRelativeTo(null);
            ventana.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) { panel.detener(); http.close(); }
            });
            http.iniciar();
            ventana.setVisible(true);
            panel.iniciar();
        });
        System.out.println("Servidor HTTP en http://localhost:" + puerto + " - espacio vacio 1000x1000.");
        System.out.println("Cerrar la ventana detiene el servidor. Ctrl+C tambien termina el proceso.");
    }
}
