package academy.cashbackmerge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class CampaignConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public CampaignConfig load(Path path) {
        Objects.requireNonNull(path, "Путь к конфигу не должен быть null");

        CampaignConfig config;
        try {
            config = mapper.readValue(Files.readString(path), CampaignConfig.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Некорректный JSON в файле конфига: " + path, e);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл конфига: " + path, e);
        }

        validate(config);
        return config;
    }

    private void validate(CampaignConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Конфиг не должен быть пустым");
        }

        if (config.boardSize() != Board.SIZE) {
            throw new IllegalArgumentException(
                    "boardSize должен быть равен " + Board.SIZE + ", получено: " + config.boardSize());
        }

        if (config.movesLimit() <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть больше нуля, получено: " + config.movesLimit());
        }

        if (config.spawnRules() == null || config.spawnRules().isEmpty()) {
            throw new IllegalArgumentException("Список spawnRules не должен быть пустым");
        }

        BigDecimal probabilitySum = BigDecimal.ZERO;

        for (int index = 0; index < config.spawnRules().size(); index++) {
            CampaignConfig.SpawnRule rule = config.spawnRules().get(index);
            String ruleName = "spawnRules[" + index + "]";

            if (rule == null) {
                throw new IllegalArgumentException(ruleName + " не должно быть null");
            }

            validateCashback(rule.cashback(), ruleName + ".cashback");

            BigDecimal probability = parseDecimal(rule.probability(), ruleName + ".probability");
            if (probability.compareTo(BigDecimal.ZERO) <= 0 || probability.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException(ruleName + ".probability должна быть больше 0 и не больше 1");
            }

            probabilitySum = probabilitySum.add(probability);
        }

        if (probabilitySum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException(
                    "Сумма вероятностей spawnRules должна быть ровно 1, получено: " + probabilitySum);
        }
    }

    private void validateCashback(String cashback, String fieldName) {
        BigDecimal value = parseDecimal(cashback, fieldName);

        try {
            CashbackLevel.fromPercent(value.intValueExact());
        } catch (ArithmeticException | IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    fieldName + " должен соответствовать допустимому целому уровню кэшбэка: " + cashback, e);
        }
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не должен быть пустым");
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    fieldName + " должен содержать корректное десятичное число: " + value, e);
        }
    }
}
