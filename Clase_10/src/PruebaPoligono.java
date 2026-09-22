
public class PruebaPoligono {
    public static void main(String[] args) {
        // 1. Crear un objeto PoligonoIrreg con 7 vértices
        PoligonoIrreg poligono = new PoligonoIrreg(7); // Se generan automáticamente 7 vértices aleatorios

        // 2. Imprimir el polígono irregular para verificar que se han generado los vértices correctamente
        System.out.println("--- Polígono Irregular Inicial (7 vértices) ---");
        System.out.println(poligono);

        //3. Agregar un nuevo vértice random al polígono
        coordenada nuevoVertice = new coordenada((Math.random() * 200.0) - 100.0, (Math.random() * 200.0) - 100.0);
        nuevoVertice = new coordenada(Math.round(nuevoVertice.abcisa() * 100.0) / 100.0, Math.round(nuevoVertice.ordenada() * 100.0) / 100.0);
        System.out.println("Agregando un nuevo vértice: " + nuevoVertice + "\n");
        poligono.AnadirVertice(nuevoVertice);


        // 4. Volver a imprimir el objeto para verificar el cambio
        System.out.println("--- Polígono Irregular Después de Agregar un Vértice ---");
        System.out.println(poligono);

        //Imprimir la maginutd de los vertices sin ordenar para verificar que se han generado correctamente
        System.out.println("--- Magnitud de los Vértices Antes de Ordenar ---");
        for (coordenada vertice : poligono.getVerticesList()) {
            System.out.println("Magnitud del vértice: " + vertice.magnitud());
        }
        // 5. Ordenar los vértices del polígono irregular de menor a mayor basado en su magnitud (distancia al origen)
        poligono.ordenaVertices();

        // 6. Imprimir la magnitud de los vértices ordenados para verificar que se han ordenado correctamente
        System.out.println("--- Polígono Irregular Después de Ordenar los Vértices por Magnitud ---");
        for (coordenada vertice : poligono.getVerticesList()) {
            System.out.println("Magnitud del vértice: " + vertice.magnitud());
        }
        

    }

}
