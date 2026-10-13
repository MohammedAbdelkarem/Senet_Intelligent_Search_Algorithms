import java.util.*;

public class Gamelogic {

    private final Scanner sc;
    private final Sticks sticks;
    private final Player white;
    private final Player black;
    private final Board board;
    private final GameState game;

    public Gamelogic() {
        sc = new Scanner(System.in);
        sticks = new Sticks();
        white = new Player(PlayerColor.WHITE);
        black = new Player(PlayerColor.BLACK);
        board = new Board();

        // Initial piece placement
        int square = 1;
        for (int i = 1; i <= 7; i++) {
            Piece w = new Piece("W" + i, PlayerColor.WHITE, square);
            white.addPiece(w);
            board.placePiece(w, square);
            square++;

            Piece b = new Piece("B" + i, PlayerColor.BLACK, square);
            black.addPiece(b);
            board.placePiece(b, square);
            square++;
        }

        game = new GameState(white, black, board);
    }

    // -------------------------------
    // GAME MODES
    // -------------------------------

    public void startHumanVsAI() {
        SenetAI ai = new SenetAI(PlayerColor.BLACK);

        while (!game.isTerminal()) {

            Player current = game.getCurrentPlayer();
            System.out.println("\n Player role: " + current.getColor());

            if (!waitForThrow())
                continue;

            int[] sticksResult = sticks.rollDetailed();
            int rollValue = sticks.calculateRoll(sticksResult);

            printSticks(sticksResult);
            System.out.println("The result of the throw : " + rollValue);

            board.printBoard();

            List<Piece> movablePieces = getMovablePieces(current, board, rollValue);
            if (movablePieces.isEmpty()) {
                System.out.println("No possible move.. turn passes");
                game.switchTurn();
                continue;
            }

            boolean movedThisTurn = false;
            boolean usedPendingPiece = false;

            List<Piece> pendingAtStart = getPendingPieces(current);

            // WHITE = Human
            if (current.getColor() == PlayerColor.WHITE) {
                while (!movedThisTurn) {
                    printMovablePieces(movablePieces);

                    Piece chosen = choosePiece(movablePieces);
                    if (chosen == null)
                        continue;

                    if (chosen.hasExitCondition()) {
                        boolean handled = handleSuspendedPiece(board, chosen, rollValue, movablePieces.size() > 1, current, true);
                        if (handled) {
                            movedThisTurn = true;
                            usedPendingPiece = true;
                        }
                        continue;
                    }

                    movedThisTurn = handleNormalMove(board, chosen, rollValue, current, true);
                }
            }
            // BLACK = AI
            else {
                Piece chosen = ai.chooseAIMove(game, rollValue);
                if (chosen == null) {
                    System.out.println("AI has no move.. turn passes");
                    game.switchTurn();
                    continue;
                }

                printMovablePieces(movablePieces);

                System.out.println("AI chose: " + chosen.getId());

                if (chosen.hasExitCondition()) {
                    movedThisTurn = handleSuspendedPiece(board, chosen, rollValue, movablePieces.size() > 1, current, true);
                    usedPendingPiece = movedThisTurn;
                } else {
                    movedThisTurn = handleNormalMove(board, chosen, rollValue, current, true);
                }
            }

            if (movedThisTurn && !usedPendingPiece) {
                returnPendingPieces(board, pendingAtStart, true);
            }

            printCounters();
            game.switchTurn();
        }

        System.out.println("\n Game over! Winner: " +
                (game.getWinner() != null ? game.getWinner().getColor() : "No one"));
    }

    // Start game (Human vs Human)
    public void startPlayerVsPlayer() {

        while (!game.isTerminal()) {

            Player current = game.getCurrentPlayer();
            System.out.println("\n Player role: " + current.getColor());

            if (!waitForThrow())
                continue;

            int[] sticksResult = sticks.rollDetailed();
            int rollValue = sticks.calculateRoll(sticksResult);

            printSticks(sticksResult);
            System.out.println("The result of the throw : " + rollValue);

            board.printBoard();

            List<Piece> movablePieces = getMovablePieces(current, board, rollValue);
            if (movablePieces.isEmpty()) {
                System.out.println("No possible move.. turn passes");
                game.switchTurn();
                continue;
            }

            boolean movedThisTurn = false;
            boolean usedPendingPiece = false;

            List<Piece> pendingAtStart = getPendingPieces(current);

            while (!movedThisTurn) {

                printMovablePieces(movablePieces);

                Piece chosen = choosePiece(movablePieces);
                if (chosen == null)
                    continue;

                // Suspended pieces (28/29/30 logic)
                if (chosen.hasExitCondition()) {
                    boolean handled = handleSuspendedPiece(board, chosen, rollValue, movablePieces.size() > 1, current, true);
                    if (handled) {
                        movedThisTurn = true;
                        usedPendingPiece = true;
                    }
                    continue;
                }

                // Normal move
                movedThisTurn = handleNormalMove(board, chosen, rollValue, current, true);
            }

            // Return pending pieces if none of them was used this turn
            if (!usedPendingPiece) {
                returnPendingPieces(board, pendingAtStart, true);
            }

            printCounters();
            game.switchTurn();
        }

        System.out.println("\n Game over! Winner: " +
                (game.getWinner() != null ? game.getWinner().getColor() : "No one"));
    }

    // Throw command (currently always allowed)
    private boolean waitForThrow() {
        // System.out.print("Type \"throw\" to throw the sticks: ");
        // String cmd = sc.nextLine().trim();
        // if (!cmd.equalsIgnoreCase("throw")) {
        // System.out.println("You must type \"throw\".");
        // return false;
        // }
        return true;

    }

    // Print sticks
    private void printSticks(int[] sticksResult) {
        System.out.print("Sticks: ");
        for (int s : sticksResult) {
            System.out.print(s == 1 ? "[X] " : "[ ] ");
        }
        System.out.println();
    }

    // Get movable pieces
    public static List<Piece> getMovablePieces(Player player, Board board, int rollValue) {
        List<Piece> movable = new ArrayList<>();

        for (Piece p : player.getPieces()) {
            if (!p.isOnBoard())
                continue;

            if (p.hasExitCondition()) {
                int c = p.getExitCondition();
                if ((c == 3 && rollValue == 3) ||
                        (c == 2 && rollValue == 2) ||
                        (c == 1)) {
                    movable.add(p);
                }
                continue;
            }

            int newSquare = p.getSquare() + rollValue;

            if (p.getSquare() < 26 && newSquare > 26)
                continue;

            Piece target = board.getPiece(newSquare);
            if (target != null && target.getColor() == p.getColor())
                continue;

            movable.add(p);
        }

        return movable;
    }

    // Print movable pieces
    private void printMovablePieces(List<Piece> movable) {
        System.out.println("Pieces that can be moved:");
        for (Piece p : movable) {
            String tag = p.hasExitCondition() ? " (suspended)" : "";
            System.out.println("- " + p.getId() + " at house " + p.getSquare() + tag);
        }
    }

    // Choose a piece
    private Piece choosePiece(List<Piece> movable) {
        System.out.print("Choose a piece: ");
        String id = sc.nextLine().trim();

        for (Piece p : movable) {
            if (p.getId().equalsIgnoreCase(id))
                return p;
        }

        System.out.println("Incorrect choice.");
        return null;
    }

    // -------------------------------
    // RULES (shared by human mode and AI simulation)
    // -------------------------------

    public static boolean handleSuspendedPiece(
            Board board,
            Piece chosen,
            int rollValue,
            boolean hasAlternativeMove,
            Player current,
            boolean verbose
    ) {

        int condition = chosen.getExitCondition();

        if (condition == 3) { // THREE TRUTHS
            if (rollValue == 3) {
                exitPiece(board, chosen, current, verbose);
                return true;
            }
            return suspendedFallback(board, chosen, hasAlternativeMove, verbose);
        }

        if (condition == 2) { // RE-ATOUM
            if (rollValue == 2) {
                exitPiece(board, chosen, current, verbose);
                return true;
            }
            return suspendedFallback(board, chosen, hasAlternativeMove, verbose);
        }

        if (condition == 1) { // HORUS
            exitPiece(board, chosen, current, verbose);
            return true;
        }

        return false;
    }

    private static boolean suspendedFallback(
            Board board,
            Piece chosen,
            boolean hasAlternativeMove,
            boolean verbose
    ) {
        if (hasAlternativeMove) {
            if (verbose) {
                System.out.println("This piece needs a specific throw. Choose another.");
            }
            return false;
        }

        int rebirth = findAvailableBefore(board, 15);
        board.removePiece(chosen.getSquare());
        board.placePiece(chosen, rebirth);
        chosen.setExitCondition(0);
        chosen.setHorusFlag(false);

        if (verbose) {
            System.out.println("Piece " + chosen.getId() + " returned to Rebirth.");
        }
        return false;
    }

    public static boolean handleNormalMove(
            Board board,
            Piece chosen,
            int rollValue,
            Player current,
            boolean verbose
    ) {

        int oldSquare = chosen.getSquare();
        int newSquare = oldSquare + rollValue;

        if (oldSquare < 26 && newSquare > 26) {
            if (verbose) {
                System.out.println("Cannot bypass House of Happiness.");
            }
            return false;
        }

        if (newSquare > 30) {
            exitPiece(board, chosen, current, verbose);
            return true;
        }

        if (newSquare == 30) {
            board.removePiece(oldSquare);
            board.placePiece(chosen, newSquare);
            chosen.setExitCondition(1);
            chosen.setHorusFlag(true);
            if (verbose) {
                System.out.println("Piece " + chosen.getId() + " reached Horus.");
            }
            return true;
        }

        Piece target = board.getPiece(newSquare);

        if (target != null) {
            if (target.getColor() == chosen.getColor()) {
                if (verbose) {
                    System.out.println("Square occupied by same color.");
                }
                return false;
            }

            board.removePiece(oldSquare);
            board.removePiece(newSquare);
            board.placePiece(chosen, newSquare);
            board.placePiece(target, oldSquare);

            if (verbose) {
                System.out.println("Exchange between " + chosen.getId() + " and " + target.getId());
            }
            return true;
        }

        board.removePiece(oldSquare);
        board.placePiece(chosen, newSquare);

        SpecialHouseType type = board.getSpecialHouse(newSquare);
        handleSpecialHouse(board, chosen, type, verbose);

        if (verbose) {
            System.out.println("Moved " + chosen.getId() + " to " + newSquare);
        }
        return true;
    }

    public static void handleSpecialHouse(Board board, Piece chosen, SpecialHouseType type, boolean verbose) {

        switch (type) {
            case WATER:
                int rebirth = findAvailableBefore(board, 15);
                board.removePiece(chosen.getSquare());
                board.placePiece(chosen, rebirth);
                chosen.setExitCondition(0);
                chosen.setHorusFlag(false);
                if (verbose) {
                    System.out.println("Returned to Rebirth!");
                }
                break;

            case THREE_TRUTHS:
                chosen.setExitCondition(3);
                if (verbose) {
                    System.out.println("Reached Three Truths.");
                }
                break;

            case RE_ATOUM:
                chosen.setExitCondition(2);
                if (verbose) {
                    System.out.println("Reached Re-Atoum.");
                }
                break;

            case HORUS:
                chosen.setExitCondition(1);
                chosen.setHorusFlag(true);
                if (verbose) {
                    System.out.println("Reached Horus.");
                }
                break;

            default:
                break;
        }
    }

    public static void returnPendingPieces(Board board, List<Piece> pending, boolean verbose) {
        for (Piece p : pending) {
            if (p.hasExitCondition() && p.isOnBoard()) {
                int rebirth = findAvailableBefore(board, 15);
                board.removePiece(p.getSquare());
                board.placePiece(p, rebirth);
                p.setExitCondition(0);
                p.setHorusFlag(false);
                if (verbose) {
                    System.out.println("Piece " + p.getId() + " returned to Rebirth (was suspended).");
                }
            }
        }
    }

    // Exit counters
    private void printCounters() {
        System.out.println("White counter: " + white.getExitedCount() +
                " | Black counter: " + black.getExitedCount());
    }

    // Find an empty square before a given square (used for Rebirth placement)
    public static int findAvailableBefore(Board board, int start) {
        for (int i = start; i >= 1; i--) {
            if (board.getPiece(i) == null)
                return i;
        }
        return start;
    }

    public static void exitPiece(Board board, Piece chosen, Player current, boolean verbose) {
        current.markExited(chosen);
        board.removePiece(chosen.getSquare());
        chosen.setOnBoard(false);
        chosen.setSquare(0);
        chosen.setExitCondition(0);
        chosen.setHorusFlag(false);

        if (verbose) {
            System.out.println("Piece Exit " + chosen.getId() + "!");
        }
    }

    public static List<Piece> getPendingPieces(Player player) {
        List<Piece> pending = new ArrayList<>();
        for (Piece p : player.getPieces()) {
            if (p.isOnBoard() && p.hasExitCondition()) {
                pending.add(p);
            }
        }
        return pending;
    }
}