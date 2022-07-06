package resource;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.File;
import resource.Entity.User;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileResource {


    public static String getFilePathOfUploadedFileByUser(String fileId) throws IOException, URISyntaxException, InterruptedException {
        System.out.println("FileResource.getFilePathOfUploadedFileByUser()");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(getURLForFilePath(fileId)))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();

        java.net.http.HttpResponse<InputStream> res = HttpClient.newHttpClient().send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());

        InputStream inputStream = res.body();
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        System.out.println("InputStream Result: " + result);
        String filePathKey = "\"file_path\":\"";
        int idx = result.indexOf(filePathKey);
        return result.substring(idx + filePathKey.length()).replace("\"}}", "");
    }

    private static String getURLForFilePath(String fileId) {
        System.out.println("FileResource.getURLForFilePath()");
        String url = "https://api.telegram.org/bot" + System.getenv("BOT_TOKEN") + "/getFile?file_id=" + fileId;;
        System.out.println(url.replace(System.getenv("BOT_TOKEN"), "<bot_token>"));
        return url;
    }

    public static InputFile getInputFile(String filePath) throws URISyntaxException, IOException, InterruptedException {
        System.out.println("FileResource.getInputFile()");
        //Get File
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(getFile(filePath)))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();

        java.net.http.HttpResponse<InputStream> res = HttpClient.newHttpClient().send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
        InputStream inputStream = res.body();
        java.io.File file = new java.io.File("BestBudsBot_Logo.png");
        try {
            try(OutputStream outputStream = new FileOutputStream(file)){
                IOUtils.copy(inputStream, outputStream);
                System.out.println("File Size after converting: " + file.length());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        InputFile inputFile = new InputFile();
        inputFile.setMedia(file, file.getName());

        return inputFile;
    }

    private static String getFile(String filePath) {
        String url = "https://api.telegram.org/file/bot" + System.getenv("BOT_TOKEN") + "/" + filePath;
        System.out.println(url.replace(System.getenv("BOT_TOKEN"), "<bot_token>"));
        return url;
    }

    public static InputFile getBirthdaySticker() throws InterruptedException, IOException, URISyntaxException {
        String bdayStickerBlueBird = "stickers/file_35.webp";
        return getInputFile(bdayStickerBlueBird);
    }

    public static String getFileIdFromUpdate(Update update) {
        String fileId = "";
        if (update.getMessage().hasDocument()) {
            System.out.println("onUpdateReceived.hasDocument()");
            fileId = update.getMessage().getDocument().getFileId();
        } else if (update.getMessage().hasPhoto()) {
            System.out.println("onUpdateReceived.hasPhoto()");
            List<PhotoSize> photos = update.getMessage().getPhoto();
            Integer chosenFileSize = -1;
            for (PhotoSize photo : photos) {
                chosenFileSize = Math.max(photo.getFileSize(), chosenFileSize);
                if (chosenFileSize.equals(photo.getFileSize())) fileId = photo.getFileId();
            }
        } else if (update.getMessage().hasVideo()) {
            System.out.println("onUpdateReceived.hasVideo()");
            fileId = update.getMessage().getVideo().getFileId();
        } else if (update.getMessage().hasSticker()) {
            System.out.println("onUpdateReceived.hasSticker()");
            fileId = update.getMessage().getSticker().getFileId();
        }

        return fileId;
    }

    public static void sendFileToUser(BestBudsBot bot, String chatId, String fileType, String filePath) throws InterruptedException, IOException, URISyntaxException, TelegramApiException {
        if (fileType.equals(File.DOCUMENT)) {
            System.out.println("onUpdateReceived.hasDocument()");
            SendDocument doc = new SendDocument();
            doc.setChatId(String.valueOf(chatId));
            doc.setDocument(FileResource.getInputFile(filePath));
            bot.execute(doc);
            //
        } else if (fileType.equals(File.PHOTO)) {
            System.out.println("onUpdateReceived.hasPhoto()");
            SendPhoto photo = new SendPhoto();
            photo.setChatId(String.valueOf(chatId));
            photo.setPhoto(FileResource.getInputFile(filePath));
            bot.execute(photo);
        } else if (fileType.equals(File.VIDEO)) {
            System.out.println("onUpdateReceived.hasVideo()");
            SendVideo vid = new SendVideo();
            vid.setChatId(String.valueOf(chatId));
            vid.setVideo(FileResource.getInputFile(filePath));
            bot.execute(vid);
        } else if (fileType.equals(File.STICKER)) {
            System.out.println("onUpdateReceived.hasSticker()");
            SendSticker stick = new SendSticker();
            stick.setChatId(String.valueOf(chatId));
            stick.setSticker(FileResource.getInputFile(filePath));
            bot.execute(stick);
        }
    }

    public static void generateMessageFile(BestBudsBot bot, String chatId, String groupCode, User receiver, PSQL psql) throws IOException, TelegramApiException, SQLException {
        //Creating SendDocuments
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));

        int year = LocalDate.now().getYear();

//        java.io.File file = new java.io.File(receiver.name + "_BestBuds_Messages_" + year + ".csv");
//        FileWriter fileWriter = new FileWriter(file);
//        ArrayList<String> feedbackFile = psql.getAllMessagesForUserRows(receiver.code, groupCode);
//        for (String rowData : feedbackFile){
//            fileWriter.write(rowData);
//        }
        InputFile inputFile = new InputFile();
//        inputFile.setMedia(file);
        inputFile.setMedia(WriteDataToExcel.run());
        sendDocument.setDocument(inputFile);
//        fileWriter.close();
        bot.execute(sendDocument);
    }
}
