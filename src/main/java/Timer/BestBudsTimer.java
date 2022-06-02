package Timer;

import TelegramBot.BestBudsBot;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class BestBudsTimer {
    private final BestBudsBot bot;

    public BestBudsTimer(BestBudsBot bestBudsBot) {
        this.bot = bestBudsBot;
    }

    public void start() throws URISyntaxException, SQLException {}

    public BestBudsBot getBot() {
        return this.bot;
    }

}
