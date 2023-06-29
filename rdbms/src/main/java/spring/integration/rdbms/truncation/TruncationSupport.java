package spring.integration.rdbms.truncation;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
public abstract class TruncationSupport {

	private static final String TABLE_TRUNCATION_SQL = "DELETE FROM ";

	protected void truncate(JdbcTemplate jdbcTemplate, List<String> tables, String disableReferentialIntegrity, String enableReferentialIntegrity) {
		jdbcTemplate.execute(disableReferentialIntegrity);
		tables.stream().map(tableName -> TABLE_TRUNCATION_SQL.concat(tableName))
				.forEach(sql -> {
					log.info("(Integration) DB-Support default cleanup : {}.", sql);
					jdbcTemplate.execute(sql);
				});
		jdbcTemplate.execute(enableReferentialIntegrity);
	}

	public abstract void truncate();
}
