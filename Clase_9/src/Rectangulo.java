public class Rectangulo extends Figura {

    private double base, altura;

    // Constructor de la clase Rectangulo
    public Rectangulo(double base, double altura, Coordenada centro) {

        super(centro, crearVertices(base, altura, centro));

        this.base = base;
        this.altura = altura;
    }

    // Metodo para crear los vertices del rectangulo
    private static Coordenada[] crearVertices(
            double base,
            double altura,
            Coordenada centro) {

        double x = centro.abcisa();
        double y = centro.ordenada();

        Coordenada[] vertices = new Coordenada[4];

        vertices[0] = new Coordenada(
                x - base / 2,
                y + altura / 2
        );

        vertices[1] = new Coordenada(
                x + base / 2,
                y + altura / 2
        );

        vertices[2] = new Coordenada(
                x + base / 2,
                y - altura / 2
        );

        vertices[3] = new Coordenada(
                x - base / 2,
                y - altura / 2
        );

        return vertices;
    }

    @Override
    public double area() {
        return areaPorCoordenadas();
    }
}