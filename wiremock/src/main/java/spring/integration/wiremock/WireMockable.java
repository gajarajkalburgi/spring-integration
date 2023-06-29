package spring.integration.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.springframework.util.StringUtils;
import spring.integration.core.support.TestException;
import spring.integration.core.support.mock.Mockable;
import spring.integration.core.support.mock.MockableAssertion;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WireMockable implements Mockable<Map<String, Object>, Void>, MockableAssertion<List<Map<String, Object>>> {

    private final ObjectMapper objectMapper;

    private WireMockServer mockServer;

    /**
     * Constructor for WireMockable.
     *
     * @param port         Port number to activate mocked endpoint
     * @param objectMapper To test spec JSON to Object
     */
    public WireMockable(int port, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        mockServer = new WireMockServer(WireMockConfiguration.options().port(port).notifier(new ConsoleNotifier(false)));
    }

    /**
     * Initialize WireMockServer with mapping files.
     *
     * @param baseDirectoryPath directory path which contains configuration files of wiremock
     */
    void initWithMappingFiles(String baseDirectoryPath) {
        mockServer.loadMappingsUsing(new JsonFileMappingsSource(new SingleRootFileSource(baseDirectoryPath), new FilenameMaker()));
    }

    @Override
    public void setup(List<Map<String, Object>> elements) {
        elements.forEach(element -> mockServer.addStubMapping(buildStub(element)));
        if (!mockServer.isRunning()) {
            mockServer.start();
        }
    }

    @Override
    public void defaultCleanUp(List<Map<String, Object>> elements) {
        cleanup(null);
    }

    @Override
    public void cleanup(List<Void> elements) {
        mockServer.getStubMappings().forEach(stubMapping -> mockServer.removeStub(stubMapping));
        mockServer.resetRequests();
    }

    @Override
    public void destroy() {
    }

    /**
     * Assert actual mock utilization.
     * TestException will be thrown when request has been found to not mocked endpoint.
     */
    @Override
    public void verifyUtilizedMock(List<Map<String, Object>> input) {
        // Check unmatched request.　TestException will be thrown when request has been found to not mocked endpoint.
        String unmatchedRequestUrls = mockServer.findUnmatchedRequests().getRequests().stream()
                .map(LoggedRequest::getUrl)
                .collect(Collectors.joining(", "));
        if (!StringUtils.isEmpty(unmatchedRequestUrls)) {
            throw new TestException(String.format("Detect request to non-present mocked endpoint : %s. please check your test specification file.", unmatchedRequestUrls));
        }
    }

    private StubMapping buildStub(Map<String, Object> element) {
        try {
            StubMapping stubMapping = StubMapping.buildFrom(objectMapper.writeValueAsString(element));
            stubMapping.setPersistent(false);
            return stubMapping;
        } catch (Exception ex) {
            throw new TestException("Exception creating stub.", ex);
        }
    }
}
