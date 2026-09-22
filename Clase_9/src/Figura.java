public abstract class Figura implements Desplazable {

    protected Coordenada centro;
    protected Coordenada[] vertices;

    // Constructor de la clase Figura, recibe el centro y los vertices de la figura
    public Figura(Coordenada centro, Coordenada[] vertices) {
        this.centro = centro;
        this.vertices = vertices;
    }

    // Metodo para calcular al area
    public abstract double area();

    // Aqui se dezplaza la figura sumando dx y dy a las coordenadas del centro y de los vertices
    @Override
    public void desplazar(double dx, double dy) {

        centro = new Coordenada(
                centro.abcisa() + dx,
                centro.ordenada() + dy
        );

        for (int i = 0; i < vertices.length; i++) { // Este for recorre el arreglo de vertices y desplaza cada uno de ellos sumando dx y dy a sus coordenadas
            vertices[i] = new Coordenada(
                    vertices[i].abcisa() + dx,
                    vertices[i].ordenada() + dy
            );
        }
    }

    // Calcula el area usando las coordenadas de los vertices
    // Como se calcula el area? Pues se calcula con el metodo de Gauss para poligonos, que consiste en sumar el producto de las coordenadas de los vertices 
    // y restar el producto de las coordenadas de los vertices siguientes, y luego dividir entre 2.0
    protected double areaPorCoordenadas() {

        double suma = 0;

        for (int i = 0; i < vertices.length; i++) { // Este for recorre el arreglo de vertices y calcula el area usando la formula de Gauss para poligonos
            int siguiente = (i + 1) % vertices.length; // Esto es para que cuando llegue al ultimo vertice, el siguiente sea el primero, para cerrar el poligono

            suma += vertices[i].abcisa() *
                    vertices[siguiente].ordenada();

            suma -= vertices[siguiente].abcisa() *
                    vertices[i].ordenada();
        }

        return Math.abs(suma) / 2.0;
    }

    // Muestra las coordenadas de los vertices
    public void mostrarVertices() {

        for (int i = 0; i < vertices.length; i++) {
            System.out.println("V" + (i + 1) + ": "
                    + vertices[i]);
        }
    }
}