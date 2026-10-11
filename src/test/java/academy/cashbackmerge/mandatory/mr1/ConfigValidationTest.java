package academy.cashbackmerge.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.cashbackmerge.CampaignConfigLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Обязательные тесты: конфиг кампании. */
@DisplayName("MR1. Конфиг кампании")
class ConfigValidationTest {

    @Test
    @DisplayName("Валидный конфиг принимается")
    void validConfigIsAccepted() {
        var loader = new CampaignConfigLoader();
        var config = loader.load(Path.of("config", "campaign.example.json"));
        assertNotNull(config);
        assertEquals(4, config.boardSize());
        assertEquals(100, config.movesLimit());
    }

    @Test
    @DisplayName("Сумма вероятностей spawn-правил не равна 1 — конфиг отклоняется")
    void probabilitiesNotSummingToOneAreRejected(@TempDir Path tempDir) throws IOException {
        String json = """
            {
              "boardSize": 4,
              "movesLimit": 100,
              "spawnRules": [
                { "cashback": "1", "probability": "0.85" },
                { "cashback": "2", "probability": "0.10" }
              ]
            }
            """;

        Path configPath = tempDir.resolve("invalid-campaign.json");
        Files.writeString(configPath, json);

        var loader = new CampaignConfigLoader();

        assertThrows(IllegalArgumentException.class, () -> loader.load(configPath));
    }

    @Test
    @DisplayName("movesLimit <= 0 — конфиг отклоняется")
    void nonPositiveMovesLimitIsRejected(@TempDir Path tempDir) throws IOException {
        String json = """
            {
              "boardSize": 4,
              "movesLimit": 0,
              "spawnRules": [
                { "cashback": "1", "probability": "0.85" },
                { "cashback": "2", "probability": "0.15" }
              ]
            }
            """;

        Path configPath = tempDir.resolve("invalid-moves-limit.json");
        Files.writeString(configPath, json);

        var loader = new CampaignConfigLoader();

        assertThrows(IllegalArgumentException.class, () -> loader.load(configPath));
    }

    @Test
    @DisplayName("Пустой список spawn-правил — конфиг отклоняется")
    void emptySpawnRulesAreRejected(@TempDir Path tempDir) throws IOException {
        String json = """
            {
              "boardSize": 4,
              "movesLimit": 100,
              "spawnRules": []
            }
            """;

        Path configPath = tempDir.resolve("empty-spawn-rules.json");
        Files.writeString(configPath, json);

        var loader = new CampaignConfigLoader();

        assertThrows(IllegalArgumentException.class, () -> loader.load(configPath));
    }
}
