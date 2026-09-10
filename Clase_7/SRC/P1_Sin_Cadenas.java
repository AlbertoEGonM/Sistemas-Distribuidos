import java.util.Random;

public class P1_Sin_Cadenas {

    private static final Random random = new Random();


    public static void main(String[] args) {

        final char[] ABC = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        // Get number from args
        String number = args[0];


        try {
            int n = Integer.parseInt(number);
            char[] large_chain = new char[n * 4];
            int ic = 0;

            // Do large_chain.
            for (int i = 0; i < n; i++) {

                // Do 1 word.
                for (int j = 0; j < 3; j++) {
                    int al = random.nextInt(25);
                    large_chain[ic] = ABC[al];
                    ic++;
                }
                large_chain[ic] = ' ';
                ic++;
            }
            large_chain[--ic] = '\n';

            char[] actual_word = new char[3];
            int ai = 0;
            for (int i = 0; i < large_chain.length - 1; i++) {
                char c = large_chain[i];

                if (c == ' ') {
                    String word = String.valueOf(actual_word[0]) + String.valueOf(actual_word[1]) + String.valueOf(actual_word[2]);

                    if (word.equals("IPN")) {
                        System.out.println("IPN encontrado en las posiciones: [" + (i-2) + ", " + i + "]");
                        break;
                    }
                    else {
                        actual_word = new char[3];
                        ai = 0;
                    }
                }
                else {
                    actual_word[ai] = large_chain[i];
                    ai++;
                }

            }
        }
        catch (NumberFormatException e) {
            System.out.println("Ingrese un numero valido");
        }
    }

}