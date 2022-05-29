package PSQL;

import resource.Entity.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

public class PSQL {
    private final Connection connection;

    public PSQL() throws URISyntaxException, SQLException {
        this.connection = getConnection();
    }

    public void addNewUser(int chatId, String name) throws SQLException {
        boolean userExists = isUserRegistered(chatId);

        if (!userExists) {
            System.out.println("This user is not registered yet.");

            String sql = "INSERT INTO Users (chat_id,name,dob,code,joined_on,description) VALUES (?, ?, ?, ?, ?, ?)";
            //INSERT INTO Users (chat_id, name, dob, code, joined_on, description) VALUES (123, 'bobby', '1999-05-29', 'BRE', '2022-05-26', 'brendan');
            //Date.valueOf("2022-05-12")
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, chatId);
            preparedStatement.setString(2, name);
            preparedStatement.setDate(3, null);
            preparedStatement.setString(4, getNewUserCode(name));
            preparedStatement.setDate(5, Date.valueOf(LocalDate.now()));
            preparedStatement.setString(6, null);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Successful registration.");
                System.out.println("[" + chatId + "] has been registered in users.");
            } else {
                System.out.println("Unsuccessful registration in users.");
            }

        }

    }

    public void addNewGroup(int chatId, String groupName) throws SQLException {
        boolean userExists = isUserRegistered(chatId);

        if (userExists) {
            User owner = this.getUserDataResultSet(chatId);

            String sql = "INSERT INTO Groups (name,code,created_on,created_by) VALUES (?, ?, ?, ?)";
            //INSERT INTO Groups (name, code, created_on, created_by) VALUES ('bob', 'bb123', '2022-05-28', 'bre9970');
            //Date.valueOf("2022-05-12")
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, groupName);
            preparedStatement.setString(2, getNewGroupCode(groupName));
            preparedStatement.setDate(3, Date.valueOf(LocalDate.now()));
            preparedStatement.setString(4, owner.code);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Successful creation.");
                System.out.println("[" + groupName + "] has been registered in Groups.");
            } else {
                System.out.println("Unsuccessful registration in Groups.");
            }

        }

    }

    public void updateUserDOB(int chatId, String text) throws SQLException {
        String sql = "UPDATE Users SET dob=? WHERE chat_id=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setDate(1, Date.valueOf(text));
        statement.setInt(2, chatId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[DOB] Update query successful.");
        } else {
            System.out.println("[DOB] Update query failed.");
        }
    }

    public void updateUserName(int chatId, String firstName) throws SQLException {
        String sql = "UPDATE Users SET name=? WHERE chat_id=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setString(1, firstName);
        statement.setInt(2, chatId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Name] Update query successful.");
        } else {
            System.out.println("[Name] Update query failed.");
        }
    }

    public void updateUserDesc(int chatId, String desc) throws SQLException {
        String sql = "UPDATE Users SET description=? WHERE chat_id=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setString(1, desc);
        statement.setInt(2, chatId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Desc] Update query successful.");
        } else {
            System.out.println("[Desc] Update query failed.");
        }
    }

    public void updateUserNameAndDOB(int chatId, String firstName, String dob) throws SQLException {
        String sql = "UPDATE Users SET name=?,dob=? WHERE chat_id=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setString(1, firstName);
        statement.setDate(2, Date.valueOf(dob));
        statement.setInt(3, chatId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Name & DOB] Update query successful.");
        } else {
            System.out.println("[Name & DOB] Update query failed.");
        }
    }

    public User getUserDataResultSet(int chatId) throws SQLException {
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Users WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        ResultSet resultSet = statement.executeQuery();
        User user = new User();

        while (resultSet.next()) {
            user.name = resultSet.getString("name");
            user.code = resultSet.getString("code");
            user.dob = resultSet.getDate("dob");
            user.desc = resultSet.getString("description");
        }

        return user;
    }

    public User getUserDataResultSet(String userCode) throws SQLException {
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Users WHERE code = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, userCode);

        ResultSet resultSet = statement.executeQuery();
        User user = new User();

        while (resultSet.next()) {
            user.name = resultSet.getString("name");
            user.code = resultSet.getString("code");
            user.dob = resultSet.getDate("dob");
            user.desc = resultSet.getString("description");
        }

        return user;
    }

    private ResultSet getUsersDataResultSet(int chatId) throws SQLException {
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Users WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        return statement.executeQuery();
    }

//    public String getUserDOB(int chatId) throws SQLException {
//        System.out.println("-- Getting User DOB State --");
//
//        String dob = "null";
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        //Selecting User from Users table.
//        ResultSet resultSet = getUsersDataResultSet(chatId);
//        while (resultSet.next()) {
//            Date date = resultSet.getDate("dob");
//            if (date != null) dob = dateFormat.format(date);
//            System.out.println("User's dob is " + dob);
//        }
//
//        return dob;
//    }

    public ArrayList<String> getAllChatId() throws SQLException {
        String sql = "SELECT * from Users";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        ArrayList<String> chatIds = new ArrayList<>();
        while(resultSet.next()) {
            int chatId = resultSet.getInt("chat_id");
            chatIds.add(Integer.toString(chatId));

        }

        return chatIds;
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

    private static String getRandomFourDigitCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(9999);

        // this will convert any number sequence into 6 character.
        return String.format("%04d", number);
    }

    private static String getNewUserCode(String name) {
        if (name.length() >= 3) {
            return name.substring(0,3) + getRandomFourDigitCode();
        }

        return name + getRandomFourDigitCode();
    }

    private static String getNewGroupCode(String name) {
        String code = "";

        String[] arr = name.split(" ");
        if (arr.length > 1) {
            for (String word : arr) {
                code += word.charAt(0);
            }
        } else {
            code = name;
        }

        return code + getRandomFourDigitCode();
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
