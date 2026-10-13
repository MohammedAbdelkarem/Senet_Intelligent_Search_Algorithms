import java.util.List;

public class Strategies {

    private final PlayerColor aiColor;

    public Strategies(PlayerColor aiColor) {
        this.aiColor = aiColor;
    }

    // ---------------------------------------------------------
    // 1. Base scoring: exited pieces difference
    // ---------------------------------------------------------
    public int scoreBaseExit(GameState state) {
        Player ai = (aiColor == PlayerColor.WHITE)
                ? state.getWhitePlayer()
                : state.getBlackPlayer();

        Player opponent = (aiColor == PlayerColor.WHITE)
                ? state.getBlackPlayer()
                : state.getWhitePlayer();

        // Weight exits strongly
        return (ai.getExitedCount() - opponent.getExitedCount()) * 150;
    }

    // ---------------------------------------------------------
    // 2. Swap priority
    // ---------------------------------------------------------

    public int scoreSwap(MoveInfo info) {
        if (info == null || info.swappedOpponent == null)
            return 0;

        int oppIndex = info.swappedOpponent.getSquare();

        // Your original formula
        return 200 + oppIndex * 5;
    }


    // ---------------------------------------------------------
    // 3. Advancement toward Happiness (26)
    //    If no swaps exist, reward the closest piece to 26
    // ---------------------------------------------------------
    public int scoreAdvancementTowardHappiness(GameState state) {
        Player ai = (aiColor == PlayerColor.WHITE)
                ? state.getWhitePlayer()
                : state.getBlackPlayer();

        int best = Integer.MIN_VALUE;

        for (Piece p : ai.getPieces()) {
            int sq = p.getSquare();

            if (sq < 1 || sq > 20) continue;

            int distance = 26 - sq;
            int score = 100 - distance; // closer = better

            if (score > best) best = score;
        }

        return best == Integer.MIN_VALUE ? 0 : best;
    }

    // ---------------------------------------------------------
    // 4. Target 24 strategy
    // ---------------------------------------------------------
    public int scoreTarget24(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;
        int newSq = oldSq + info.rollValue; // we need rollValue in MoveInfo

        // Rule A: If move lands on 24 → big reward
        if (newSq == 24) {
            return 1000; // strong reward
        }

        // in order to get to 24, the piece must be on 22
        if(newSq == 22) {
            return 400;
        }

        if(newSq == 20 || newSq == 18 || newSq == 16) {
            return 200;
        }

        // Rule B: If piece is already on 24 → only move if roll = 2
        if (oldSq == 24 && info.rollValue != 2) {
            return -500; // huge penalty to prevent moving it
        }

        if (oldSq > 18 && oldSq <= 23 && newSq != 24) {
            return -200; // punish ignoring the chance
        }


        return 0;
    }

    
    // ---------------------------------------------------------
    // 5. Reach Happiness (26)
    // ---------------------------------------------------------
    public int scoreReachHappiness(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null)
            return 0;

        int oldSq = info.originalSquare;
        int newSq = oldSq + info.rollValue;

        // Only consider squares 21–25
        if (oldSq > 20 && oldSq <= 25) {
            if (newSq == 26) {
                // Highest priority: reaching the happiness house
                return 5000; // huge reward to override all other strategies
            }
        }

        return 0;
    }

    // ---------------------------------------------------------
    // 6. Water House avoiding
    // ---------------------------------------------------------

    public int scoreWaterHouse(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;
        int newSq = oldSq + info.rollValue;

        // Punish ANY move that leads to the Water House (27)
        if (newSq == 27) {
            return -5000; 
        }

        return 0;
    }

    // ---------------------------------------------------------
    // 7. Hours moving
    // ---------------------------------------------------------
    public int scoreHorusHouse(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;
        int newSq = oldSq + info.rollValue;

        // Reward ANY move that lands on the Horus House (30)
        if (oldSq == 30) {
            return 10000; 
        }

        return 0;
    }

    // ---------------------------------------------------------
    // 8. From Happiness House (26): move to Horus or Re-Atum
    // ---------------------------------------------------------
    public int scoreFromHappiness(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;   // always use original square
        int roll = info.rollValue;
        int newSq = oldSq + roll;

        // Only apply this logic if the piece is on the Happiness House (26)
        if (oldSq != 26) return 0;

        // Rule 1: Roll = 4 → move to Horus (30)
        if (roll == 4 && newSq == 30) {
            return 10000;   // very strong reward
        }

        // Rule 2: Roll = 3 → move to Re-Atum (29)
        if (roll == 3 && newSq == 29) {
            return 3000;   // strong reward
        }
        
        return 0;
    }
    // ---------------------------------------------------------
    // 9. From Re-Atum (29): move when 2 is rolled
    // ---------------------------------------------------------
    public int scoreReAtumHouse(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;   // always use original square
        int roll = info.rollValue;
        int newSq = oldSq + roll;

        if(oldSq == 29 && roll == 2) {
            return 10000;
        }
        
        return 0;
    }

    // ---------------------------------------------------------
    // 10. From Three Truths (28): move when 2 is rolled
    // ---------------------------------------------------------
    public int scoreThreeTruthsHouse(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;   // always use original square
        int roll = info.rollValue;
        int newSq = oldSq + roll;

        if(oldSq == 28 && roll == 2) {
            return 10000;
        }
        
        return 0;
    }
    // ---------------------------------------------------------
    // 11. get out when result is 5
    // ---------------------------------------------------------
    public int scoreLeaveHapinessWhen5(GameState state, MoveInfo info) {
        if (info == null || info.movedPiece == null) return 0;

        int oldSq = info.originalSquare;   // always use original square
        int roll = info.rollValue;
        int newSq = oldSq + roll;

        if(oldSq == 26 && roll == 5) {
            return 10000;
        }
        
        return 0;
    }


}
