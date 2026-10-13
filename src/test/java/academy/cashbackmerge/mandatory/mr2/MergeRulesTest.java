package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.Board;
import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.CashbackLevel;
import academy.cashbackmerge.GameState;
import academy.cashbackmerge.MoveDirection;
import academy.cashbackmerge.MoveEngine;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Обязательные тесты: правила слияния плиток. */
@DisplayName("MR2. Слияние плиток")
class MergeRulesTest {

    private static final CashbackLevel SPAWN_LEVEL = CashbackLevel.THIRTY_TWO;

    @ParameterizedTest
    @CsvSource({
        "'1,1,.,.', '2,.,.,.'",
        "'1,1,1,.', '2,1,.,.'",
        "'1,1,1,1', '2,2,.,.'",
        "'2,2,4,4', '4,8,.,.'",
    })
    @DisplayName("Сдвиг влево: {0} -> {1}")
    void rowShiftsLeftAndMerges(String before, String expected) {
        GameState state = stateWithFirstRow(before);

        var result = new MoveEngine(configWithSpawnLevel32(), 1).applyMove(state, MoveDirection.LEFT);

        assertTrue(result.valid());
        assertRowMatchesExpectedIgnoringSpawn(state.board(), expected);
    }

    @Test
    @DisplayName("Одна плитка не сливается дважды за ход: [1,1,2,.] -> [2,2,.,.]")
    void tileDoesNotMergeTwiceInOneMove() {
        GameState state = stateWithFirstRow("1,1,2,.");

        var result = new MoveEngine(configWithSpawnLevel32(), 1).applyMove(state, MoveDirection.LEFT);

        assertTrue(result.valid());
        assertRowMatchesExpectedIgnoringSpawn(state.board(), "2,2,.,.");
    }

    @Test
    @DisplayName("Слияние работает во всех четырёх направлениях")
    void mergeWorksInAllDirections() {
        assertPairMerges(MoveDirection.LEFT, 0, 1, 0, 2, 0, 0);
        assertPairMerges(MoveDirection.RIGHT, 0, 1, 0, 2, 0, 3);
        assertPairMerges(MoveDirection.UP, 1, 0, 2, 0, 0, 0);
        assertPairMerges(MoveDirection.DOWN, 1, 0, 2, 0, 3, 0);
    }

    private void assertPairMerges(
            MoveDirection direction,
            int firstRow,
            int firstCol,
            int secondRow,
            int secondCol,
            int resultRow,
            int resultCol) {
        Board board = new Board();
        board.place(firstRow, firstCol, CashbackLevel.ONE);
        board.place(secondRow, secondCol, CashbackLevel.ONE);
        GameState state = new GameState(board, 10);
        MoveEngine engine = new MoveEngine(configWithSpawnLevel32(), 2);

        var result = engine.applyMove(state, direction);

        assertTrue(result.valid());
        assertEquals(CashbackLevel.TWO, board.tileAt(resultRow, resultCol).orElseThrow());
    }

    private GameState stateWithFirstRow(String rowText) {
        Board board = new Board();
        String[] cells = rowText.split(",");
        for (int col = 0; col < cells.length; col++) {
            if (!cells[col].equals(".")) {
                board.place(0, col, CashbackLevel.fromPercent(Integer.parseInt(cells[col])));
            }
        }
        return new GameState(board, 10);
    }

    private void assertRowMatchesExpectedIgnoringSpawn(Board board, String expectedText) {
        String[] expectedCells = expectedText.split(",");
        List<Integer> actualValues = new ArrayList<>();
        for (int col = 0; col < Board.SIZE; col++) {
            var tile = board.tileAt(0, col);
            if (tile.isPresent() && tile.get() != SPAWN_LEVEL) {
                actualValues.add(tile.get().percent());
            }
        }

        List<Integer> expectedValues = new ArrayList<>();
        for (String cell : expectedCells) {
            if (!".".equals(cell)) {
                expectedValues.add(Integer.parseInt(cell));
            }
        }

        assertEquals(expectedValues, actualValues);
    }

    private CampaignConfig configWithSpawnLevel32() {
        CampaignConfig.SpawnRule spawnRule = new CampaignConfig.SpawnRule("32", "1");
        return new CampaignConfig(4, 10, List.of(spawnRule), List.of(), "32", "0.20");
    }
}
