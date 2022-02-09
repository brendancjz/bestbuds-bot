package Timer;

import TelegramBot.BirthdayBot;

import java.util.TimerTask;

public class SendHappyBirthdayMessageTask extends TimerTask {
    private final BirthdayBot bot;
    private final int chatId;

    public SendHappyBirthdayMessageTask(BirthdayBot bot, int chatId) {
        super();
        this.bot = bot;
        this.chatId = chatId;
    }

    @Override
    public void run() {
        this.bot.runScheduleHappyBirthdayMessage(chatId);
    }
}
