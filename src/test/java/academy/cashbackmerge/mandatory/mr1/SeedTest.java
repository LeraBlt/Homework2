package academy.cashbackmerge.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import academy.cashbackmerge.Board;
import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.CampaignConfigLoader;
import academy.cashbackmerge.GameState;
import academy.cashbackmerge.InitialGameFactory;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: воспроизводимость по seed. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    @Test
    @DisplayName("Одинаковый конфиг и seed дают одинаковое начальное поле")
    void sameConfigAndSeedProduceSameInitialBoard() {
        CampaignConfig config = new CampaignConfigLoader().load(Path.of("config", "campaign.example.json"));

        InitialGameFactory factory = new InitialGameFactory();
        GameState first = factory.create(config, 42);
        GameState second = factory.create(config, 42);

        assertEquals(first.movesLimit(), second.movesLimit());

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                assertEquals(first.board().tileAt(row, col), second.board().tileAt(row, col));
            }
        }
    }

    @Test
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() {
        fail("Тест не реализован");
    }
}
