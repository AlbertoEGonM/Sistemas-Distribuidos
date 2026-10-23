// Proyecto 3 - Sistemas Distribuidos
// Nombre: Gonzalez Martinez Alberto Ezequiel
// Grupo: 7CM3
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/** Pruebas sin JUnit: modelo, dibujo fuera de pantalla e integracion HTTP real. */
public final class PruebasProyecto3 {
    private static int comprobaciones;

    private static void verificar(boolean cumple, String mensaje) {
        comprobaciones++;
        if (!cumple) throw new AssertionError(mensaje);
    }

    private static void cerca(double real, double esperado, String mensaje) {
        verificar(Math.abs(real - esperado) < 1e-7, mensaje + ": " + real + " != " + esperado);
    }

    private static BufferedImage lienzo() {
        BufferedImage imagen = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1000, 1000); g.dispose();
        return imagen;
    }

    private static void modelo() throws Exception {
        FabricaAsteroides fabrica = new FabricaAsteroides(new Random(73));
        Set<String> firmas = new HashSet<>();
        BufferedImage inicio = lienzo();
        Graphics2D g = inicio.createGraphics();
        g.setColor(Color.WHITE);
        for (int i = 0; i < 3000; i++) {
            // Incluye ejes puros, cuatro cuadrantes y vectores casi tangentes.
            double dx = i % 7 == 0 ? 0 : (i % 2 == 0 ? 1 : -1);
            double dy = i % 5 == 0 ? 0 : (i % 3 == 0 ? 0.00001 : -2);
            if (dx == 0 && dy == 0) dy = 1;
            Asteroide a = fabrica.crear(100, dx, dy, i % 2 == 0 ? 90 : -90);
            verificar(firmas.add(a.getFirma()), "Forma repetida");
            verificar(a.getX() + a.getRadio() < 0 || a.getX() - a.getRadio() > 1000
                    || a.getY() + a.getRadio() < 0 || a.getY() - a.getRadio() > 1000,
                    "El asteroide nace visible");
            a.dibujar(g);
            cerca(Math.hypot(a.getVx(), a.getVy()), 100, "Rapidez normalizada");
            double x0 = a.getX(), y0 = a.getY();
            a.avanzar(0.001);
            cerca(a.getX(), x0 + a.getVx() * 0.001, "Desplazamiento x");
            cerca(a.getY(), y0 + a.getVy() * 0.001, "Desplazamiento y");
            for (int t = 0; t < 200; t++) a.avanzar(0.05);
            verificar(a.estaEnEspacio(), "No entro desde el exterior");
            verificar(a.getX() >= 0 && a.getX() < 1000 && a.getY() >= 0 && a.getY() < 1000,
                    "No se conserva el espacio toroidal");
        }
        g.dispose();
        for (int y = 0; y < 1000; y++) for (int x = 0; x < 1000; x++)
            if ((inicio.getRGB(x, y) & 0xffffff) != 0) throw new AssertionError("Dibujo inicial no vacio");
        verificar(true, "Todos los nacimientos son invisibles");

        Path2D.Double forma = new Path2D.Double();
        forma.moveTo(-12, -8); forma.lineTo(12, 0); forma.lineTo(-12, 8); forma.closePath();
        Asteroide borde = new Asteroide(forma, "test", 15, 995, 995, 10, 10, -Math.PI, 0);
        borde.avanzar(1);
        cerca(borde.getX(), 5, "Envoltura derecha"); cerca(borde.getY(), 5, "Envoltura abajo");
        cerca(borde.getAngulo(), Math.PI, "Giro negativo");
        borde.avanzar(1000);
        cerca(borde.getX(), 5, "Multiples vueltas sin perder atributos");
        cerca(borde.getVx(), 10, "Velocidad conservada");
        Asteroide otro = new Asteroide(forma, "test2", 15, 5, 5, -10, -10, Math.PI, 0);
        otro.avanzar(1);
        cerca(otro.getX(), 995, "Envoltura izquierda"); cerca(otro.getY(), 995, "Envoltura arriba");
        cerca(otro.getAngulo(), Math.PI, "Giro positivo");
        BufferedImage esquina = lienzo();
        g = esquina.createGraphics(); g.setColor(Color.WHITE); borde.dibujar(g); g.dispose();
        verificar(hayBlanco(esquina, 0, 30, 0, 30), "Falta fragmento en origen");
        verificar(hayBlanco(esquina, 975, 1000, 0, 30), "Falta copia horizontal");
        verificar(hayBlanco(esquina, 0, 30, 975, 1000), "Falta copia vertical");
        ImageIO.write(esquina, "png", new File("tmp/prueba_esquina.png"));
        otro.avanzar(0.25);
        borde.avanzar(0.25);
        cerca(otro.getAngulo(), Math.PI * 1.25, "Sentido positivo");
        cerca(borde.getAngulo(), Math.PI * 0.75, "Sentido negativo");

        for (double[] valores : new double[][] {{0,1,0,1}, {-1,1,0,1}, {1,0,0,1},
                {Double.NaN,1,0,1}, {1,Double.POSITIVE_INFINITY,0,1}, {1,1,0,Double.NaN}}) {
            boolean rechazo = false;
            try { fabrica.crear(valores[0], valores[1], valores[2], valores[3]); }
            catch (IllegalArgumentException ex) { rechazo = true; }
            verificar(rechazo, "Acepto atributos invalidos");
        }
    }

    private static boolean hayBlanco(BufferedImage imagen, int x0, int x1, int y0, int y1) {
        for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++)
            if ((imagen.getRGB(x, y) & 0xffffff) != 0) return true;
        return false;
    }

    private static void http() throws Exception {
        Simulacion modelo = new Simulacion();
        try (ServidorAsteroides servidor = new ServidorAsteroides(0, modelo)) {
            servidor.iniciar();
            String base = "http://localhost:" + servidor.getPuerto();
            ClienteHTTP cliente = new ClienteHTTP(base);
            verificar(cliente.total() == 0, "Estado inicial");
            verificar(cliente.agregar(100, 1, 0, 90) == 1, "Primera creacion");
            verificar(cliente.aleatorios(5) == 6, "Lote de 5");
            verificar(cliente.agregar(80, -1, 1, -90) == 7, "Segunda creacion");
            verificar(cliente.aleatorios(10) == 17, "Lote de 10");
            verificar(cliente.aleatorios(1000) == 1017, "Lote de 1000");
            verificar(cliente.total() == 1017, "Contador persistente");

            ExecutorService hilos = Executors.newFixedThreadPool(4);
            try {
                List<Callable<Integer>> tareas = new ArrayList<>();
                for (int i = 0; i < 12; i++) tareas.add(() -> new ClienteHTTP(base).aleatorios(5));
                for (var futuro : hilos.invokeAll(tareas)) futuro.get();
            } finally { hilos.shutdownNow(); }
            verificar(cliente.total() == 1077, "Creaciones concurrentes sin perdidas");

            HttpClient raw = HttpClient.newHttpClient();
            error(raw, base, "/aleatorios", "GET", null, 405);
            error(raw, base, "/no-existe", "GET", null, 404);
            error(raw, base, "/estado/otra", "GET", null, 404);
            error(raw, base, "/aleatorios", "POST", "n=-1", 400);
            error(raw, base, "/aleatorios", "POST", "n=0", 400);
            error(raw, base, "/aleatorios", "POST", "n=10001", 400);
            error(raw, base, "/asteroides", "POST", "v=1&dx=0&dy=0&omega=0", 400);
            error(raw, base, "/asteroides", "POST", "v=NaN&dx=1&dy=0&omega=0", 400);
            error(raw, base, "/asteroides", "POST", "v=1&dx=1", 400);
            error(raw, base, "/aleatorios", "POST", "n=1&n=2", 400);
            error(raw, base, "/aleatorios", "POST", "n=%ZZ", 400);
            error(raw, base, "/aleatorios", "POST", "x=" + "0".repeat(4100), 413);
            HttpResponse<String> tipo = raw.send(HttpRequest.newBuilder(URI.create(base + "/aleatorios"))
                    .POST(HttpRequest.BodyPublishers.ofString("n=1")).build(), HttpResponse.BodyHandlers.ofString());
            verificar(tipo.statusCode() == 415, "Tipo de contenido");
            verificar(cliente.total() == 1077, "Los errores no modifican el total");
            for (int i = 0; i < 1200; i++) modelo.avanzar(1.0 / 60);
            verificar(modelo.total() == 1077, "Ningun asteroide desaparece");
            BufferedImage imagen = lienzo();
            Graphics2D g = imagen.createGraphics(); g.setColor(Color.WHITE); modelo.dibujar(g); g.dispose();
            verificar(hayBlanco(imagen, 0, 1000, 0, 1000), "Simulacion no dibuja");
            ImageIO.write(imagen, "png", new File("tmp/prueba_simulacion.png"));
        }
    }

    private static void error(HttpClient cliente, String base, String ruta, String metodo,
                              String cuerpo, int esperado) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(base + ruta));
        if (metodo.equals("POST")) req.header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        else req.GET();
        verificar(cliente.send(req.build(), HttpResponse.BodyHandlers.ofString()).statusCode() == esperado,
                "HTTP " + esperado + " en " + ruta + " " + cuerpo);
    }

    private static String textoPantalla(TerminalScreen pantalla) {
        StringBuilder texto = new StringBuilder();
        for (int y = 0; y < 30; y++) {
            for (int x = 0; x < 92; x++) texto.append(pantalla.getFrontCharacter(x, y).getCharacterString());
            texto.append('\n');
        }
        return texto.toString();
    }

    private static void esperarPantalla(TerminalScreen pantalla, String esperado) throws Exception {
        long limite = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < limite) {
            if (textoPantalla(pantalla).contains(esperado)) { verificar(true, esperado); return; }
            Thread.sleep(20);
        }
        throw new AssertionError("No aparecio: " + esperado + "\n" + textoPantalla(pantalla));
    }

    private static void tecla(DefaultVirtualTerminal terminal, KeyType tipo, int veces) {
        for (int i = 0; i < veces; i++) terminal.addInput(new KeyStroke(tipo));
    }

    private static void interfaz() throws Exception {
        try (ServidorAsteroides servidor = new ServidorAsteroides(0, new Simulacion())) {
            servidor.iniciar();
            DefaultVirtualTerminal terminal = new DefaultVirtualTerminal(new TerminalSize(92, 30));
            TerminalScreen pantalla = new TerminalScreen(terminal);
            ClienteAsteroides cliente = new ClienteAsteroides("http://localhost:" + servidor.getPuerto());
            AtomicReference<Throwable> fallo = new AtomicReference<>();
            Thread hilo = new Thread(() -> {
                try { cliente.ejecutarEnPantalla(pantalla); }
                catch (Throwable ex) { fallo.set(ex); }
            }, "prueba-lanterna");
            hilo.setDaemon(true);
            hilo.start();
            try {
                esperarPantalla(pantalla, "Asteroides creados en el servidor: 0");
                // El foco empieza en rapidez. TAB recorre dx, dy, giro y Crear.
                tecla(terminal, KeyType.Tab, 4);
                tecla(terminal, KeyType.Enter, 1);
                esperarPantalla(pantalla, "Asteroides creados en el servidor: 1");
                tecla(terminal, KeyType.Tab, 2); // cantidad, luego Crear n
                tecla(terminal, KeyType.Enter, 1);
                esperarPantalla(pantalla, "Asteroides creados en el servidor: 6");
                Files.writeString(Path.of("tmp/prueba_lanterna.txt"), textoPantalla(pantalla));
                tecla(terminal, KeyType.Tab, 2); // actualizar, salir
                tecla(terminal, KeyType.Enter, 1);
                hilo.join(3000);
                verificar(!hilo.isAlive(), "El cliente debe cerrar limpiamente");
                verificar(fallo.get() == null, "Fallo de interfaz: " + fallo.get());
            } finally {
                if (hilo.isAlive()) { terminal.addInput(new KeyStroke(KeyType.EOF)); hilo.join(1000); }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new File("tmp").mkdirs();
        modelo();
        http();
        interfaz();
        System.out.println("OK: " + comprobaciones + " comprobaciones de modelo, dibujo y HTTP.");
        System.out.println("Secuencia del video validada: 0 -> 1 -> 6 -> 7 -> 17 -> 1017.");
    }
}
