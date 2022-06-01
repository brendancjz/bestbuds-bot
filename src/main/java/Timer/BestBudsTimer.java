package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Timer;

public class BestBudsTimer {
    private final PSQL psql;
    private final Timer timer;
    private final BestBudsBot bot;

    public BestBudsTimer(BestBudsBot bestBudsBot) throws URISyntaxException, SQLException {
        this.timer = new Timer();
        this.psql = new PSQL();
        this.bot = bestBudsBot;
    }

    public void start() throws URISyntaxException, SQLException {}

    public BestBudsBot getBot() {
        return this.bot;
    }

    public PSQL getPSQL() {
        return this.psql;
    }

    public Timer getTimer() {
        return this.timer;
    }
}
