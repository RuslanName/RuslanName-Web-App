package mainFiles.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Database {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public int getNextId(String tableName) {
        String selectMaxIdQuery = "SELECT COALESCE(MAX(id), 0) + 1 FROM %s;".formatted(tableName);
        return jdbcTemplate.queryForObject(selectMaxIdQuery, Integer.class);
    }

    public void updateDatabaseSequences(String tableName) {
        String tempTable = "temp_" + tableName;
        String createTempTableQuery = (
                "CREATE TEMP TABLE %s AS " +
                        "SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS new_id " +
                        "FROM %s;").formatted(tempTable, tableName);
        jdbcTemplate.execute(createTempTableQuery);

        String updateQuery = (
                "UPDATE %s " +
                        "SET id = (SELECT new_id FROM %s WHERE %s.id = %s.id);").formatted(
                tableName, tempTable, tableName, tempTable);
        jdbcTemplate.execute(updateQuery);

        String dropTempTableQuery = "DROP TABLE %s;".formatted(tempTable);
        jdbcTemplate.execute(dropTempTableQuery);

        String resetSequenceQuery = "UPDATE %s SET id = (SELECT COALESCE(MAX(id), 0) + ROW_NUMBER() OVER (ORDER BY id) FROM %s) WHERE id > 0;".formatted(
                tableName, tableName);
        jdbcTemplate.execute(resetSequenceQuery);
    }
}
