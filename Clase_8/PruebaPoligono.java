public class PruebaPoligono {
    public static void main(String[] args) {
        // 1. Crear un objeto PoligonoIrreg con 7 vértices
        PoligonoIrreg poligono = new PoligonoIrreg(7); // Se generan automáticamente 7 vértices aleatorios

        // 2. Imprimir el polígono irregular para verificar que se han generado los vértices correctamente
        System.out.println("--- Polígono Irregular Inicial (7 vértices) ---");
        System.out.println(poligono);

        // 3. Modificar un vértice específico (por ejemplo, el índice 3)
        coordenada nuevoPunto = new coordenada(0.0, 0.0);
        System.out.println("Modificando el vértice 3 a: " + nuevoPunto + "\n");
        poligono.modificaVertice(3, nuevoPunto);

        // 4. Volver a imprimir el objeto para verificar el cambio
        System.out.println("--- Polígono Irregular Actualizado ---");
        System.out.println(poligono);
    }

}
