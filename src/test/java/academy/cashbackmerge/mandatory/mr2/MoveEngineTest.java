package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.Board;
import academy.cashbackmerge.CashbackLevel;
import academy.cashbackmerge.GameState;
import academy.cashbackmerge.MoveDirection;
import academy.cashbackmerge.MoveEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: движок ходов — что считается ходом и что после него происходит. */
@DisplayName("MR2. Движок ходов")
class MoveEngineTest {

    @Test
    @DisplayName("Невалидный ход не меняет поле, не тратит ход и не создаёт плитку")
    void invalidMoveChangesNothing() {
        Board board = new Board();
        board.place(0, 0, CashbackLevel.ONE);
        GameState state = new GameState(board, 10);
        String boardBeforeMove = board.toString();

        var result = new MoveEngine().applyMove(state, MoveDirection.LEFT);

        assertFalse(result.valid());
        assertEquals(boardBeforeMove, board.toString());
        assertEquals(1, countTiles(board));
        assertEquals(0, state.movesPlayed());
        assertEquals(0, state.score());
    }

    @Test
    @DisplayName("После валидного хода появляется ровно одна новая плитка")
    void validMoveSpawnsExactlyOneTile() {
        Board board = new Board();
        board.place(0, 1, CashbackLevel.ONE);
        GameState state = new GameState(board, 10);

        var result = new MoveEngine().applyMove(state, MoveDirection.LEFT);

        assertTrue(result.valid());
        assertEquals(2, countTiles(board));
        assertEquals(1, state.movesPlayed());
    }

    @Test
    @DisplayName("После N валидных ходов игра завершена")
    void gameEndsAfterMovesLimit() {
        Board board = new Board();
        board.place(0, 1, CashbackLevel.ONE);
        GameState state = new GameState(board, 1);
        MoveEngine engine = new MoveEngine();

        var firstResult = engine.applyMove(state, MoveDirection.LEFT);

        assertTrue(firstResult.valid());
        assertTrue(state.isFinished());
        assertEquals(1, state.movesPlayed());

        String boardAfterLastAllowedMove = board.toString();
        var nextResult = engine.applyMove(state, MoveDirection.RIGHT);

        assertFalse(nextResult.valid());
        assertEquals(boardAfterLastAllowedMove, board.toString());
        assertEquals(1, state.movesPlayed());
    }

    @Test
    @DisplayName("Достижение плитки 32% не заканчивает игру — доигрываем до лимита ходов")
    void reachingMaxTileDoesNotEndGame() {
        Board board = new Board();
        board.place(0, 1, CashbackLevel.THIRTY_TWO);
        GameState state = new GameState(board, 2);

        var result = new MoveEngine().applyMove(state, MoveDirection.LEFT);

        assertTrue(result.valid());
        assertFalse(state.isFinished());
        assertEquals(1, state.movesPlayed());
        assertEquals(CashbackLevel.THIRTY_TWO, board.tileAt(0, 0).orElseThrow());
    }

    @Test
    @DisplayName("Score считается по правилам и растёт только при слияниях")
    void scoreIsCalculatedOnMerges() {
        Board mergingBoard = new Board();
        mergingBoard.place(0, 0, CashbackLevel.ONE);
        mergingBoard.place(0, 1, CashbackLevel.ONE);
        GameState mergingState = new GameState(mergingBoard, 10);

        var mergeResult = new MoveEngine().applyMove(mergingState, MoveDirection.LEFT);

        assertEquals(2, mergeResult.scoreGained());
        assertEquals(2, mergingState.score());

        Board slidingBoard = new Board();
        slidingBoard.place(0, 1, CashbackLevel.ONE);
        GameState slidingState = new GameState(slidingBoard, 10);

        var slideResult = new MoveEngine().applyMove(slidingState, MoveDirection.LEFT);

        assertTrue(slideResult.valid());
        assertEquals(0, slideResult.scoreGained());
        assertEquals(0, slidingState.score());
    }

    private int countTiles(Board board) {
        int count = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.tileAt(row, col).isPresent()) {
                    count++;
                }
            }
        }
        return count;
    }
}
