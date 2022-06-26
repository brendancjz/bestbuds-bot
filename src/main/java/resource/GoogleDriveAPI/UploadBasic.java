package resource.GoogleDriveAPI;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ClientId;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/* Class to demonstrate use of Drive insert file API */
public class UploadBasic {

    /**
     * Upload new file.
     * @return Inserted file metadata if successful, {@code null} otherwise.
     * @throws IOException if service account credentials file not found.
     */
    public static String uploadBasic() throws IOException{
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.

        GoogleCredentials credentials = GoogleCredentials.create(refreshAccessToken()).createScoped(Arrays.asList(DriveScopes.DRIVE_FILE));
        System.out.println(credentials.getAccessToken());
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                credentials);

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("BestBuds Bot Drive")
                .build();

        // Upload file photo.jpg on drive.
        File fileMetadata = new File();
        fileMetadata.setName("photo.jpg");
        // File's content.
        java.io.File filePath = new java.io.File("photo.jpg");
        // Specify media type and file-path for file.
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        try {
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            return file.getId();
        }catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            throw e;
        }
    }

    public static String getNewToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
                refreshToken, clientId, clientSecret).setScopes(Arrays.asList(DriveScopes.DRIVE_FILE)).setGrantType("refresh_token").execute();

        return tokenResponse.getAccessToken();
    }

    static AccessToken refreshAccessToken() throws IOException {
            TokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    "4/0AX4XfWhdcOK0NjjEZdXxgMwv7FyTHhu2-XkcrY6A5UXsdw9-8CIMMMhWTXChEFC9DOtU0Q",
                    "514287612123-774mkhq9l83c7ieppf0b51brv4vhbhbk.apps.googleusercontent.com",
                    "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").execute();
            System.out.println("Access token: " + response.getAccessToken());

            return new AccessToken(response.getAccessToken(), null);
    }

}