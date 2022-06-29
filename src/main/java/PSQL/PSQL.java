package PSQL;

import org.telegram.telegrambots.meta.api.objects.Update;
import resource.Entity.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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

    public Boolean makeUserAdminInGroup(Integer chatId, String groupCode) throws SQLException {
        Boolean userExists = isUserRegistered(chatId);
        if (!userExists) return false;
        if (isGroupCodeUnique(groupCode)) return false;

        System.out.println("Make User an Admin to Group");

        String sql = "UPDATE GroupUsers SET is_admin = 'True' WHERE group_code = ? AND chat_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, groupCode);
        preparedStatement.setInt(2, chatId);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful in making admin in Groups.");
            System.out.println("[" + chatId + "] is now admin in Group " + groupCode + ".");
            return true;
        } else {
            System.out.println("Unsuccessful in making admin in Groups.");
            return false;
        }
    }

    public Boolean makeUserNormalInGroup(Integer chatId, String groupCode) throws SQLException {
        Boolean userExists = isUserRegistered(chatId);
        if (!userExists) return false;
        if (isGroupCodeUnique(groupCode)) return false;

        System.out.println("Make User Normal to Group");

        String sql = "UPDATE GroupUsers SET is_admin = 'False' WHERE group_code = ? AND chat_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, groupCode);
        preparedStatement.setInt(2, chatId);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful in making normal in Groups.");
            System.out.println("[" + chatId + "] is now normal in Group " + groupCode + ".");
            return true;
        } else {
            System.out.println("Unsuccessful in making normal in Groups.");
            return false;
        }
    }

    public Boolean addUserIntoBirthdayManagement(Integer chatId, Date birthday) throws SQLException {
        Boolean userExists = isUserRegistered(chatId);
        if (!userExists) return false;
        if (isUserAlreadyInBirthdayManagement(chatId)) return false;

        System.out.println("Adding User to BirthdayManagement");

        String sql = "INSERT INTO BirthdayManagement (chat_id, birthday, has_sent_initial) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, chatId);
        preparedStatement.setDate(2, birthday);
        preparedStatement.setBoolean(3, false);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful adding entry to bday mgmt");
            return true;
        } else {
            System.out.println("Unsuccessful entry in bday mgmt.");
            return false;
        }
    }

    public Boolean addMessage(String receiverCode, Integer chatId, String senderMessage) throws SQLException {
        System.out.println("PSQL.addMessage()");
        Boolean userExists = isUserRegistered(chatId);
        User otherUser = getUserDataResultSet(receiverCode);
        if (!userExists || User.isNull(otherUser)) return false;

        User user = getUserDataResultSet(chatId);

        if (isUserAlreadyInBirthdayManagement(otherUser.chatId) && isUserSameGroupAsOtherUser(chatId, otherUser.chatId)) {

            if (hasUserAlreadySentBirthdayMessage(receiverCode, user.code)) {
                String sql = "UPDATE Messages SET message = ? WHERE user_code_from = ? AND user_code_to = ? AND message_sent = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, senderMessage);
                preparedStatement.setString(2, user.code);
                preparedStatement.setString(3, otherUser.code);
                preparedStatement.setBoolean(4, false);

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successful adding entry to messages");
                    return true;
                } else {
                    System.out.println("Unsuccessful entry in messages.");
                    return false;
                }
            } else {
                String sql = "INSERT INTO Messages (user_code_from,user_code_to,message,message_sent,created_on) VALUES (?, ?, ?, ?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, user.code);
                preparedStatement.setString(2, otherUser.code);
                preparedStatement.setString(3, senderMessage);
                preparedStatement.setBoolean(4, false);
                preparedStatement.setDate(5, Date.valueOf(LocalDate.now()));

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successful adding entry to messages");
                    return true;
                } else {
                    System.out.println("Unsuccessful entry in messages.");
                    return false;
                }
            }
        }

        return false;
    }

    public Boolean addFile(String fileType, String filePath, Integer messageId) throws SQLException {
        String sql = "INSERT INTO Files (message_id,file_type,file_path,created_on) VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, messageId);
        preparedStatement.setString(2, fileType);
        preparedStatement.setString(3, filePath);
        preparedStatement.setDate(4, Date.valueOf(LocalDate.now()));

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful adding entry to Files");
            return true;
        } else {
            System.out.println("Unsuccessful entry in Files.");
            return false;
        }

    }

    private boolean hasUserAlreadySentBirthdayMessage(String receiverCode, String senderCode) throws SQLException {
        String sql = "SELECT * FROM Messages WHERE user_code_from = ? AND user_code_to = ? AND message_sent = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, senderCode);
        preparedStatement.setString(2, receiverCode);
        preparedStatement.setBoolean(3, false);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            Message message = this.convertResultSetToMessage(resultSet);

            if (!Message.isNull(message)) return true;
        }

        return false;
    }

    public Boolean addTestMessage(String receiverCode, Integer chatId, String senderMessage) throws SQLException {
        Boolean userExists = isUserRegistered(chatId);
        User otherUser = getUserDataResultSet(receiverCode);
        if (!userExists || User.isNull(otherUser)) return false;

        User user = getUserDataResultSet(chatId);

        if (isUserSameGroupAsOtherUser(chatId, otherUser.chatId)) {

            if (hasUserAlreadySentBirthdayMessage(receiverCode, user.code)) {
                String sql = "UPDATE Messages SET message = ? WHERE user_code_from = ? AND user_code_to = ? AND message_sent = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, senderMessage);
                preparedStatement.setString(2, user.code);
                preparedStatement.setString(3, otherUser.code);
                preparedStatement.setBoolean(4, false);

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successful adding entry to messages");
                    return true;
                } else {
                    System.out.println("Unsuccessful entry in messages.");
                    return false;
                }
            } else {
                String sql = "INSERT INTO Messages (user_code_from,user_code_to,message,message_sent,created_on) VALUES (?, ?, ?, ?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, user.code);
                preparedStatement.setString(2, otherUser.code);
                preparedStatement.setString(3, senderMessage);
                preparedStatement.setBoolean(4, false);
                preparedStatement.setDate(5, Date.valueOf(LocalDate.now()));

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Successful adding entry to messages");
                    return true;
                } else {
                    System.out.println("Unsuccessful entry in messages.");
                    return false;
                }
            }
        }

        return false;
    }

    private boolean isUserSameGroupAsOtherUser(Integer chatId, Integer otherChatId) throws SQLException {
        System.out.println("PSQL.isUserSameGroupAsOtherUser()");
        String sql = "SELECT * FROM GroupUsers gu1 INNER JOIN GroupUsers gu2 ON gu1.group_code = gu2.group_code WHERE gu1.chat_id = ? AND gu2.chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);
        statement.setInt(2, otherChatId);

        ResultSet resultSet = statement.executeQuery();

        boolean isSameGroup = false;

        while (resultSet.next()) {
            isSameGroup = true;
        }

        return isSameGroup;
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

    public void removeUserFromBirthdayManagement(Integer chatId) throws SQLException {
        boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return;
        if (!isUserAlreadyInBirthdayManagement(chatId)) return;

        String sql = "DELETE FROM BirthdayManagement WHERE chat_id= ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, chatId);

        int rowsInserted = preparedStatement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Successful removal of user from BirthdayManagement.");
            System.out.println("[" + chatId + "] has been removed from BdayMgmt.");
        } else {
            System.out.println("Unsuccessful removal from BdayMgmt.");
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

    public Boolean isUserOwnerOfGroup(Integer chatId, String groupCode) throws SQLException {
        boolean userExists = isUserRegistered(chatId);
        System.out.println(userExists);
        if (!userExists) return false;
        if (isGroupCodeUnique(groupCode)) return false;

        System.out.println("PSQL.isUserOwnerOfGroup()");

        User user = this.getUserDataResultSet(chatId);

        String sql = "SELECT * FROM Groups WHERE code = ? AND created_by = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, groupCode);
        statement.setString(2, user.code);

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            return true;
        }

        return false;
    }

    public void updateUserLatestText(Integer chatId, String text) throws SQLException {
        boolean userExists = isUserRegistered(chatId);
        if (!userExists) return;

        String sql = "UPDATE Users SET latest_text=? WHERE chat_id=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setString(1, text);
        statement.setInt(2, chatId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Latest Text] Update query successful.");
        } else {
            System.out.println("[Latest Text] Update query failed.");
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

    public void updateGroupDesc(String groupCode, String desc) throws SQLException {
        String sql = "UPDATE Groups SET description=? WHERE code=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setString(1, desc);
        statement.setString(2, groupCode);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Desc] Update query successful.");
        } else {
            System.out.println("[Desc] Update query failed.");
        }
    }

    public void updateHasSentInitialBirthdayManagement(Integer chatId, Boolean hasSentInitial) throws SQLException {
        String sql = "UPDATE BirthdayManagement SET has_sent_initial=? WHERE chat_id=? ";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setBoolean(1, hasSentInitial);
        statement.setInt(2, chatId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Birthday Mgmt for " + chatId + "] Update query successful.");
        } else {
            System.out.println("[Birthday Mgmt for " + chatId + "] Update query failed.");
        }
    }

    public void updateUserMessageToSent(Integer messageId) throws SQLException {
        String sql = "UPDATE Messages SET message_sent = ? WHERE message_id = ?";
        PreparedStatement statement= connection.prepareStatement(sql);
        statement.setBoolean(1, true);
        statement.setInt(2, messageId);
        int rowsInserted = statement.executeUpdate();

        if ((rowsInserted > 0)) {
            System.out.println("[Messages for " + messageId + "] Update query successful.");
        } else {
            System.out.println("[Messages for " + messageId + "] Update query failed.");
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

    public List<Group> getAllGroups() throws SQLException {
        System.out.println("PSQL.getAllGroups()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Groups";
        PreparedStatement statement = connection.prepareStatement(sql);

        ResultSet resultSet = statement.executeQuery();
        List<Group> groups = new ArrayList<>();

        while (resultSet.next()) {
            Group group = this.convertResultSetToGroup(resultSet);
            groups.add(group);
        }

        return groups;
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

    public BirthdayManagement getBirthdayManagementDataResultSet(Integer chatId) throws SQLException {
        System.out.println("PSQL.getBirthdayManagementDataResultSet()");
        // Obtaining user information from BdayMgmt
        String sql = "SELECT * FROM BirthdayManagement WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, chatId);

        ResultSet resultSet = statement.executeQuery();
        BirthdayManagement bdayMgmt = new BirthdayManagement();

        while (resultSet.next()) {
            bdayMgmt = this.convertResultSetToBirthdayManagement(resultSet);
        }

        return bdayMgmt;
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

    public List<User> getAdminsFromGroup(String groupCode) throws SQLException {
        System.out.println("PSQL.getAdminsFromGroup");
        String sql = "SELECT * FROM Users WHERE chat_id = ANY (SELECT chat_id FROM GroupUsers WHERE group_code = ? AND is_admin = ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, groupCode);
        statement.setBoolean(2, true);

        ResultSet resultSet = statement.executeQuery();
        List<User> users = new ArrayList<>();

        while (resultSet.next()) {
            User user = this.convertResultSetToUser(resultSet);
            users.add(user);
        }

        return users;
    }

    public List<Message> getUserMessages(String userCode) throws SQLException {
        System.out.println("PSQL.getUserMessages()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Messages WHERE user_code_to = ? AND message_sent = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, userCode);
        statement.setBoolean(2, false);

        ResultSet resultSet = statement.executeQuery();
        List<Message> messages = new ArrayList<>();

        while (resultSet.next()) {
            Message message = this.convertResultSetToMessage(resultSet);
            messages.add(message);
        }

        return messages;
    }

    public List<Message> getUserMessagesFromUsersOfGroup(String bdayUserCode, String groupCode) throws SQLException {
        //Get messages where usercode to is usercode and the sender of that msg is in the same group as the user calling this function
        System.out.println("PSQL.getUserMessagesFromUsersOfGroup()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Messages WHERE user_code_to = ? AND message_sent = ? and " +
                "user_code_from = ANY (SELECT code FROM Users WHERE chat_id = ANY (SELECT chat_id FROM GroupUsers " +
                "WHERE group_code = ? AND chat_id = ANY (SELECT chat_id FROM Users " +
                "WHERE code = ANY (SELECT user_code_from FROM Messages m WHERE user_code_to = ? AND message_sent = ?))))";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, bdayUserCode);
        statement.setBoolean(2, false);
        statement.setString(3, groupCode);
        statement.setString(4, bdayUserCode);
        statement.setBoolean(5, false);

        ResultSet resultSet = statement.executeQuery();
        List<Message> messages = new ArrayList<>();

        while (resultSet.next()) {
            Message message = this.convertResultSetToMessage(resultSet);
            messages.add(message);
        }

        return messages;
    }

    public List<File> getFilesFromMessage(Integer messageId) throws SQLException {
        System.out.println("PSQL.getFilesFromMessage()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Files WHERE message_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, messageId);

        ResultSet resultSet = statement.executeQuery();
        List<File> files = new ArrayList<>();

        while (resultSet.next()) {
            File file = this.convertResultSetToFile(resultSet);
            files.add(file);
        }

        return files;
    }


    public Message getMessageByMessageText(String receiverCode, String text) throws SQLException {
        System.out.println("PSQL.getMessageByMessageText()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Message WHERE user_code_to = ? message = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, receiverCode);
        statement.setString(2, text);

        ResultSet resultSet = statement.executeQuery();
        Message message = new Message();
        while (resultSet.next()) {
            message = this.convertResultSetToMessage(resultSet);
        }

        return message;
    }

    private File convertResultSetToFile(ResultSet resultSet) throws SQLException {
        File file = new File();
        file.id = resultSet.getInt("file_id");
        file.type = resultSet.getString("file_type");
        file.path = resultSet.getString("file_path");
        file.createdOn = resultSet.getDate("created_on");

        return file;
    }

    private Message convertResultSetToMessage(ResultSet resultSet) throws SQLException {
        Message message = new Message();
        message.id = resultSet.getInt("message_id");
        message.message = resultSet.getString("message");
        message.hasSent = resultSet.getBoolean("message_sent");
        message.createdOn = resultSet.getDate("created_on");
        message.userFrom = this.getUserDataResultSet(resultSet.getString("user_code_from"));
        message.userTo = this.getUserDataResultSet(resultSet.getString("user_code_to"));
        message.files = this.getFilesFromMessage(message.id);

        return message;
    }

    private User convertResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.chatId = resultSet.getInt("chat_id");
        user.name = resultSet.getString("name");
        user.code = resultSet.getString("code");
        user.dob = resultSet.getDate("dob");
        user.desc = resultSet.getString("description");
        user.latestText = resultSet.getString("latest_text");
        user.groups = this.getGroupsFromUser(user.chatId);

        return user;
    }

    private Group convertResultSetToGroup(ResultSet resultSet) throws SQLException {
        Group group = new Group();
        group.name = resultSet.getString("name");
        group.code = resultSet.getString("code");
        group.createdBy = resultSet.getString("created_by");
        group.createdOn = resultSet.getDate("created_on");
        group.description = resultSet.getString("description");

        return group;
    }

    private BirthdayManagement convertResultSetToBirthdayManagement(ResultSet resultSet) throws SQLException {
        BirthdayManagement bdayMgmt = new BirthdayManagement();
        bdayMgmt.user = this.getUserDataResultSet(resultSet.getInt("chat_id"));
        bdayMgmt.birthday = resultSet.getDate("birthday");
        bdayMgmt.hasSentInitialMessage = resultSet.getBoolean("has_sent_initial");

        return bdayMgmt;
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


    public boolean hasUserSentBdayMessageToUser(String senderCode, String receiverCode, BirthdayManagement bdayMgmt) throws SQLException {
        System.out.println("PSQL.hasUserSentBdayMessageToUser()");
        // Obtaining user information from USERS
        String sql = "SELECT * FROM Messages WHERE user_code_to = ? AND user_code_from = ? AND message_sent = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, receiverCode);
        statement.setString(2, senderCode);
        statement.setBoolean(3, false);

        ResultSet resultSet = statement.executeQuery();
        List<Message> messages = new ArrayList<>();

        while (resultSet.next()) {
            Message message = this.convertResultSetToMessage(resultSet);
            messages.add(message);
        }

        for (Message msg : messages) {
            if (msg.createdOn.toLocalDate().getYear() == bdayMgmt.birthday.toLocalDate().getYear()) return true;
        }

        return false;
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
