package academy.cashbackmerge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Данные кампании, загружаемые из JSON-конфига. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CampaignConfig(
        int boardSize,
        int movesLimit,
        List<SpawnRule> spawnRules,
        List<RewardTier> rewardTable,
        String maxRewardCashback,
        String maxRewardAllowedRate) {

    /** Правило появления плитки с заданным уровнем и вероятностью. */
    public record SpawnRule(String cashback, String probability) {}

    /** Уровень плитки и соответствующая ему награда. */
    public record RewardTier(String cashback, String reward) {}
}
