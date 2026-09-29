//Actividad Clase 11 - Programadores y On-line

//Archivos:
//- GeneradorCURP.java: genera cadenas de 18 caracteres con la estructura del ejemplo.
//- Ejercicio1.java: elimina por sexo usando Iterator.remove().
//- Ejercicio2.java: inserta en orden con ListIterator.add().

//H significa hombre; M significa mujer. El segundo argumento de Ejercicio1 indica el sexo que se ELIMINA.
//Las cadenas son aleatorias de ejemplo: no garantizan una CURP oficial valida (fecha, nombres ni digito verificador).

//static: un miembro static pertenece a la clase. main debe ser static para que la JVM lo invoque sin crear un objeto. generar() es static porque no necesita estado de instancia. Las constantes static final se comparten entre llamadas.

//Ejercicio1: el sexo ocupa el indice 10 (undecimo caracter). Se obtiene un Iterator, se avanza con next() y se elimina el ultimo elemento leido con remove().
//Ejercicio2: se recorren los prefijos con compareTo(); si el nuevo va antes, previous() devuelve el cursor a la posicion de insercion y add() lo inserta. Si no se encuentra uno mayor, add() agrega al final. Los prefijos iguales conservan su orden de llegada.
import java.util.concurrent.ThreadLocalRandom;

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

    public static String generar() {
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        StringBuilder curp = new StringBuilder(18);

        for (int i = 0; i < 4; i++) {
            curp.append(LETRAS.charAt(azar.nextInt(LETRAS.length())));
        }

        for (int i = 0; i < 6; i++) {
            curp.append(NUMEROS.charAt(azar.nextInt(NUMEROS.length())));
        }

        curp.append(SEXOS.charAt(azar.nextInt(SEXOS.length())));
        curp.append(ENTIDADES[azar.nextInt(ENTIDADES.length)]);

        for (int i = 0; i < 3; i++) {
            curp.append(LETRAS.charAt(azar.nextInt(LETRAS.length())));
        }

        for (int i = 0; i < 2; i++) {
            curp.append(NUMEROS.charAt(azar.nextInt(NUMEROS.length())));
        }

        return curp.toString();
    }
}