import java.util.*;

enum Direction {
    UP, DOWN, LEFT, RIGHT
}

class Position {
    int row, col;

    Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position p = (Position) obj;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}

class Snake {
    Deque<Position> body = new LinkedList<>();
    Set<Position> occupied = new HashSet<>();
    Direction direction = Direction.RIGHT;

    Snake(Position start) {
        body.addFirst(start);
        occupied.add(start);
    }

    Position getHead() {
        return body.peekFirst();
    }

    void changeDirection(Direction newDirection) {
        if (direction == Direction.UP && newDirection == Direction.DOWN) return;
        if (direction == Direction.DOWN && newDirection == Direction.UP) return;
        if (direction == Direction.LEFT && newDirection == Direction.RIGHT) return;
        if (direction == Direction.RIGHT && newDirection == Direction.LEFT) return;

        direction = newDirection;
    }
}

class SnakeGame {
    private int rows, cols, score = 0;
    private Snake snake;
    private Position food;
    private boolean gameOver = false;
    private Random random = new Random();

    SnakeGame(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        snake = new Snake(new Position(rows / 2, cols / 2));
        generateFood();
    }

    private Position nextPosition() {
        Position head = snake.getHead();
        int r = head.row, c = head.col;

        switch (snake.direction) {
            case UP:    r--; break;
            case DOWN:  r++; break;
            case LEFT:  c--; break;
            case RIGHT: c++; break;
        }

        return new Position(r, c);
    }

    public void move() {
        if (gameOver) return;

        Position next = nextPosition();
        boolean eating = next.equals(food);

        if (next.row < 0 || next.row >= rows ||
            next.col < 0 || next.col >= cols) {
            gameOver = true;
            return;
        }

        // Tail moves away unless the snake is eating.
        Position tail = snake.body.peekLast();

        if (snake.occupied.contains(next) &&
            (eating || !next.equals(tail))) {
            gameOver = true;
            return;
        }

        snake.body.addFirst(next);
        snake.occupied.add(next);

        if (eating) {
            score++;
            generateFood();
        } else {
            Position removed = snake.body.removeLast();
            snake.occupied.remove(removed);
        }
    }

    private void generateFood() {
        if (snake.occupied.size() == rows * cols) {
            gameOver = true;
            return;
        }

        Position p;
        do {
            p = new Position(random.nextInt(rows), random.nextInt(cols));
        } while (snake.occupied.contains(p));

        food = p;
    }

    public void changeDirection(Direction direction) {
        if (!gameOver) snake.changeDirection(direction);
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void printBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Position p = new Position(r, c);

                if (snake.getHead().equals(p))
                    System.out.print("H ");
                else if (snake.occupied.contains(p))
                    System.out.print("S ");
                else if (food != null && food.equals(p))
                    System.out.print("F ");
                else
                    System.out.print(". ");
            }
            System.out.println();
        }

        System.out.println("Score: " + score);
    }
}

public class Main {
    public static void main(String[] args) {
        SnakeGame game = new SnakeGame(10, 10);

        game.printBoard();

        game.changeDirection(Direction.DOWN);
        game.move();
        game.move();

        game.printBoard();
        System.out.println("Game over: " + game.isGameOver());
    }
}