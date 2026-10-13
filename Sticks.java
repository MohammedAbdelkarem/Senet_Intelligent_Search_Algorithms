import java.util.Random;

public class Sticks {
    private final Random random;

    public Sticks() {
        this.random = new Random();
    }

    public int[] rollDetailed() {
        int[] sticks = new int[4];
        for (int i = 0; i < 4; i++) {
            sticks[i] = random.nextInt(2);
        }
        return sticks;
    }

    public int calculateRoll(int[] sticks) {
        int darkCount = 0;
        for (int s : sticks) darkCount += s;
        return (darkCount == 0) ? 5 : darkCount;
    }
    public static double probabilityOfRoll(int k) {
        if (k < 1 || k > 5) return 0.0;

        if (k == 5) {
            return 1.0 / 16.0;
        }

        int ways = binomialCoefficient(4, k);
        return (double) ways / Math.pow(2, 4);
    }

    public static int binomialCoefficient(int n, int k) {
        int res = 1;
        for (int i = 1; i <= k; i++) {
            res = res * (n - i + 1) / i;
        }
        return res;
    }
}