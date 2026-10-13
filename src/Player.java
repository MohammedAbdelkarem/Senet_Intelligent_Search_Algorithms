import java.util.ArrayList;
import java.util.List;

public class Player
{
    private final PlayerColor color;
    private final List<Piece> pieces;
    private int exitedCount;

    public Player(PlayerColor color) {
        this.color = color;
        this.pieces = new ArrayList<>();
        this.exitedCount = 0;
    }

    public PlayerColor getColor() {
        return color;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public int getExitedCount() {
        return exitedCount;
    }

    public void setExitedCount(int exitedCount) {
        this.exitedCount = exitedCount;
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    public void markExited(Piece piece) {
        piece.setSquare(0);
        exitedCount++;
    }

    public boolean hasWon() {
        return exitedCount == pieces.size();
    }
}