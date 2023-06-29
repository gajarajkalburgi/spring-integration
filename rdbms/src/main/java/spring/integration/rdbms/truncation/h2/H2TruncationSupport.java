package spring.integration.rdbms.truncation.h2;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.integration.rdbms.truncation.TruncationSupport;

@AllArgsConstructor
public class H2TruncationSupport extends TruncationSupport {

    private static final String TABLE_LIST_SQL = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'TABLE'";
    private static final String DISABLE_INTEGRITY = "SET REFERENTIAL_INTEGRITY FALSE";
    private static final String ENABLE_INTEGRITY = "SET REFERENTIAL_INTEGRITY TRUE";

    private JdbcTemplate jdbcTemplate;

    @Override
    public void truncate() {
        truncate(jdbcTemplate, jdbcTemplate.queryForList(TABLE_LIST_SQL, String.class), DISABLE_INTEGRITY, ENABLE_INTEGRITY);
    }
}
