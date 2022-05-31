package TelegramBot;

import Command.*;
import Command.GroupCommand.*;
import Command.MessageCommand.MessageComand;
import Command.UserCommand.ProfileCommand;
import Command.UserCommand.UpdateCommand;
import Command.UserCommand.ViewBestBudCommand;
import PSQL.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class BestBudsBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("onUpdateReceived called.");
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                SendMessage message = new SendMessage();

                message.setChatId(update.getMessage().getChatId().toString());
                message.enableHtml(true);

                Integer chatId = Integer.parseInt(update.getMessage().getChatId().toString());
                PSQL psql = new PSQL();

                //Personal Chats have positive chatId while Group Chats have negative chatId
                if (chatId > 0) {
                    personalChatMessage(message, update, chatId, psql);
                } else {
                    groupChatMessage(message);
                }

                psql.closeConnection();
            } else if (update.hasCallbackQuery()) {

                Integer chatId = Integer.parseInt(update.getCallbackQuery().getMessage().getChatId().toString());
                PSQL psql = new PSQL();

                if (chatId > 0) {
                    System.out.println("In personalChatCallback");
                    personalChatCallback(update, chatId, psql);
                } else {
                    System.out.println("In groupChatCallback");
                    groupChatCallback(update, chatId, psql);
                }

                psql.closeConnection();
            }
        } catch (SQLException | URISyntaxException throwables) {
            throwables.printStackTrace();
        }

    }

    private void groupChatCallback(Update update, Integer chatId, PSQL psql) {
    }

    private void personalChatCallback(Update update, Integer chatId, PSQL psql) {
        try {
            String callData = update.getCallbackQuery().getData();
            String prevMessage = update.getCallbackQuery().getMessage().getText();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(chatId.toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            Command command = null;

//            SendMessage message = new SendMessage();
//            message.setChatId(chatId.toString());
//            message.setText("Hello world");
//            execute(message);

            if (callData.startsWith("confirmation")) { //TODO Abstract out the create command to join too

                String[] callBackArr = update.getCallbackQuery().getData().split("_");
                String commandStr = callBackArr[1];
                
                if (commandStr.equals("create")) {
                    command = new CreateCommand(this, update, psql);
                } else if (commandStr.equals("join")) {
                    command = new JoinCommand(this, update, psql);
                } else if (commandStr.equals("exit")) {
                    command = new ExitCommand(this, update, psql);
                }
                command.runCallback();
                return;
            } else if (callData.startsWith("select_")) {
                String[] callBackArr = update.getCallbackQuery().getData().split("_");
                String commandStr = callBackArr[1];

                if (commandStr.equals("viewBestBuds")) {
                    command = new ViewBestBudsCommand(this, update, psql);
                } else if (commandStr.equals("viewGroup")) {
                    command = new ViewGroupCommand(this, update, psql);
                } else if (commandStr.equals("shareCode")) {
                    command = new ShareCodeCommand(this, update, psql);
                }

                command.runCallback();
                return;

            } else if (callData.startsWith("start_page")) {

                System.out.println("=== Start Event Called === ");
                command = new StartCommand(this, update, psql);
                command.runCallback();
                return;
            } else if (callData.startsWith("subscribe_page")) {

                System.out.println("=== Subscribe Event Called === ");
                command = new SubscribeCommand(this, update, psql);
                command.runCallback();
                return;
            } else if (callData.startsWith("profile_page")) {

                System.out.println("=== Profile Event Called === ");
                command = new ProfileCommand(this, update, psql);
                command.runCallback();
                return;
            } else if (callData.startsWith("message_page")) {

                System.out.println("=== Message Event Called === ");
                command = new MessageComand(this, update, psql);
                command.runCallback();
                return;
            }
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private void groupChatMessage(SendMessage message) {
        try {
            message.setText("Sorry. This bot currently does not support group messaging.");

            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void personalChatMessage(SendMessage message, Update update, int chatId, PSQL psql) {
        try {
            String text = update.getMessage().getText().trim();

            runPersonalChatMessageRouter(this, update, psql);

            Command command;
            //Universal Commands. No need to update Query and check User.
            if (text.startsWith("/start")) {
                System.out.println("=== Start Event Called === ");
                command = new StartCommand(this, update, psql);
                command.runCommand();
                return;
            }
            if (text.startsWith("/help")) {
                System.out.println("=== Help Event Called === ");
                command = new HelpCommand(this, update, psql);
                command.runCommand();
                return;
            }
            if (text.startsWith("/subscribe")) {
                System.out.println("=== Subscribe Event Called === ");
                command = new SubscribeCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/profile")) {
                System.out.println("=== Profile Event Called === ");
                command = new ProfileCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/message")) {
                System.out.println("=== Message Event Called === ");
                command = new MessageComand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/create")) {
                System.out.println("=== Create Event Called === ");
                command = new CreateCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/join")) {
                System.out.println("=== Join Event Called === ");
                command = new JoinCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/exit")) {
                System.out.println("=== Exit Event Called === ");
                command = new ExitCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/view_group")) {
                System.out.println("=== View Group Event Called === ");
                command = new ViewGroupCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/view_bestbuds")) {
                System.out.println("=== View BestBuds Event Called === ");
                command = new ViewBestBudsCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/share_code")) {
                System.out.println("=== Share Code Event Called === ");
                command = new ShareCodeCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/update")) {
                System.out.println("=== Update Event Called === ");
                command = new UpdateCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/view_user")) {
                System.out.println("=== View Event Called === ");
                command = new ViewBestBudCommand(this, update, psql);
                command.runCommand();
            } else {
                message.enableHtml(false);
                message.setText("Bad Command: " + text + " . Enter /help for assistance.");
                executeMessage(message);
            }
        } catch (SQLException | URISyntaxException throwables) {
            throwables.printStackTrace();
        }
    }

    private void runPersonalChatMessageRouter(BestBudsBot bestBudsBot, Update update, PSQL psql) {
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void runScheduleHappyBirthdayMessage(int chatId) {
        try {
            PSQL psql = new PSQL();

            SendMessage message = new SendMessage();
            message.setChatId("" + chatId);

            message.setText("Happy Birthday!");

            execute(message);
            psql.closeConnection();
        } catch (URISyntaxException | SQLException | TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
