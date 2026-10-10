package academy.cashbackmerge;

import java.util.Random;

/** Сборка начального состояния */
public class InitialGameFactory {
    public GameState create(CampaignConfig config, long seed) {
        Board board = new Board();
        Random random = new Random(seed);
        int firstCell = random.nextInt(Board.SIZE * Board.SIZE);

        int secondCell;
        do {
            secondCell = random.nextInt(Board.SIZE * Board.SIZE);
        } while (secondCell == firstCell);
        String cashback = config.spawnRules().get(0).cashback();
        CashbackLevel level = CashbackLevel.fromPercent(Integer.parseInt(cashback));

        int firstRow = firstCell / Board.SIZE;
        int firstCol = firstCell % Board.SIZE;
        board.place(firstRow, firstCol, level);

        int secondRow = secondCell / Board.SIZE;
        int secondCol = secondCell % Board.SIZE;
        board.place(secondRow, secondCol, level);

        return new GameState(board, config.movesLimit());
    }
}
