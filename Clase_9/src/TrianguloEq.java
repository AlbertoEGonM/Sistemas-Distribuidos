public class TrianguloEq extends Figura {

    private double lado;

    // Constructor de la clase TrianguloEq
    public TrianguloEq(double lado, Coordenada centro) {

        super(centro, crearVertices(lado, centro));

        this.lado = lado;
    }

    // Metodo privado para crear los vertices del triangulo equilatero a partir del lado y el centro
    private static Coordenada[] crearVertices(double lado, Coordenada centro) { // Metodo privado para crear los vertices del triangulo equilatero a partir del lado y el centro

        double altura = Math.sqrt(3) * lado / 2; // Calcula la altura del triangulo equilatero usando la formula de la altura de un triangulo equilatero: h = (sqrt(3) / 2) * lado

        double x = centro.abcisa();
        double y = centro.ordenada();

        Coordenada[] vertices = new Coordenada[3]; // Crea un arreglo de 3 coordenadas para los vertices del triangulo equilatero

        vertices[0] = new Coordenada(
                x, y + (2.0 / 3.0) * altura // Calcula la coordenada y del vertice superior del triangulo equilatero usando la formula: y = y + (2/3) * altura
        );

        vertices[1] = new Coordenada(
                x - lado / 2, y - (1.0 / 3.0) * altura
        );

        vertices[2] = new Coordenada(
                x + lado / 2, y - (1.0 / 3.0) * altura
        );

        return vertices;
    }

    @Override
    public double area() {

        return areaPorCoordenadas();
    }
}
