package mainFiles.database.tables.differentState;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "different_states_data")
public class DifferentState {

    @Id
    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "state", columnDefinition = "INTEGER")
    private Integer state;

    @Column(name = "step_1", columnDefinition = "VARCHAR(255)")
    private String step_1;

    @Column(name = "step_2", columnDefinition = "VARCHAR(255)")
    private String step_2;

    @Column(name = "step_3", columnDefinition = "VARCHAR(255)")
    private String step_3;

    @Column(name = "step_4", columnDefinition = "VARCHAR(255)")
    private String step_4;

    @Column(name = "step_5", columnDefinition = "VARCHAR(255)")
    private String step_5;
}