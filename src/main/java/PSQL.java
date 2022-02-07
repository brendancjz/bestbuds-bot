import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class PSQL {
    private final Connection connection;

    public PSQL() throws URISyntaxException, SQLException {
        this.connection = getConnection();
    }

    public void addNewUser(int chatId, String date) throws SQLException, ParseException {
        boolean userExists = isUserRegistered(chatId);

        if (!userExists) {
            System.out.println("This user is not registered yet.");

            String sql = "INSERT INTO Users (chat_id, dob) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, chatId);
            preparedStatement.setDate(2, (Date) new SimpleDateFormat("dd/MM/yyyy").parse(date));

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Successful registration.");
                System.out.println("[" + chatId + "] has been registered in users.");
            } else {
                System.out.println("Unsuccessful registration in users.");
            }

        }

    }

    private ResultSet getUsersDataResultSet(int chatId) throws SQLException {
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Users WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        return statement.executeQuery();
    }

    public boolean isUserRegistered(int chatId) throws SQLException {

        boolean userExists = false;
        ResultSet resultSet = getUsersDataResultSet(chatId);
        while (resultSet.next()) {
            userExists = true;
            String selectedChatId = resultSet.getString("chat_id");
            System.out.println("[" + selectedChatId + "] has been selected.");
        }

        return userExists;
    }

    private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
