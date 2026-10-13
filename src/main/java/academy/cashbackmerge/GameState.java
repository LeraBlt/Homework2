package academy.cashbackmerge;

/** Хранит игровое поле и установленный для игры лимит ходов. */
public class GameState {

    private final Board board;
    private final int movesLimit;
    private int movesPlayed;
    private int score;

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

    public int movesPlayed() {
        return movesPlayed;
    }

    public boolean isFinished() {
        return movesPlayed >= movesLimit;
    }

    public int score() {
        return score;
    }

    /** Регистрирует валидный ход и начисляет очки за слияния. */
    public void recordValidMove(int gainedScore) {
        if (gainedScore < 0) {
            throw new IllegalArgumentException("Количество начисляемых очков не должно быть отрицательным");
        }
        if (isFinished()) {
            throw new IllegalStateException("Лимит ходов уже исчерпан");
        }

        movesPlayed++;
        score += gainedScore;
    }
}
