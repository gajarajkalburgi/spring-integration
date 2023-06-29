package spring.integration.core.support.mock;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import spring.integration.core.support.TestException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockSupporter {

    private final Map<String, Mockable> mockContainer;

    @PostConstruct
    public void init() {
        log.info("(Integration) Mockable Loaded : {}", mockContainer.keySet());
    }

    public void addMockable(String name, Mockable mockable) {
        mockContainer.put(name, mockable);
    }

    private Mockable getMockable(String tagName) {
        return Optional.ofNullable(mockContainer.get(tagName)).orElseThrow(() -> new TestException("Mockable not found for " + tagName));
    }

    private List<Mockable> getAllMockable() {
        return mockContainer.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public void setup(Map<String, Object> setupData) {
        if (Objects.nonNull(setupData)) {
            setupData.forEach((key, value) -> getMockable(key).setup((List) value));
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanUp(Map<String, Object> setupData, Map<String, Object> cleanUpData) {
        if (Objects.nonNull(setupData)) {
            setupData.forEach((key, value) -> getMockable(key).defaultCleanUp((List) value));
        }

        if (Objects.nonNull(cleanUpData)) {
            cleanUpData.forEach((key, value) -> getMockable(key).cleanup((List) value));
        }
    }

    public void destroy() {
        getAllMockable().forEach(Mockable::destroy);
    }

    public void executeMockableAssertion(Map<String, Object> setupData) {
        if (Objects.nonNull(setupData)) {
            setupData.forEach((key, value) -> {
                Mockable mockable = getMockable(key);
                if (mockable instanceof MockableAssertion) {
                    // noinspection unchecked
                    ((MockableAssertion) mockable).verifyUtilizedMock(value);
                }
            });
        }
    }
}
