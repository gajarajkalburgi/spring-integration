package spring.integration.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@AutoConfiguration
@RequiredArgsConstructor
@ComponentScan(basePackages = "spring.integration.wiremock")
@ConditionalOnProperty(prefix = "integration.wiremock", name = "port")
public class WireMockConfig {

    private static final String NAME = "wiremock";

    @Value("${integration.wiremock.port}")
    private int port;

    @Value("${integration.wiremock.initialMappings:#{null}}")
    private Optional<String> baseDirectoryPath;

    private final ObjectMapper objectMapper;

    @Bean(name = NAME)
    WireMockable wireMockable() {
        WireMockable wireMockable = new WireMockable(port, objectMapper);
        baseDirectoryPath.ifPresent(wireMockable::initWithMappingFiles);
        return wireMockable;
    }
}
