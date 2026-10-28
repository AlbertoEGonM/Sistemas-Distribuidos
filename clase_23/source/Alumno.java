import java.util.Locale;

/** Recurso del micro-SAES. Los identificadores los asigna el servidor. */
public class Alumno {
    int id;
    String nombre;
    String apellido1;
    String apellido2;
    double promedio;

    public Alumno(int id, String nombre, String apellido1, String apellido2, double promedio) {
        this.id = id;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.promedio = promedio;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "ID: %d   Nombre: %s   Apellido 1: %s   Apellido 2: %s   Promedio: %s",
                id, nombre, apellido1, apellido2, Double.toString(promedio));
    }
}
