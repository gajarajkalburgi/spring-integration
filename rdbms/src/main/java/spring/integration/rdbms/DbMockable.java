package spring.integration.rdbms;

import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.integration.core.support.mock.Mockable;
import spring.integration.rdbms.truncation.TruncationSupport;

@Slf4j
public class DbMockable implements Mockable<String, String> {

	private JdbcTemplate jdbcTemplate;
	private TruncationSupport truncationSupport;

	/**
	 * Constructor.
	 *
	 * @param dataSource        dataSource.
	 * @param truncationSupport truncationSupport.
	 */
	public DbMockable(DataSource dataSource, TruncationSupport truncationSupport) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.truncationSupport = truncationSupport;
	}

	public void init(String sql) {
		log.info("(Integration) DB-Support init : {}.", sql);
		jdbcTemplate.execute(sql);
	}

	@Override
	public void setup(List<String> elements) {
		elements.forEach(sql -> {
			log.info("(Integration) DB-Support setup : {}.", sql);
			jdbcTemplate.execute(sql);
		});
	}

	@Override
	public void defaultCleanUp(List<String> elements) {
		truncationSupport.truncate();
	}

	@Override
	public void cleanup(List<String> elements) {
		elements.forEach(sql -> {
			log.info("(Integration) DB-Support cleanup : {}.", sql);
			jdbcTemplate.execute(sql);
		});
	}

	@Override
	public void destroy() {
		// Do Nothing
	}

}
