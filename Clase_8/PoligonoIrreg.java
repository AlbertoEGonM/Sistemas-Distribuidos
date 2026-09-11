public class PoligonoIrreg {
    private coordenada[] vertices;

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
        }
    }

    // Metodo para modificar el vertice n-esimo
    public void modificaVertice(int n, coordenada nuevaCoord) {
        if (n >= 0 && n < vertices.length) {
            this.vertices[n] = nuevaCoord;
        } else {
            System.out.println("Índice de vértice fuera de rango: " + n);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vértices del Polígono Irregular (Total: ").append(vertices.length).append("):\n");
        for (int i = 0; i < vertices.length; i++) {
            sb.append("Vértice [").append(i).append("]: ").append(vertices[i]).append("\n");
        }
        return sb.toString();
    }
}