package spring.integration.rdbms;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import spring.integration.core.utils.ResourceUtility;
import spring.integration.rdbms.truncation.TruncationSupportFactory;

import javax.sql.DataSource;
import java.util.Optional;

@AutoConfiguration
@RequiredArgsConstructor
@ComponentScan(basePackages = "spring.integration.rdbms")
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class DbMockConfig {

    private static final String NAME = "database";

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${integration.database.init:#{null}}")
    private Optional<String> initialFilePath;

    @Bean(name = NAME)
    public DbMockable databaseMockable() {
        DbMockable databaseMockable = new DbMockable(dataSource, TruncationSupportFactory.getTruncateSupport(dataSourceUrl, dataSource));
        initialFilePath.ifPresent(path -> databaseMockable.init(ResourceUtility.getResourceAsString(path)));
        return databaseMockable;
    }
}
