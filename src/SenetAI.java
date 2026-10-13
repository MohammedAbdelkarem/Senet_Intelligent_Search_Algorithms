import java.util.List;
import java.util.ArrayList;

public class SenetAI {

    private final PlayerColor aiColor;
    private final Strategies strategies;
    private final boolean DEBUG = true;


    private void debug(int depth, String message) {
        if (!DEBUG) return;
        String indent = "    ".repeat(depth);
        System.out.println(indent + message);
    }


    public SenetAI(PlayerColor aiColor) {
        this.aiColor = aiColor;
        this.strategies = new Strategies(aiColor);
    }

    // -------------------------------
    // PUBLIC: choose the best move
    // -------------------------------
    public Piece chooseAIMove(GameState state, int rollValue) {
        
        List<Piece> movable = Gamelogic.getMovablePieces(
                state.getCurrentPlayer(),
                state.getBoard(),
                rollValue);

        List<String> summary = new ArrayList<>();// to store the summary od scores

        Piece bestPiece = null;
        int bestValue = Integer.MIN_VALUE;

        debug(0, "=== AI TURN ===");
        for (Piece p : movable) {
            MoveInfo info = new MoveInfo();
            GameState next = simulateMove(state, p, rollValue , info);


            int value = expectiminimax(next, 0, false , info); // false = opponent turn


            summary.add(
                "Move " + p.getId() +
                " from " + p.getSquare() +
                " → " + (p.getSquare() + rollValue) +
                (info.swappedOpponent != null ? " | swap " + info.swappedOpponent.getId() : "") +
                " | score = " + value
            );



            if (value > bestValue || (value == bestValue && p.getSquare() > bestPiece.getSquare())) {
                bestValue = value;
                bestPiece = p;
            }

        }

        System.out.println("\n=== FINAL MOVE SCORES ===");
        for (String line : summary) {
            System.out.println(line);
        }
        System.out.println("==========================\n");


        return bestPiece;
    }

    // -------------------------------
    // EXPECTIMINIMAX (depth 2)
    // -------------------------------

    private int expectiminimax(GameState state, int depth, boolean isAITurn, MoveInfo rootInfo) {

        // LEAF
        if (depth == 2 || state.isTerminal()) {
            int eval = evaluate(state, rootInfo);
            printNode(depth, "\\__ ", "[LEAF] eval = " + eval);
            return eval;
        }

        Player current = state.getCurrentPlayer();

        // MAX node
        if (isAITurn) {
            printNode(depth, "\\__ ", "[MAX] AI turn");

            int best = Integer.MIN_VALUE;

            for (int roll = 1; roll <= 5; roll++) {
                double prob = Sticks.probabilityOfRoll(roll);
                printNode(depth, "|-- ", "Roll " + roll + " (p=" + prob + ")");

                List<Piece> moves = Gamelogic.getMovablePieces(current, state.getBoard(), roll);

                if (moves.isEmpty()) {
                    printNode(depth + 1, "\\__ ", "No moves → pass");
                    GameState passed = state.deepCopy();
                    passed.switchTurn();
                    int value = expectiminimax(passed, depth + 1, false, rootInfo);
                    best = Math.max(best, (int)(value * prob));
                } else {
                    for (int i = 0; i < moves.size(); i++) {
                        Piece p = moves.get(i);
                        MoveInfo dummy = new MoveInfo();
                        GameState next = simulateMove(state, p, roll, dummy);

                        boolean last = (i == moves.size() - 1);
                        String branch = last ? "\\__ " : "|-- ";

                        printNode(depth + 1, branch, "Move " + moveString(p, roll, dummy));

                        int value = expectiminimax(next, depth + 1, false, dummy);
                        printNode(depth + 1, "    \\__ ", "value = " + value);

                        best = Math.max(best, (int)(value * prob));
                    }
                }
            }

            printNode(depth, "\\__ ", "[MAX] best = " + best);
            return best;
        }

        // MIN node
        else {
            printNode(depth, "\\__ ", "[MIN] Opponent turn");

            int worst = Integer.MAX_VALUE;

            for (int roll = 1; roll <= 5; roll++) {
                double prob = Sticks.probabilityOfRoll(roll);
                printNode(depth, "|-- ", "Roll " + roll + " (p=" + prob + ")");

                List<Piece> moves = Gamelogic.getMovablePieces(current, state.getBoard(), roll);

                if (moves.isEmpty()) {
                    printNode(depth + 1, "\\__ ", "No moves → pass");
                    GameState passed = state.deepCopy();
                    passed.switchTurn();
                    int value = expectiminimax(passed, depth + 1, true, rootInfo);
                    worst = Math.min(worst, (int)(value * prob));
                } else {
                    for (int i = 0; i < moves.size(); i++) {
                        Piece p = moves.get(i);
                        MoveInfo dummy = new MoveInfo();
                        GameState next = simulateMove(state, p, roll, dummy);

                        boolean last = (i == moves.size() - 1);
                        String branch = last ? "\\__ " : "|-- ";

                        printNode(depth + 1, branch, "Move " + moveString(p, roll, dummy));

                        int value = expectiminimax(next, depth + 1, true, dummy);
                        printNode(depth + 1, "    \\__ ", "value = " + value);

                        worst = Math.min(worst, (int)(value * prob));
                    }
                }
            }

            printNode(depth, "\\__ ", "[MIN] worst = " + worst);
            return worst;
        }
    }




    private int evaluate(GameState state, MoveInfo info) {

        int score = 0;

        // 1. Exits
        score += strategies.scoreBaseExit(state);

        // 2. Swap priority 
        score += strategies.scoreSwap(info);

        // 3. Target 24 strategy
        score += strategies.scoreTarget24(state, info);

        // 4. Reach Happiness (26)
        score += strategies.scoreReachHappiness(state, info);

        // 5. Advancement
        // score += strategies.scoreAdvancementTowardHappiness(state);

        // 6. Water House avoiding
        score += strategies.scoreWaterHouse(state, info);

        // 7. Horus House
        score += strategies.scoreHorusHouse(state, info);

        // 8. From Happiness House (26): move to Horus or Re-Atum
        score += strategies.scoreFromHappiness(state, info);

        // 9. From Re-Atum House (29): move when 2 is rolled
        score += strategies.scoreReAtumHouse(state, info);

        // 10. From Three Truths House (28): move when 2 is rolled
        score += strategies.scoreThreeTruthsHouse(state, info);

        // 11. Leave Happiness when result is 5
        score += strategies.scoreLeaveHapinessWhen5(state, info);

        return score;
    }



    private GameState simulateMove(GameState original, Piece piece, int rollValue, MoveInfo info) {

        GameState copy = original.deepCopy();
        Board board = copy.getBoard();
        Player current = copy.getCurrentPlayer();

        info.movedPiece = copy.findPiece(piece.getId());
        info.rollValue = rollValue;
        info.originalSquare = info.movedPiece.getSquare();
        info.swappedOpponent = null;

        int oldSquare = info.movedPiece.getSquare();
        int newSquare = oldSquare + rollValue;

        // Detect swap BEFORE applying move
        if (newSquare >= 1 && newSquare <= 30) {
            Piece target = board.getPiece(newSquare);
            if (target != null && target.getColor() != info.movedPiece.getColor()) {
                info.swappedOpponent = target;
            }
        }

        // Apply the real move
        boolean usedPending = info.movedPiece.hasExitCondition();
        boolean moved;

        if (usedPending) {
            List<Piece> moves = Gamelogic.getMovablePieces(current, board, rollValue);
            moved = Gamelogic.handleSuspendedPiece(board, info.movedPiece, rollValue, moves.size() > 1, current, false);
        } else {
            moved = Gamelogic.handleNormalMove(board, info.movedPiece, rollValue, current, false);
        }

        if (moved && !usedPending) {
            List<Piece> pending = Gamelogic.getPendingPieces(current);
            Gamelogic.returnPendingPieces(board, pending, false);
        }

        copy.switchTurn();
        return copy;
    }

    private void printNode(int depth, String prefix, String message) {
        if (!DEBUG) return;

        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("|   ");
        }

        System.out.println(indent.toString() + prefix + message);
    }

    private String moveString(Piece p, int roll, MoveInfo info) {
        int oldSq = info.originalSquare;
        int newSq = oldSq + roll;
        String swap = (info.swappedOpponent != null)
                ? " swap(" + info.swappedOpponent.getId() + ")"
                : "";
        return p.getId() + " " + oldSq + " --> " + newSq + " r=" + roll + swap;
    }


}
