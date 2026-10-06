import java.util.Random;

// Genera cadenas de ejemplo; no valida CURPs oficiales.
public class GeneradorCURP {
    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMEROS = "0123456789";
    private static final String SEXOS = "HM";
    private static final String[] ENTIDADES = {
        "AS", "BC", "BS", "CC", "CS", "CH", "CL", "CM",
        "DF", "DG", "GT", "GR", "HG", "JC", "MC", "MN",
        "MS", "NT", "NL", "OC", "PL", "QT", "QR", "SP",
        "SL", "SR", "TC", "TL", "TS", "VZ", "YN", "ZS"
    };
    private static final Random AZAR = new Random(14);

    public static String generar() {
        StringBuilder curp = new StringBuilder(18);
        for (int i = 0; i < 4; i++) {
            curp.append(LETRAS.charAt(AZAR.nextInt(LETRAS.length())));
        }
        for (int i = 0; i < 6; i++) {
            curp.append(NUMEROS.charAt(AZAR.nextInt(10)));
        }
        curp.append(SEXOS.charAt(AZAR.nextInt(2)));
        curp.append(ENTIDADES[AZAR.nextInt(ENTIDADES.length)]);
        for (int i = 0; i < 3; i++) {
            curp.append(LETRAS.charAt(AZAR.nextInt(LETRAS.length())));
        }
        for (int i = 0; i < 2; i++) {
            curp.append(NUMEROS.charAt(AZAR.nextInt(10)));
        }
        return curp.toString();
    }
}
