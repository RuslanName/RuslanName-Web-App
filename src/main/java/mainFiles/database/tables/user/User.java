package mainFiles.database.tables.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "users_data")
public class User {

    @Id
    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "user_name", columnDefinition = "VARCHAR(255)")
    private String userName;

    @Column(name = "first_name", columnDefinition = "VARCHAR(255)")
    private String firstName;

    @Column(name = "last_name", columnDefinition = "VARCHAR(255)")
    private String lastName;

    @Column(name = "user_data", columnDefinition = "VARCHAR(255)")
    private String userData;

    @Column(name = "default_pickup_point_id", columnDefinition = "INTEGER")
    private Integer defaultPickupPointId;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;

    @Column(name = "access_rights", columnDefinition = "INTEGER")
    private Integer accessRights;

    @Column(name = "block", columnDefinition = "BOOLEAN")
    private boolean block;
}
