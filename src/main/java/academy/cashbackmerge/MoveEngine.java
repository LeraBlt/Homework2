package academy.cashbackmerge;

import java.util.Objects;
import java.util.Random;
import org.jspecify.annotations.Nullable;

/** Выполняет ходы и создаёт новую плитку после валидного хода. */
public class MoveEngine {

    private final Random random;
    private final CashbackLevel spawnLevel;

    /** Создаёт движок со случайным генератором и плитками уровня 1. */
    public MoveEngine() {
        this.spawnLevel = CashbackLevel.ONE;
        this.random = new Random();
    }

    /** Создаёт движок, результат работы которого можно повторить по seed. */
    public MoveEngine(CampaignConfig config, long seed) {
        Objects.requireNonNull(config, "Конфиг кампании не должен быть null");

        if (config.spawnRules() == null || config.spawnRules().isEmpty()) {
            throw new IllegalArgumentException("Список spawnRules не должен быть пустым");
        }

        CampaignConfig.SpawnRule firstRule = config.spawnRules().get(0);
        if (firstRule == null || firstRule.cashback() == null) {
            throw new IllegalArgumentException("Первое spawn-правило должно содержать cashback");
        }

        int cashback = Integer.parseInt(firstRule.cashback());
        this.spawnLevel = CashbackLevel.fromPercent(cashback);
        this.random = new Random(seed);
    }

    /** Применяет ход и возвращает его результат. */
    public MoveResult applyMove(GameState state, MoveDirection direction) {
        Objects.requireNonNull(state, "Состояние игры не должно быть null");
        Objects.requireNonNull(direction, "Направление хода не должно быть null");

        if (state.isFinished()) {
            return new MoveResult(false, 0);
        }

        Board board = state.board();
        boolean boardChanged = false;
        int totalScoreGained = 0;

        for (int lineNumber = 0; lineNumber < Board.SIZE; lineNumber++) {
            LineResult result = moveLine(board, lineNumber, direction);
            if (result.changed()) {
                boardChanged = true;
            }
            totalScoreGained += result.scoreGained();
        }

        if (!boardChanged) {
            return new MoveResult(false, 0);
        }

        spawnTile(board);  /* Добавляет одну плитку в случайную пустую клетку*/
        state.recordValidMove(totalScoreGained); /* Сообщает состоянию игры, что ход состоялся.*/
        return new MoveResult(true, totalScoreGained); /* Создаёт результат текущего хода*/
    }

    /** Сдвигает одну строку или колонку и объединяет соседние одинаковые плитки. */
    private LineResult moveLine(Board board, int lineNumber, MoveDirection direction) {
        @Nullable CashbackLevel[] original = readLine(board, lineNumber, direction);
        @Nullable CashbackLevel[] compacted = new CashbackLevel[Board.SIZE];
        int compactedSize = 0;

        // Копируем только непустые клетки, сохраняя их порядок.
        for (int position = 0; position < Board.SIZE; position++) {
            if (original[position] != null) {
                compacted[compactedSize] = original[position];
                compactedSize++;
            }
        }

        @Nullable CashbackLevel[] result = new CashbackLevel[Board.SIZE];
        int resultSize = 0;
        int scoreGained = 0;
        int index = 0;

        // Проходим по плиткам. При слиянии перескакиваем через обе исходные плитки.
        while (index < compactedSize) {
            CashbackLevel current = Objects.requireNonNull(compacted[index]); /* Проверяем, что плитка не null и записываем её в текущую плитку*/

            boolean sameAsNext = index + 1 < compactedSize && current == compacted[index + 1]; /* Следующая и текущая плитка одного уровня*/
            boolean canGrow = current.percent() < CashbackLevel.THIRTY_TWO.percent(); /* Проверяет есть ли следующий уровень*/

            /* Слияние*/
            if (sameAsNext && canGrow) {
                CashbackLevel nextLevel = CashbackLevel.fromPercent(current.percent() * 2);
                result[resultSize] = nextLevel;
                resultSize++;
                scoreGained += nextLevel.percent();
                index += 2;
            } else {
                result[resultSize] = current;
                resultSize++;
                index++;
            }
        }

        /* Записываем обработанную линию обратно в поле*/
        boolean changed = false;
        for (int position = 0; position < Board.SIZE; position++) {
            int row = rowFor(direction, lineNumber, position);
            int col = colFor(direction, lineNumber, position);
            @Nullable CashbackLevel newLevel = result[position];

            if (original[position] != newLevel) {
                changed = true;
            }

            if (newLevel == null) {
                board.clear(row, col);
            } else {
                board.set(row, col, newLevel);
            }
        }

        return new LineResult(changed, scoreGained);
    }

    /** Читает клетки линии от края, к которому движутся плитки. */
    private @Nullable CashbackLevel[] readLine(Board board, int lineNumber, MoveDirection direction) {
        @Nullable CashbackLevel[] line = new CashbackLevel[Board.SIZE];

        for (int position = 0; position < Board.SIZE; position++) {
            int row = rowFor(direction, lineNumber, position);
            int col = colFor(direction, lineNumber, position);
            line[position] = board.tileAt(row, col).orElse(null);
        }

        return line;
    }

    /** Возвращает номер строки клетки для выбранного направления. */
    private int rowFor(MoveDirection direction, int lineNumber, int position) {
        switch (direction) {
            case LEFT:
            case RIGHT:
                return lineNumber;
            case UP:
                return position;
            case DOWN:
                return Board.SIZE - 1 - position;
            default:
                throw new IllegalArgumentException("Неизвестное направление: " + direction);
        }
    }

    /** Возвращает номер столбца клетки для выбранного направления. */
    private int colFor(MoveDirection direction, int lineNumber, int position) {
        switch (direction) {
            case UP:
            case DOWN:
                return lineNumber;
            case LEFT:
                return position;
            case RIGHT:
                return Board.SIZE - 1 - position;
            default:
                throw new IllegalArgumentException("Неизвестное направление: " + direction);
        }
    }

    /** Ставит одну новую плитку в случайную пустую клетку. */
    private void spawnTile(Board board) {
        int emptyCellsCount = 0;

        /* Считаем число свободных клеток*/
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.tileAt(row, col).isEmpty()) {
                    emptyCellsCount++;
                }
            }
        }

        if (emptyCellsCount == 0) {
            throw new IllegalStateException("После валидного хода должна остаться пустая клетка");
        }

        int selectedEmptyCell = random.nextInt(emptyCellsCount);

        /* Ставим новую плитку*/
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.tileAt(row, col).isEmpty()) {
                    if (selectedEmptyCell == 0) {
                        board.place(row, col, spawnLevel);
                        return;
                    }
                    selectedEmptyCell--;
                }
            }
        }
    }

    /** Результат обработки одной линии. */
    private static final class LineResult {

        private final boolean changed;
        private final int scoreGained; /*Очки слияния линии*/

        private LineResult(boolean changed, int scoreGained) {
            this.changed = changed;
            this.scoreGained = scoreGained;
        }

        private boolean changed() {
            return changed;
        }

        private int scoreGained() {
            return scoreGained;
        }
    }
}
