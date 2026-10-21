public class TestSHA {
    public static void main(String[] args) throws Exception {
        for (String texto : new String[]{"hola", "Hola", "HOLA"})
            System.out.println(texto + " -> " + FuerzaBrutaHash.sha256(texto));
    }
}
