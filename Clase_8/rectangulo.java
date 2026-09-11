public class rectangulo {
    private coordenada superiorIzq, inferiorDer;

    public rectangulo() {
        this.superiorIzq = new coordenada(0, 0);
        this.inferiorDer = new coordenada(0, 0);
    }

    public rectangulo(double xSupIzq, double ySupIzq, double xInfDer, double yInfDer) {
        this.superiorIzq = new coordenada(xSupIzq, ySupIzq);
        this.inferiorDer = new coordenada(xInfDer, yInfDer);
    }

    // Constructor agregado para el Ejercicio 1
    public rectangulo(coordenada supIzq, coordenada infDer) {
        this.superiorIzq = supIzq;
        this.inferiorDer = infDer;
    }

    public coordenada superiorIzquierda() { 
        return superiorIzq; 
    }

    public coordenada inferiorDerecha() { 
        return inferiorDer; 
    }

    @Override
    public String toString() {
        return "Esquina superior izquierda: " + superiorIzq + 
               "\tEsquina inferior derecha: " + inferiorDer + "\n";
    }
}