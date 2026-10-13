package academy.cashbackmerge;

import java.util.Optional;

/** Игровое поле фиксированного размера 4x4. */
public class Board {

    public static final int SIZE = 4;

    private final CashbackLevel[][] cells = new CashbackLevel[SIZE][SIZE];

    /** Проверка на координаты в поле */
    private void checkCoordinates(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException("Координаты вне поля: row=" + row + ", col=" + col);
        }
    }

    /** Возвращает плитку в клетке или пустой результат, если клетка свободна. */
    public Optional<CashbackLevel> tileAt(int row, int col) {
        checkCoordinates(row, col);
        return Optional.ofNullable(cells[row][col]); /* Контейнер, который может хранить значение или быть пустым*/
    }

    /** Размещает плитку в свободной клетке. */
    public void place(int row, int col, CashbackLevel level) {
        checkCoordinates(row, col);

        if (level == null) {
            throw new IllegalArgumentException("Уровень плитки не должен быть null");
        }
        if (cells[row][col] != null) {
            throw new IllegalArgumentException("Клетка уже занята: row=" + row + ", col=" + col);
        }

        cells[row][col] = level;
    }

    /** Очистка клетки */
    public void clear(int row, int col) {
        checkCoordinates(row, col);
        cells[row][col] = null;
    }

    /** Замена содержимого клетки */
    public void set(int row, int col, CashbackLevel level) {
        checkCoordinates(row, col);
        if (level == null) {
            throw new IllegalArgumentException("Уровень плитки не должен быть null");
        }
        cells[row][col] = level;
    }

    /** Метод для вывода поля */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                var tile = tileAt(row, col);

                if (tile.isPresent()) {
                    text.append(tile.get().percent()).append("% ");
                } else {
                    text.append(". ");
                }
            }
            text.append("\n");
        }

        return text.toString();
    }
}
