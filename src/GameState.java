public class GameState {
    private final Board board;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private Player currentPlayer;

    public GameState(Player whitePlayer, Player blackPlayer, Board board) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.board = board;
        this.currentPlayer = whitePlayer;
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
    }

    public boolean isTerminal() {
        return whitePlayer.hasWon() || blackPlayer.hasWon();
    }

    public Player getWinner() {
        if (whitePlayer.hasWon()) return whitePlayer;
        if (blackPlayer.hasWon()) return blackPlayer;
        return null;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Piece findPiece(String id) {
        for (Piece p : whitePlayer.getPieces()) {
            if (p.getId().equals(id)) return p;
        }
        for (Piece p : blackPlayer.getPieces()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public GameState deepCopy() {
        Board newBoard = new Board();

        Player newWhite = new Player(PlayerColor.WHITE);
        Player newBlack = new Player(PlayerColor.BLACK);

        // Copy pieces (white)
        for (Piece p : whitePlayer.getPieces()) {
            Piece np = new Piece(p.getId(), p.getColor(), p.getSquare());
            np.setOnBoard(p.isOnBoard());
            np.setExitCondition(p.getExitCondition());
            np.setHorusFlag(p.getHorusFlag());
            newWhite.addPiece(np);

            if (p.isOnBoard()) {
                newBoard.placePiece(np, p.getSquare());
            }
        }

        // Copy pieces (black)
        for (Piece p : blackPlayer.getPieces()) {
            Piece np = new Piece(p.getId(), p.getColor(), p.getSquare());
            np.setOnBoard(p.isOnBoard());
            np.setExitCondition(p.getExitCondition());
            np.setHorusFlag(p.getHorusFlag());
            newBlack.addPiece(np);

            if (p.isOnBoard()) {
                newBoard.placePiece(np, p.getSquare());
            }
        }

        // Copy exited counters (used by evaluation)
        newWhite.setExitedCount(whitePlayer.getExitedCount());
        newBlack.setExitedCount(blackPlayer.getExitedCount());

        GameState copy = new GameState(newWhite, newBlack, newBoard);

        // Copy turn
        if (this.getCurrentPlayer().getColor() == PlayerColor.BLACK) {
            copy.switchTurn();
        }

        return copy;
    }



}