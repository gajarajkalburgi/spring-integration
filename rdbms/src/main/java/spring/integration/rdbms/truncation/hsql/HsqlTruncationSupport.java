package spring.integration.rdbms.truncation.hsql;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.integration.rdbms.truncation.TruncationSupport;

@AllArgsConstructor
public class HsqlTruncationSupport extends TruncationSupport {

    private static final String TABLE_LIST_SQL = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE NOT IN ('SYSTEM TABLE', 'VIEW') AND TABLE_SCHEMA NOT LIKE 'SYSTEM_%'";
    private static final String DISABLE_INTEGRITY = "SET DATABASE REFERENTIAL INTEGRITY FALSE";
    private static final String ENABLE_INTEGRITY = "SET DATABASE REFERENTIAL INTEGRITY TRUE";

    private JdbcTemplate jdbcTemplate;

    @Override
    public void truncate() {
        truncate(jdbcTemplate, jdbcTemplate.queryForList(TABLE_LIST_SQL, String.class), DISABLE_INTEGRITY, ENABLE_INTEGRITY);
    }
}
