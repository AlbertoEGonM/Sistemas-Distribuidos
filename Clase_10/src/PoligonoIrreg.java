import java.util.ArrayList;
import static java.util.Arrays.sort;
public class PoligonoIrreg{
    private coordenada[] vertices;

    private ArrayList<coordenada> verticesList = new ArrayList<>();

    public PoligonoIrreg(int numVertices) {
        this.vertices = new coordenada[numVertices];

        for (int i = 0; i < numVertices; i++) {
            // Rango [-100.0, 100.0] para cubrir los cuatro cuadrantes
            double x = (Math.random() * 200.0) - 100.0;
            double y = (Math.random() * 200.0) - 100.0;

            // Redondeo a 2 decimales para que sea legible en consola
            x = Math.round(x * 100.0) / 100.0;
            y = Math.round(y * 100.0) / 100.0;

            this.vertices[i] = new coordenada(x, y);
            this.verticesList.add(new coordenada(x, y));
        }
    }

    // Metodo para modificar el vertice n-esimo
    public void modificaVertice(int n, coordenada nuevaCoord) {
        if (n >= 0 && n < vertices.length) {
            this.vertices[n] = nuevaCoord;
            this.verticesList.set(n, nuevaCoord);
        } else {
            System.out.println("Índice de vértice fuera de rango: " + n);
        }
    }

    public void AnadirVertice(coordenada nuevaCoord) {
        // Crear un nuevo arreglo con un tamaño mayor
        coordenada[] nuevoVertices = new coordenada[vertices.length + 1];

        // Copiar los vértices existentes al nuevo arreglo
        for (int i = 0; i < vertices.length; i++) {
            nuevoVertices[i] = vertices[i];
        }

        // Agregar el nuevo vértice al final del arreglo
        nuevoVertices[vertices.length] = nuevaCoord;

        // Actualizar la referencia del arreglo de vértices
        this.vertices = nuevoVertices;
        this.verticesList.add(nuevaCoord);
    }


    public void ordenaVertices() {
        // Ordenar los vértices de menor a mayor basado en su magnitud (distancia al origen) utilizando una interfaz comparator
        sort(this.vertices, (v1, v2) -> Double.compare(v1.magnitud(), v2.magnitud()));

        // Actualizar la lista de vértices después de ordenar
        this.verticesList.clear();
        for (coordenada vertice : this.vertices) {
            this.verticesList.add(vertice);
        }
    }

    // Método para obtener la lista de vértices
    public ArrayList<coordenada> getVerticesList() {
        return this.verticesList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Polígono Irregular con ").append(vertices.length).append(" vértices:\n");
        for (int i = 0; i < vertices.length; i++) {
            sb.append("Vértice ").append(i).append(": ").append(vertices[i]).append("\n");
        }
        return sb.toString();
    }
}