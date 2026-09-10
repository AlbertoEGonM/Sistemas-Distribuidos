import java.util.Random;

public class P1 {

    private static final Random random = new Random();


    public static void main(String[] args) {

        final char[] ABC = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        // Get number from args
        String number = args[0];


        try {
            int n = Integer.parseInt(number);
            StringBuilder large_chain = new StringBuilder();

            // Do large_chain.
            for (int i = 0; i < n; i++) {

                // Do 1 word.
                for (int j = 0; j < 3; j++) {
                    int al = random.nextInt(25);
                    large_chain.append(ABC[al]);
                }
                large_chain.append(' ');
            }

            String[] words = large_chain.toString().split(" ");

            for (int i = 0; i < words.length; i++ ) {

                String word = words[i];
                if (word.equals("IPN")) {
                    System.out.println("IPN encontrado en la posición: [" + i + "]");
                    break;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese un numero valido");
        }
    }

}