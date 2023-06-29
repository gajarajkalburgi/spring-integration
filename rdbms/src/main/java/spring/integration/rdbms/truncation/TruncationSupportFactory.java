package spring.integration.rdbms.truncation;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.integration.core.support.TestException;
import spring.integration.rdbms.truncation.h2.H2TruncationSupport;
import spring.integration.rdbms.truncation.hsql.HsqlTruncationSupport;

public class TruncationSupportFactory {

	public static final String HSQLDB = "HSQLDB";
	public static final String H2 = "H2";

	/**
	 * Create Truncation Support object.
	 *
	 * @param dataSourceUrl dataSourceUrl.
	 * @param dataSource    dataSource.
	 *
	 * @return TruncationSupport.
	 */
	public static TruncationSupport getTruncateSupport(String dataSourceUrl, DataSource dataSource) {
		if (dataSourceUrl.toUpperCase().contains(HSQLDB)) {
			return new HsqlTruncationSupport(new JdbcTemplate(dataSource));
		} else if (dataSourceUrl.toUpperCase().contains(H2)) {
			return new H2TruncationSupport(new JdbcTemplate(dataSource));
		} else {
			throw new TestException("UnSupported database type detected.");
		}
	}
}
