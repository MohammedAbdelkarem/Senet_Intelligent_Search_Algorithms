public class Board {
    private Piece[] squares;

    public Board() {
        squares = new Piece[30];
    }

    public void placePiece(Piece piece, int square) {
        if (square < 1 || square > 30) {
            throw new IllegalArgumentException("Square must be between 1 and 30");
        }
        squares[square - 1] = piece;
        piece.setSquare(square);
        piece.setOnBoard(true);
    }

    public void removePiece(int square) {
        if (square < 1 || square > 30) return;
        Piece p = squares[square - 1];
        if (p != null) {
            p.setOnBoard(false);
            p.setSquare(0);
        }
        squares[square - 1] = null;
    }

    public Piece getPiece(int square) {
        if (square < 1 || square > 30) return null;
        Piece p = squares[square - 1];
        if (p != null && !p.isOnBoard())
        {
            return null;
        }
        return p;
    }



    public SpecialHouseType getSpecialHouse(int square) {
        switch (square) {
            case 15: return SpecialHouseType.REBIRTH;
            case 26: return SpecialHouseType.HAPPINESS;
            case 27: return SpecialHouseType.WATER;
            case 28: return SpecialHouseType.THREE_TRUTHS;
            case 29: return SpecialHouseType.RE_ATOUM;
            case 30: return SpecialHouseType.HORUS;
            default: return SpecialHouseType.NONE;
        }
    }
    public void printBoard() {
        System.out.println("\n=== The Board ===");

        for (int i = 1; i <= 10; i++) {
            System.out.print(formatSquare(i));
        }
        System.out.println();

        for (int i = 20; i >= 11; i--) {
            System.out.print(formatSquare(i));
        }
        System.out.println();

        for (int i = 21; i <= 30; i++) {
            System.out.print(formatSquare(i));
        }
        System.out.println();
    }

    private String formatSquare(int square) {
        Piece p = getPiece(square);
        String content = (p == null) ? "." : p.getId();

        String marker;
        switch (getSpecialHouse(square)) {
            case REBIRTH:       marker = "R";  break;
            case HAPPINESS:     marker = "H";  break;
            case WATER:         marker = "W";  break;
            case THREE_TRUTHS:  marker = "T3"; break;
            case RE_ATOUM:      marker = "RA"; break;
            case HORUS:         marker = "HO"; break;
            default:            marker = "  "; break;
        }

        return String.format("[%-2d:%-3s%-3s] ", square, content, marker);
    }

}