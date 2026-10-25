
import java.util.*;

// 1. Main game controller
class TicTacToe {
    private final Board board;
    private final Player[] players;
    private int currentPlayerIndex;
    private GameStatus status;

    public TicTacToe(Player p1, Player p2) {
        board = new Board();
        players = new Player[]{p1, p2};
        currentPlayerIndex = 0;
        status = GameStatus.IN_PROGRESS;
    }

    public boolean makeMove(int row, int col) {
        if (status != GameStatus.IN_PROGRESS) return false;

        Player current = players[currentPlayerIndex];

        if (!board.placeSymbol(row, col, current.getSymbol())) {
            return false;
        }

        if (board.hasWinner(row, col, current.getSymbol())) {
            status = GameStatus.WON;
            System.out.println(current.getName() + " wins!");
        } else if (board.isFull()) {
            status = GameStatus.DRAW;
            System.out.println("Game is a draw!");
        } else {
            currentPlayerIndex = 1 - currentPlayerIndex;
        }

        return true;
    }

    public void printBoard() {
        board.print();
    }

    public GameStatus getStatus() {
        return status;
    }
}

// 2. Board: stores symbols and checks game state
class Board {
    private final Symbol[][] grid = new Symbol[3][3];
    private int moves = 0;

    public boolean placeSymbol(int row, int col, Symbol symbol) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            return false;
        }

        if (grid[row][col] != null) {
            return false;
        }

        grid[row][col] = symbol;
        moves++;
        return true;
    }

    public boolean hasWinner(int row, int col, Symbol symbol) {
        boolean rowWin = true;
        boolean colWin = true;

        for (int i = 0; i < 3; i++) {
            if (grid[row][i] != symbol) rowWin = false;
            if (grid[i][col] != symbol) colWin = false;
        }

        if (rowWin || colWin) return true;

        boolean diagonalWin = true;
        boolean antiDiagonalWin = true;

        for (int i = 0; i < 3; i++) {
            if (grid[i][i] != symbol) diagonalWin = false;
            if (grid[i][2 - i] != symbol) antiDiagonalWin = false;
        }

        return diagonalWin || antiDiagonalWin;
    }

    public boolean isFull() {
        return moves == 9;
    }

    public void print() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(
                    (grid[i][j] == null ? "-" : grid[i][j]) + " "
                );
            }
            System.out.println();
        }
    }
}

// 3. Player
class Player {
    private final String name;
    private final Symbol symbol;

    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public Symbol getSymbol() {
        return symbol;
    }
}

// 4. Symbols
enum Symbol {
    X, O
}

// 5. Game lifecycle
enum GameStatus {
    IN_PROGRESS,
    WON,
    DRAW
}

// 6. Example usage
public class Main {
    public static void main(String[] args) {
        Player p1 = new Player("Alice", Symbol.X);
        Player p2 = new Player("Bob", Symbol.O);

        TicTacToe game = new TicTacToe(p1, p2);

        game.makeMove(0, 0);
        game.makeMove(1, 0);
        game.makeMove(0, 1);
        game.makeMove(1, 1);
        game.makeMove(0, 2);

        game.printBoard();
        System.out.println(game.getStatus());
    }
}
