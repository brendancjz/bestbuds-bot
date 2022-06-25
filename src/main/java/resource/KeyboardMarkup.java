package resource;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardMarkup {

    public KeyboardMarkup(){
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup deleteKBSecond(YearMonth currYearMonth) {

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Confirm");
        button1.setCallbackData("del_confirm_" + currYearMonth.getYear() + "_" + currYearMonth.getMonthValue());
        row.add(button1);

        keyboard.add(row);

        row = new ArrayList<>();

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Back");
        button2.setCallbackData("del_cancel_" + currYearMonth.getYear() + "_" + currYearMonth.getMonthValue()); //direct back
        row.add(button2);

        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup entriesKB(YearMonth prevMonth, YearMonth nextMonth) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("<");
        button1.setCallbackData("entry_" + prevMonth.getYear() + "_" + prevMonth.getMonthValue()); //e.g. fin_2021_7
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText(">");
        button2.setCallbackData("entry_" + nextMonth.getYear() + "_" + nextMonth.getMonthValue());
        row.add(button2);

        keyboard.add(row);
        row = new ArrayList<>();

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup confirmationKB(String command, String callbackData) {

        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("YES");
        button1.setCallbackData("confirmation_" + command + "_YES_" + callbackData);
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("NO");
        button2.setCallbackData("confirmation_" + command + "_NO_" + callbackData);
        row.add(button2);
        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup continueKB(String command) {

        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("CONTINUE TO NEXT PAGE");
        button1.setCallbackData(command + "_page_2");
        row.add(button1);

        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup backKB(String command, Integer currentPageNumber) {

        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("BACK TO PREVIOUS PAGE");
        button1.setCallbackData(command + "_page_" + (currentPageNumber - 1));
        row.add(button1);

        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup navigationKB(String command, Integer currentPageNumber) {

        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("BACK");
        button1.setCallbackData(command + "_page_" + (currentPageNumber - 1));
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("NEXT PAGE");
        button2.setCallbackData(command + "_page_" + (currentPageNumber + 1));
        row.add(button2);
        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    public static ReplyKeyboard selectKB(List<String> options, String context) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        int count = 1;
        for (String selection : options) {
            if (count > 1 && count % 2 == 1) {
                row = new ArrayList<>();
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(selection);
            button.setCallbackData("select_" + context + "_" + selection);
            row.add(button);
            if (count % 2 == 0) {
                keyboard.add(row);
            } else if (count == options.size()) {
                keyboard.add(row);
            }
            count++;
        }

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup refreshKB(String groupSelection, String command) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("REFRESH CONTENT");
        button1.setCallbackData("select_" + command + "_" + groupSelection);
        row.add(button1);

        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup financeKB(YearMonth prevMonth, YearMonth currMonth, YearMonth nextMonth) {

        List<InlineKeyboardButton> row = new ArrayList<>();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("<");
        button1.setCallbackData("fin_" + prevMonth.getYear() + "_" + prevMonth.getMonthValue()); //e.g. fin_2021_7
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText(">");
        button2.setCallbackData("fin_" + nextMonth.getYear() + "_" + nextMonth.getMonthValue());
        row.add(button2);

        keyboard.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText(String.valueOf(LocalDate.now().getMonth()));
        button3.setCallbackData("fin_revert");
        row.add(button3);

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Refresh");
        button4.setCallbackData("fin_refresh_" + currMonth.getYear() + "_" + currMonth.getMonthValue());
        row.add(button4);

        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    //KeyboardMarkUps
    public static InlineKeyboardMarkup deleteKB(YearMonth prevMonth, YearMonth currMonth, YearMonth nextMonth) {

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("<");
        button1.setCallbackData("del_" + prevMonth.getYear() + "_" + prevMonth.getMonthValue()); //e.g. fin_2021_7
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText(">");
        button2.setCallbackData("del_" + nextMonth.getYear() + "_" + nextMonth.getMonthValue());
        row.add(button2);

        keyboard.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Delete " + currMonth.getMonth().name());
        button3.setCallbackData("del_month_" + currMonth.getYear() + "_" + currMonth.getMonthValue());
        row.add(button3);

        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(keyboard);
        return inlineKeyboard;
    }

    public static ReplyKeyboardMarkup getEarnReplyKeyboardMarkup() {
        String[] eCategory = {"Allowance", "Income", "Investment", "Cancel"};

        KeyboardRow row = new KeyboardRow();

        List<KeyboardRow> keyboard = new ArrayList<>();
        int count = 1;
        for (String category : eCategory) {
            if (count > 1 && count % 2 == 1) {
                row = new KeyboardRow();
            }

            KeyboardButton button = new KeyboardButton();
            button.setText(category);
            row.add(button);
            if (count % 2 == 0) {
                keyboard.add(row);
            } else if (count == eCategory.length) {
                keyboard.add(row);
            }
            count++;
        }

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    public static ReplyKeyboardMarkup getSpendReplyKeyboardMarkup() {
        String[] sCategory = {"Entertainment","Food","Gift","Shopping","Transport", "Utilities", "Cancel"};


        KeyboardRow row = new KeyboardRow();

        List<KeyboardRow> keyboard = new ArrayList<>();
        int count = 1;
        for (String category : sCategory) {
            if (count > 1 && count % 2 == 1) {
                row = new KeyboardRow();
            }

            KeyboardButton button = new KeyboardButton();
            button.setText(category);
            row.add(button);
            if (count % 2 == 0) {
                keyboard.add(row);
            } else if (count == sCategory.length) {
                keyboard.add(row);
            }
            count++;
        }

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    public static ReplyKeyboardMarkup getDelReplyKeyboardMarkup() {
        KeyboardRow row = new KeyboardRow();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardButton button1 = new KeyboardButton();
        button1.setText("anything");
        row.add(button1);

        keyboard.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }
}
