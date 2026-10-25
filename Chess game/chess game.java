//The core flow is: initialize game
//  → validate move → execute move → check check/checkmate → switch turn.

import java.util.*;

// 1. Main game controller
class ChessGame {
    private final Board board;
    private final Player white;
    private final Player black;
    private Color currentTurn;
    private boolean gameOver;

    public ChessGame(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.board = new Board();
        this.currentTurn = Color.WHITE;
        this.gameOver = false;
    }

    public boolean makeMove(Move move) {
        if (gameOver) return false;

        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());

        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }

        if (!piece.canMove(board, move.getFromRow(), move.getFromCol(),
                           move.getToRow(), move.getToCol())) {
            return false;
        }

        Piece captured = board.getPiece(
                move.getToRow(), move.getToCol());

        board.movePiece(move);

        if (board.isKingInCheck(currentTurn)) {
            board.undoMove(move, piece, captured);
            return false;
        }

        Color opponent = (currentTurn == Color.WHITE)
                ? Color.BLACK : Color.WHITE;

        if (board.isCheckmate(opponent)) {
            gameOver = true;
            System.out.println(opponent + " is checkmated!");
        } else {
            currentTurn = opponent;
        }

        return true;
    }

    public void printBoard() {
        board.print();
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }
}

// 2. Board: stores pieces and manages movement
class Board {
    private final Piece[][] grid = new Piece[8][8];

    public Board() {
        initialize();
    }

    private void initialize() {
        for (int col = 0; col < 8; col++) {
            grid[1][col] = new Pawn(Color.BLACK);
            grid[6][col] = new Pawn(Color.WHITE);
        }

        grid[0][0] = new Rook(Color.BLACK);
        grid[0][7] = new Rook(Color.BLACK);
        grid[7][0] = new Rook(Color.WHITE);
        grid[7][7] = new Rook(Color.WHITE);

        grid[0][1] = new Knight(Color.BLACK);
        grid[0][6] = new Knight(Color.BLACK);
        grid[7][1] = new Knight(Color.WHITE);
        grid[7][6] = new Knight(Color.WHITE);

        grid[0][2] = new Bishop(Color.BLACK);
        grid[0][5] = new Bishop(Color.BLACK);
        grid[7][2] = new Bishop(Color.WHITE);
        grid[7][5] = new Bishop(Color.WHITE);

        grid[0][3] = new Queen(Color.BLACK);
        grid[7][3] = new Queen(Color.WHITE);

        grid[0][4] = new King(Color.BLACK);
        grid[7][4] = new King(Color.WHITE);
    }

    public Piece getPiece(int row, int col) {
        if (!inside(row, col)) return null;
        return grid[row][col];
    }

    public void movePiece(Move move) {
        grid[move.getToRow()][move.getToCol()] =
                grid[move.getFromRow()][move.getFromCol()];
        grid[move.getFromRow()][move.getFromCol()] = null;
    }

    public void undoMove(Move move, Piece piece, Piece captured) {
        grid[move.getFromRow()][move.getFromCol()] = piece;
        grid[move.getToRow()][move.getToCol()] = captured;
    }

    public boolean inside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public boolean isKingInCheck(Color color) {
        int kingRow = -1, kingCol = -1;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];

                if (p instanceof King && p.getColor() == color) {
                    kingRow = r;
                    kingCol = c;
                }
            }
        }

        if (kingRow == -1) return true;

        Color opponent = color == Color.WHITE
                ? Color.BLACK : Color.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];

                if (p != null && p.getColor() == opponent
                        && p.canAttack(this, r, c, kingRow, kingCol)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isCheckmate(Color color) {
        if (!isKingInCheck(color)) return false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = grid[r][c];

                if (piece == null || piece.getColor() != color) continue;

                for (int tr = 0; tr < 8; tr++) {
                    for (int tc = 0; tc < 8; tc++) {
                        if (!piece.canMove(this, r, c, tr, tc)) continue;

                        Piece captured = grid[tr][tc];
                        Move move = new Move(r, c, tr, tc);
                        movePiece(move);

                        boolean stillInCheck = isKingInCheck(color);
                        undoMove(move, piece, captured);

                        if (!stillInCheck) return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean pathClear(int fr, int fc, int tr, int tc) {
        int dr = Integer.compare(tr, fr);
        int dc = Integer.compare(tc, fc);

        int r = fr + dr, c = fc + dc;

        while (r != tr || c != tc) {
            if (grid[r][c] != null) return false;
            r += dr;
            c += dc;
        }

        return true;
    }

    public void print() {
        for (Piece[] row : grid) {
            for (Piece p : row) {
                System.out.print(
                    p == null ? ". " : p.getSymbol() + " "
                );
            }
            System.out.println();
        }
    }
}

// 3. Abstract piece: shared movement contract
abstract class Piece {
    protected final Color color;

    public Piece(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public boolean canAttack(Board board, int fr, int fc, int tr, int tc) {
        return canMove(board, fr, fc, tr, tc);
    }

    public abstract boolean canMove(
            Board board, int fr, int fc, int tr, int tc);

    public abstract String getSymbol();

    protected boolean validTarget(
            Board board, int fr, int fc, int tr, int tc) {
        if (!board.inside(tr, tc) || (fr == tr && fc == tc)) {
            return false;
        }

        Piece target = board.getPiece(tr, tc);
        return target == null || target.getColor() != color;
    }
}

// 4. Individual chess pieces
class Pawn extends Piece {
    public Pawn(Color color) { super(color); }

    public boolean canMove(Board b, int fr, int fc, int tr, int tc) {
        if (!b.inside(tr, tc) || (fr == tr && fc == tc)) return false;

        int d = color == Color.WHITE ? -1 : 1;
        Piece target = b.getPiece(tr, tc);

        if (fc == tc && target == null && tr - fr == d) return true;

        int start = color == Color.WHITE ? 6 : 1;
        if (fc == tc && target == null && fr == start
                && tr - fr == 2 * d
                && b.getPiece(fr + d, fc) == null) return true;

        return Math.abs(tc - fc) == 1 && tr - fr == d
                && target != null && target.getColor() != color;
    }

    public boolean canAttack(Board b, int fr, int fc, int tr, int tc) {
        int d = color == Color.WHITE ? -1 : 1;
        return tr - fr == d && Math.abs(tc - fc) == 1;
    }

    public String getSymbol() { return color == Color.WHITE ? "P" : "p"; }
}

class Rook extends Piece {
    public Rook(Color c) { super(c); }

    public boolean canMove(Board b, int fr, int fc, int tr, int tc) {
        return validTarget(b, fr, fc, tr, tc)
                && (fr == tr || fc == tc)
                && b.pathClear(fr, fc, tr, tc);
    }

    public String getSymbol() { return color == Color.WHITE ? "R" : "r"; }
}

class Bishop extends Piece {
    public Bishop(Color c) { super(c); }

    public boolean canMove(Board b, int fr, int fc, int tr, int tc) {
        return validTarget(b, fr, fc, tr, tc)
                && Math.abs(tr - fr) == Math.abs(tc - fc)
                && b.pathClear(fr, fc, tr, tc);
    }

    public String getSymbol() { return color == Color.WHITE ? "B" : "b"; }
}

class Knight extends Piece {
    public Knight(Color c) { super(c); }

    public boolean canMove(Board b, int fr, int fc, int tr, int tc) {
        if (!validTarget(b, fr, fc, tr, tc)) return false;

        int dr = Math.abs(tr - fr), dc = Math.abs(tc - fc);
        return dr * dc == 2;
    }

    public String getSymbol() { return color == Color.WHITE ? "N" : "n"; }
}

class Queen extends Piece {
    public Queen(Color c) { super(c); }

    public boolean canMove(Board b, int fr, int fc, int tr, int tc) {
        boolean straight = fr == tr || fc == tc;
        boolean diagonal = Math.abs(tr - fr) == Math.abs(tc - fc);

        return validTarget(b, fr, fc, tr, tc)
                && (straight || diagonal)
                && b.pathClear(fr, fc, tr, tc);
    }

    public String getSymbol() { return color == Color.WHITE ? "Q" : "q"; }
}

class King extends Piece {
    public King(Color c) { super(c); }

    public boolean canMove(Board b, int fr, int fc, int tr, int tc) {
        return validTarget(b, fr, fc, tr, tc)
                && Math.abs(tr - fr) <= 1
                && Math.abs(tc - fc) <= 1;
    }

    public String getSymbol() { return color == Color.WHITE ? "K" : "k"; }
}

// 5. Move
class Move {
    private final int fromRow, fromCol, toRow, toCol;

    public Move(int fr, int fc, int tr, int tc) {
        fromRow = fr;
        fromCol = fc;
        toRow = tr;
        toCol = tc;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
}

// 6. Player and enums
class Player {
    private final String name;
    private final Color color;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public Color getColor() { return color; }
}

enum Color {
    WHITE, BLACK
}
