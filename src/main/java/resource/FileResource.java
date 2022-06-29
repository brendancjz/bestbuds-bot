package resource;

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
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

    public static void sendFileToUser(BestBudsBot bot, Update update, String filePath) throws InterruptedException, IOException, URISyntaxException, TelegramApiException {
        if (update.getMessage().hasDocument()) {
            System.out.println("onUpdateReceived.hasDocument()");
            SendDocument doc = new SendDocument();
            doc.setChatId(String.valueOf(update.getMessage().getChatId()));
            doc.setDocument(FileResource.getInputFile(filePath));
            bot.execute(doc);
            //
        } else if (update.getMessage().hasPhoto()) {
            System.out.println("onUpdateReceived.hasPhoto()");
            SendPhoto photo = new SendPhoto();
            photo.setChatId(String.valueOf(update.getMessage().getChatId()));
            photo.setPhoto(FileResource.getInputFile(filePath));
            bot.execute(photo);
        } else if (update.getMessage().hasVideo()) {
            System.out.println("onUpdateReceived.hasVideo()");
            SendVideo vid = new SendVideo();
            vid.setChatId(String.valueOf(update.getMessage().getChatId()));
            vid.setVideo(FileResource.getInputFile(filePath));
            bot.execute(vid);
        } else if (update.getMessage().hasSticker()) {
            System.out.println("onUpdateReceived.hasSticker()");
            SendSticker stick = new SendSticker();
            stick.setChatId(String.valueOf(update.getMessage().getChatId()));
            stick.setSticker(FileResource.getInputFile(filePath));
            bot.execute(stick);
        }
    }
}
