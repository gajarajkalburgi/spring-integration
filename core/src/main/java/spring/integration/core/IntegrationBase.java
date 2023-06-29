package spring.integration.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.StringUtils;
import spring.integration.core.support.IntegrationRestTemplate;
import spring.integration.core.support.ResponseHistoryService;
import spring.integration.core.support.assertion.AssertionSupporterFactory;
import spring.integration.core.support.mock.MockSupporter;
import spring.integration.core.utils.ResourceUtility;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Slf4j
public abstract class IntegrationBase {

    protected static final String JSON_FILE_FILTER = ".+\\.(?i)json$";

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path:}")
    private String serverContextPath;

    @Autowired
    private IntegrationRestTemplate integrationRestTemplate;

    @Autowired
    private MockSupporter mockSupporter;

    @Autowired
    private ResponseHistoryService responseHistoryService;

    @Autowired
    private ObjectMapper integrationObjectMapper;

    /**
     * Get directory path of test data location.
     */
    protected abstract String getTestDataLocation();

    /**
     * Get include pattern of test specification file.
     * The default is pattern which has ".json" as suffix.
     * Need to override when it's needed to change.
     */
    protected String getIncludePattern() {
        return JSON_FILE_FILTER;
    }


    /**
     * Get boolean to execute mockable assertion or not.
     */
    protected boolean isMockableAssertionMandatory() {
        return false;
    }

    /**
     * Setup parameterized test with test specification data.
     */
    @DisplayName("Test execution with specification files")
    @TestFactory
    Stream<DynamicTest> setup() {
        IntegrationExecutor integrationExecutor = new IntegrationExecutor(integrationObjectMapper, mockSupporter, isMockableAssertionMandatory(), responseHistoryService);
        AssertionSupporterFactory assertionFactory =
                new AssertionSupporterFactory(port, serverContextPath, integrationObjectMapper, integrationRestTemplate, responseHistoryService);
        if (!StringUtils.isEmpty(System.getProperty("integration.interactive.port"))) {
            integrationExecutor.execute(assertionFactory, Integer.parseInt(System.getProperty("integration.interactive.port")));
            return Stream.empty();
        } else {
            String testDataLocation = getTestDataLocation();
            String includePattern = getIncludePattern();
            log.info(includePattern);
            List<File> specificationFileList = ResourceUtility.getFilesRecursively(testDataLocation, includePattern);
            return specificationFileList.stream()
                    .map(file -> {
                        String displayName = file.getPath().replaceAll(".*" + testDataLocation, "");
                        return dynamicTest(displayName, () -> integrationExecutor.execute(assertionFactory, file));
                    });
        }
    }

    @AfterEach
    void destroyMock() {
        mockSupporter.destroy();
    }

}
