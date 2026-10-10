package academy.cashbackmerge;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа в Cashback Merge. */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final long DEFAULT_SEED = 42L;

    private Main() {}

    public static void main(String[] args) {
        LOG.debug("Аргументы запуска: {}", String.join(" ", args));

        Path configPath = Path.of("config", "campaign.example.json");
        long seed = DEFAULT_SEED;

        CampaignConfig config = new CampaignConfigLoader().load(configPath);
        GameState state = new InitialGameFactory().create(config, seed);

        System.out.println("Начальное поле, seed = " + seed + ":");
        System.out.println(state.board());
        System.out.println("Лимит ходов: " + state.movesLimit());
    }
}
