package academy.cashbackmerge;

/** Значения плиток */
public enum CashbackLevel {
    ONE(1),
    TWO(2),
    FOUR(4),
    EIGHT(8),
    SIXTEEN(16),
    THIRTY_TWO(32);

    private final int percent;

    CashbackLevel(int percent) {
        this.percent = percent;
    }

    public int percent() {
        return percent;
    }

    public static CashbackLevel fromPercent(int percent) {
        for (CashbackLevel level : CashbackLevel.values()) {
            if (level.percent == percent) {
                return level;
            }
        }
        throw new IllegalArgumentException("Неизвестный уровень кэшбэка: " + percent);
    }
}
