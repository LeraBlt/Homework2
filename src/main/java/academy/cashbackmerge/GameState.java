package academy.cashbackmerge;

/** Хранит игровое поле и установленный для игры лимит ходов. */
public class GameState {

    private final Board board;
    private final int movesLimit;

    public GameState(Board board, int movesLimit) {
        if (board == null) {
            throw new IllegalArgumentException("Игровое поле не должно быть null");
        }
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("Максимальное число ходов должно быть больше нуля");
        }

        this.board = board;
        this.movesLimit = movesLimit;
    }

    public Board board() {
        return board;
    }

    public int movesLimit() {
        return movesLimit;
    }
}
