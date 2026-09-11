public class Pruebarectangulo {
    public static void main(String[] args) {
        // Constructor original con valores primitivos
        rectangulo rectOriginal = new rectangulo(2, 3, 5, 1);
        double altoOrig = rectOriginal.superiorIzquierda().ordenada() - rectOriginal.inferiorDerecha().ordenada();
        double anchoOrig = rectOriginal.inferiorDerecha().abcisa() - rectOriginal.superiorIzquierda().abcisa();

        System.out.println("Constructor Original");
        System.out.println(rectOriginal);
        System.out.println("El área del rectángulo es: " + (anchoOrig * altoOrig));

        // Prueba del nuevo constructor recibiendo dos objetos Coordenada
        coordenada c1 = new coordenada(2, 3);
        coordenada c2 = new coordenada(5, 1);
        rectangulo rect1 = new rectangulo(c1, c2);

        double alto = rect1.superiorIzquierda().ordenada() - rect1.inferiorDerecha().ordenada();
        double ancho = rect1.inferiorDerecha().abcisa() - rect1.superiorIzquierda().abcisa();

        System.out.println("\n(Ejercicio 1)");
        System.out.println("Calculando el área de un rectángulo dadas sus coordenadas en un plano cartesiano:");
        System.out.println(rect1);
        System.out.println("El área del rectángulo es: " + (ancho * alto));
    }
}