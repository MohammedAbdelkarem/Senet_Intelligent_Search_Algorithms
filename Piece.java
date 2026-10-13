public class Piece {
    private String id;
    private PlayerColor color;
    private int square;
    private boolean onBoard;
    private int exitCondition = 0;
    private boolean horusFlag = false;

    public Piece(String id, PlayerColor color, int square) {
        this.id = id;
        this.color = color;
        this.square = square;
        this.onBoard = true;
    }

    public String getId() {
        return id;
    }
    public PlayerColor getColor() {
        return color;
    }
    public int getSquare() {
        return square;
    }
    public boolean isOnBoard() {
        return onBoard;
    }
    public void setSquare(int square) {
        this.square = square;
    }
    public void setOnBoard(boolean onBoard) {
        this.onBoard = onBoard;
    }
    public void setExitCondition(int value) {
        this.exitCondition = value;
    }
    public boolean hasExitCondition() {

        return exitCondition > 0;
    }
    public int getExitCondition() {

        return exitCondition;
    }
    public void setHorusFlag(boolean flag) {

        this.horusFlag = flag;
    }

    public boolean getHorusFlag() {
        return horusFlag;
    }



}