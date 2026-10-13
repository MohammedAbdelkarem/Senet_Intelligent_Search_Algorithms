public class Main {
    public static void main(String[] args) {
        java.util.Scanner sc = new java.util.Scanner(System.in);

        Gamelogic engine = new Gamelogic();
        printProbabilities();

        int mode = 0;
        while (mode != 1 && mode != 2) {
            System.out.println("Choose game mode:");
            System.out.println("1) Human vs Human");
            System.out.println("2) AI vs Human (White = Human, Black = AI)");
            System.out.print("> ");

            String input = sc.nextLine().trim();
            try {
                mode = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                mode = 0;
            }

            if (mode != 1 && mode != 2) {
                System.out.println("Invalid choice. Please enter 1 or 2.\n");
            }
        }

        if (mode == 1) {
            engine.startPlayerVsPlayer();
        } else {
            engine.startHumanVsAI();
        }
    }

    // Print roll probabilities
    private static void printProbabilities() {
        System.out.println("Probability of each throw:");

        for (int k = 1; k <= 5; k++) {
            double p = Sticks.probabilityOfRoll(k);
            System.out.printf("throw %d: %.2f%%\n", k, p * 100);
        }

        System.out.println();
    }
}
