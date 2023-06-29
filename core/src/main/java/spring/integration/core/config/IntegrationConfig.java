package spring.integration.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import spring.integration.core.support.AvailablePortPropertySource;
import spring.integration.core.support.IntegrationRestTemplate;
import spring.integration.core.support.ResponseHistoryService;
import spring.integration.core.support.mock.MockSupporter;
import spring.integration.core.support.mock.Mockable;

import java.util.Map;

@Configuration
public class IntegrationConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public AvailablePortPropertySource availablePortPropertySource(ConfigurableEnvironment environment) {
        AvailablePortPropertySource availablePortPropertySource = new AvailablePortPropertySource();
        environment.getPropertySources().addLast(availablePortPropertySource);
        return availablePortPropertySource;
    }

    @Bean
    public IntegrationRestTemplate integrationRestTemplate() {
        return new IntegrationRestTemplate();
    }

    @Bean
    public MockSupporter mockSupporter(Map<String, Mockable> mockables) {
        return new MockSupporter(mockables);
    }

    @Bean
    public ObjectMapper integrationObjectMapper() {

        return new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS);
    }

    @Bean
    public ResponseHistoryService responseHistoryService(ObjectMapper integrationObjectMapper) {
        return new ResponseHistoryService(integrationObjectMapper);
    }
}
