package PSQL;

import resource.Entity.Group;
import resource.Entity.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    public Group addNewGroup(int chatId, String groupName) throws SQLException {
        boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return null;
        System.out.println("Adding new Group");

        User owner = this.getUserDataResultSet(chatId);
        Group newGroup = new Group();
        newGroup.name = groupName;
        newGroup.code = getNewGroupCode(groupName);
        newGroup.createdOn = Date.valueOf(LocalDate.now());
        newGroup.createdBy = owner.code;

        String sql = "INSERT INTO Groups (name,code,created_by,created_on) VALUES (?, ?, ?, ?)";
        //insert into groups (name, code, created_by, created_on) VALUES ('bob vans', 'bv123', 'bren6016', '2022-05-29');
        //Date.valueOf("2022-05-12")
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, groupName);
        preparedStatement.setString(2, newGroup.code);
        preparedStatement.setString(3, newGroup.createdBy);
        preparedStatement.setDate(4, newGroup.createdOn);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful creation.");
            System.out.println("[" + groupName + "] has been registered in Groups.");

            return newGroup;
        } else {
            System.out.println("Unsuccessful registration in Groups.");

            return null;
        }

    }

    public Boolean addUserIntoGroup(Integer chatId, String groupCode) throws SQLException {
        Boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return false;
        if (isGroupCodeUnique(groupCode)) return false;

        System.out.println("Adding User to Group");

        String sql = "INSERT INTO GroupUsers (group_code, chat_id) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, groupCode);
        preparedStatement.setInt(2, chatId);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful joining of group.");
            System.out.println("[" + chatId + "] has been joined a Group " + groupCode + ".");
            return true;
        } else {
            System.out.println("Unsuccessful registration in Groups.");
            return false;
        }
    }


    public Boolean addUserIntoBirthdayManagement(Integer chatId) throws SQLException {
        Boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return false;
        if (isUserAlreadyInBirthdayManagement(chatId)) return false;

        System.out.println("Adding User to BirthdayManagement");

        String sql = "INSERT INTO BirthdayManagement (chat_id, has_sent_initial) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, chatId);
        preparedStatement.setBoolean(2, false);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful adding entry to bday mgmt");
            return true;
        } else {
            System.out.println("Unsuccessful entry in bday mgmt.");
            return false;
        }
    }

    private boolean isUserAlreadyInBirthdayManagement(Integer chatId) throws SQLException {
        System.out.println("PSQL.isUserAlreadyInBirthdayManagement()");
        // Obtaining user information from BIRTHDAYMANAGEMENT
        String sql = "SELECT * FROM BirthdayManagement WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        ResultSet resultSet = statement.executeQuery();
        User user = new User();

        while (resultSet.next()) {
            user = this.getUserDataResultSet(resultSet.getInt("chat_id"));
        }

        return !User.isNull(user);
    }

    public void removeUserFromGroup(Integer chatId, String groupCode) throws SQLException {
        boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return;
        if (isGroupCodeUnique(groupCode)) return;

        System.out.println("Removing User From Group");

        String sql = "DELETE FROM GroupUsers WHERE group_code=? AND chat_id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, groupCode);
        preparedStatement.setInt(2, chatId);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful exiting of group.");
            System.out.println("[" + chatId + "] has exited a Group " + groupCode + ".");
        } else {
            System.out.println("Unsuccessful exiting in Groups.");
        }
    }

    public Boolean isUserInGroup(Integer chatId, String groupCode) throws SQLException {
        boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return false;
        if (isGroupCodeUnique(groupCode)) return false;

        System.out.println("PSQL.isUserInGroup()");

        String sql = "SELECT * FROM GroupUsers WHERE chat_id = ? AND group_code = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);
        statement.setString(2, groupCode);

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            return true;
        }

        return false;
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

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        PreparedStatement statement = connection.prepareStatement(sql);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            User user = this.convertResultSetToUser(resultSet);
            users.add(user);
        }

        return users;
    }

    public User getUserDataResultSet(int chatId) throws SQLException {
        System.out.println("PSQL.getUserDataResultSet()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Users WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        ResultSet resultSet = statement.executeQuery();
        User user = new User();

        while (resultSet.next()) {
            user = this.convertResultSetToUser(resultSet);
        }

        return user;
    }

    public User getUserDataResultSet(String userCode) throws SQLException {
        System.out.println("PSQL.getUserDataResultSet()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Users WHERE code = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, userCode);

        ResultSet resultSet = statement.executeQuery();
        User user = new User();

        while (resultSet.next()) {
            user = this.convertResultSetToUser(resultSet);
        }

        return user;
    }

    public Group getGroupDataResultSet(String groupNameOrCode) throws SQLException {
        System.out.println("PSQL.getGroupDataResultSet()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Groups WHERE code = ? OR name = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, groupNameOrCode);
        statement.setString(2, groupNameOrCode);

        ResultSet resultSet = statement.executeQuery();
        Group group = new Group();

        while (resultSet.next()) {
            group = this.convertResultSetToGroup(resultSet);
        }

        sql = "SELECT * FROM Users WHERE chat_id = ANY (SELECT chat_id FROM GroupUsers WHERE group_code = ?)";
        statement = connection.prepareStatement(sql);
        statement.setString(1, group.code);

        resultSet = statement.executeQuery();

        while (resultSet.next()) {
            User user = this.convertResultSetToUser(resultSet);
            group.users.add(user);
        }

        return group;
    }

    public List<User> getUsersFromGroup(String groupCode) throws SQLException {
        System.out.println("PSQL.getUsersFromGroup");
        String sql = "SELECT * FROM Users WHERE chat_id = ANY (SELECT chat_id FROM GroupUsers WHERE group_code = ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, groupCode);

        ResultSet resultSet = statement.executeQuery();
        List<User> users = new ArrayList<>();

        while (resultSet.next()) {
            User user = this.convertResultSetToUser(resultSet);
            users.add(user);
        }

        return users;
    }

    public List<User> getUsersFromGroupExceptUser(String groupCode, Integer chatId) throws SQLException {
        System.out.println("PSQL.getUsersFromGroup");
        String sql = "SELECT * FROM Users WHERE chat_id = ANY (SELECT chat_id FROM GroupUsers WHERE group_code = ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, groupCode);

        ResultSet resultSet = statement.executeQuery();
        List<User> users = new ArrayList<>();

        while (resultSet.next()) {
            User user = this.convertResultSetToUser(resultSet);
            if (!user.chatId.equals(chatId)) {
                users.add(user);
            }
        }
        return users;
    }

    public List<Group> getGroupsFromUser(Integer chatId) throws SQLException {
        System.out.println("PSQL.getGroupsFromUser()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Groups WHERE code = ANY (SELECT group_code FROM GroupUsers WHERE chat_id = ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        ResultSet resultSet = statement.executeQuery();
        List<Group> groups = new ArrayList<>();

        while (resultSet.next()) {
            Group group = this.convertResultSetToGroup(resultSet);
            groups.add(group);
        }

        return groups;
    }

    private User convertResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.chatId = resultSet.getInt("chat_id");
        user.name = resultSet.getString("name");
        user.code = resultSet.getString("code");
        user.dob = resultSet.getDate("dob");
        user.desc = resultSet.getString("description");
        user.groups = this.getGroupsFromUser(user.chatId);

        return user;
    }

    private Group convertResultSetToGroup(ResultSet resultSet) throws SQLException {
        Group group = new Group();
        group.name = resultSet.getString("name");
        group.code = resultSet.getString("code");
        group.createdBy = resultSet.getString("created_by");
        group.createdOn = resultSet.getDate("created_on");

        return group;
    }

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
        System.out.println("PSQL.isUserRegistered()");
        User user = this.getUserDataResultSet(chatId);
        return !User.isNull(user);
    }

    private static String getRandomFourDigitCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(9999);

        // this will convert any number sequence into 6 character.
        return String.format("%04d", number);
    }

    private Boolean isUserCodeUnique(String code) throws SQLException {
        User user = this.getUserDataResultSet(code);
        return User.isNull(user);
    }

    public Boolean isGroupCodeUnique(String code) throws SQLException {
        Group group = this.getGroupDataResultSet(code);
        return Group.isNull(group);
    }

    private String getNewUserCode(String name) throws SQLException {
        String code = "";

        do {
            String[] arr = name.split(" ");
            if (arr.length > 1) {
                for (String word : arr) {
                    code += word.charAt(0);
                }
            } else {
                if (name.length() >= 4) {
                    code = name.substring(0,4);
                } else {
                    code = name;
                }
            }

        } while (!isUserCodeUnique(code));

        return code + getRandomFourDigitCode();
    }

    private String getNewGroupCode(String name) {
        String code = "";

        String[] arr = name.split(" ");
        if (arr.length > 1) {
            for (String word : arr) {
                code += word.charAt(0);
            }
        } else {
            if (name.length() >= 4) {
                code = name.substring(0,4);
            } else {
                code = name;
            }
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
