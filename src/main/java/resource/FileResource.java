package resource;

import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;

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
        return "https://api.telegram.org/bot" + System.getenv("BOT_TOKEN") + "/getFile?file_id=" + fileId;
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
        java.io.File file = new java.io.File(filePath);
        try {
            try(OutputStream outputStream = new FileOutputStream(file)){
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (IOException e) {
            // handle exception here
            System.out.println(e.getMessage());
        }
        InputFile inputFile = new InputFile();
        inputFile.setMedia(file, file.getName());

        return inputFile;
    }

    private static String getFile(String filePath) {
        String url = "https://api.telegram.org/file/bot" + System.getenv("BOT_TOKEN") + "/" + filePath;
        System.out.println(url);
        return url;
    }
}
