package mainFiles.database.tables.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;



@Getter
@Setter
@Entity(name = "usersData")
public class User {

    @Id
    private Long chatId;

    private String userName;
    private String firstName;
    private String lastName;
    private String userData;
    private Timestamp registeredAt;

    private int accessRights;

    @Override
    public String toString() {
        return "User {" +
                "chatId = \"" + chatId + "\"" +
                ", userName = \"" + userName + "\"" +
                ", firstName = \"" + firstName + "\"" +
                ", lastName = \"" + lastName + "\"" +
                ", userData = \"" + userData + "\"" +
                ", registeredAt = \"" + registeredAt + "\"" +
                ", accessRights = \"" + accessRights + "\"" +
                "}";
    }
}

// Rights id
// 1 - administrator
// 2 - pickup point
// 3 - user
