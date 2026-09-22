public class main {
    public static void main(String[]args){
        //Crear las figuras
        TrianguloEq triangulo = new TrianguloEq(2, new Coordenada(0, 0));
        Rectangulo rectangulo = new Rectangulo(4, 5, new Coordenada(0, 0));

        //Posicion Final
        System.out.println("Posicion Final: TrianguloEq");
        System.out.println("\nTRIANGULO EQUILATERO");

        System.out.printf("Area: %.2f%n",triangulo.area());
        triangulo.mostrarVertices();


        System.out.println("\nRECTANGULO");

        System.out.printf(
                "Area: %.2f%n",
                rectangulo.area()
        );

        rectangulo.mostrarVertices();

        //dezplazar figuras
        triangulo.desplazar(5, -5);
        rectangulo.desplazar(20, -5);

        //Posicion dezplazada
        System.out.println("\nTRIANGULO EQUILATERO");

        System.out.printf(
                "Area: %.2f%n",
                triangulo.area()
        );

        triangulo.mostrarVertices();


        System.out.println("\nRECTANGULO");

        System.out.printf(
                "Area: %.2f%n",
                rectangulo.area()
        );

        rectangulo.mostrarVertices();
    }    
}
