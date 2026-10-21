import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;

/** Busqueda secuencial educativa de cinco letras ASCII minusculas. */
public class FuerzaBrutaHash {
    public static String sha256(String texto) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
            .digest(texto.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(64);
        for (byte b : digest) hex.append(String.format("%02x", b & 255));
        return hex.toString();
    }
    public static byte[] decodificar(String hash) {
        if (!hash.matches("[0-9a-fA-F]{64}"))
            throw new IllegalArgumentException("El hash debe tener 64 caracteres hexadecimales");
        byte[] bytes = new byte[32];
        for (int i = 0; i < 32; i++)
            bytes[i] = (byte) Integer.parseInt(hash.substring(i*2, i*2+2), 16);
        return bytes;
    }
    public static class Resultado {
        public final String palabra;
        public final long intentos;
        Resultado(String palabra, long intentos) { this.palabra = palabra; this.intentos = intentos; }
    }
    public static Resultado buscar(String hash, char desde, char hasta) throws Exception {
        if (desde < 'a' || hasta > 'z' || desde > hasta)
            throw new IllegalArgumentException("Rango invalido");
        byte[] objetivo = decodificar(hash);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] candidato = new byte[5];
        long intentos = 0;
        for (char a = desde; a <= hasta; a++) {
            candidato[0] = (byte) a;
            for (char b = 'a'; b <= 'z'; b++) {
                candidato[1] = (byte) b;
                for (char c = 'a'; c <= 'z'; c++) {
                    candidato[2] = (byte) c;
                    for (char d = 'a'; d <= 'z'; d++) {
                        candidato[3] = (byte) d;
                        for (char e = 'a'; e <= 'z'; e++) {
                            candidato[4] = (byte) e;
                            intentos++;
                            if (Arrays.equals(md.digest(candidato), objetivo))
                                return new Resultado(new String(candidato, StandardCharsets.UTF_8), intentos);
                        }
                    }
                }
            }
        }
        return new Resultado(null, intentos);
    }
    public static void main(String[] args) throws Exception {
        String palabra = args.length == 0 ? "zorro" : args[0];
        if (!palabra.matches("[a-z]{5}"))
            throw new IllegalArgumentException("Use exactamente cinco letras de a a z");
        String hash = sha256(palabra);
        System.out.println("Hash objetivo: " + hash);
        System.out.println("Buscando contrasena de cinco letras...");
        long inicio = System.nanoTime();
        Resultado r = buscar(hash, 'a', 'z');
        double ms = (System.nanoTime() - inicio) / 1_000_000.0;
        System.out.println("Contrasena encontrada: " + r.palabra);
        System.out.println("Intentos: " + r.intentos);
        System.out.printf(Locale.ROOT, "Tiempo(ms): %.3f%nTiempo(s): %.6f%n", ms, ms/1000);
    }
}
