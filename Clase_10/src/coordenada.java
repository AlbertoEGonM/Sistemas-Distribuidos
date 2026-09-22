public class coordenada{
    private double x, y;
    public coordenada(double x, double y){
        this.x = x;
        this.y = y;
    }
    //Metodo getter de x
    public double abcisa () {
        return this.x;
    }
    //Metodo getter de y
    public double ordenada () {
        return this.y;
    }
    //Metodo para guardar magnitud de la coordenada
    public double magnitud() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }
    //Sobreescrituro del metodo de la superclase objeto para imprimir con System.out.println()
    @Override
    public String toString() {
        return "Coordenada: (" + this.x + ", " + this.y + ")";
    }
}