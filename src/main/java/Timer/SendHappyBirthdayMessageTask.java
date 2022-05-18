package Timer;

import TelegramBot.BestBudsBot;

import java.util.TimerTask;

public class SendHappyBirthdayMessageTask extends TimerTask {
    private final BestBudsBot bot;
    private final int chatId;

    public SendHappyBirthdayMessageTask(BestBudsBot bot, int chatId) {
        super();
        this.bot = bot;
        this.chatId = chatId;
    }

    @Override
    public void run() {
        this.bot.runScheduleHappyBirthdayMessage(chatId);
    }
}
