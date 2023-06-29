package spring.integration.core


import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.util.TestSocketUtils
import spock.lang.Specification
import spring.integration.core.support.ResponseHistoryService
import spring.integration.core.support.assertion.AssertionSupporterFactory
import spring.integration.core.support.mock.MockSupporter

import java.util.stream.Stream

class IntegrationExecutorSpec extends Specification {

    def objectMapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS)

    def MOCK_OF_MOCK_SUPPORTER = Mock(MockSupporter)

    def MOCK_OF_RESPONSE_HISTORY = Mock(ResponseHistoryService)

    def FILE_FOR_TEST = new File(this.getClass().getResource("/integration.json").getFile())

    def "execute with mockableAssertionMandatory is false"() {
        when:
        def target = new IntegrationExecutor(objectMapper, MOCK_OF_MOCK_SUPPORTER, false, MOCK_OF_RESPONSE_HISTORY)
        def assertionSupporterFactory = Stub(AssertionSupporterFactory) {
            create(_ as Map) >> Stream.of()
        }
        target.execute(assertionSupporterFactory, FILE_FOR_TEST)

        then:
        1 * MOCK_OF_MOCK_SUPPORTER.setup(_ as Map)
        0 * MOCK_OF_MOCK_SUPPORTER.executeMockableAssertion(_ as Map)
        1 * MOCK_OF_MOCK_SUPPORTER.cleanUp(_ as Map, _ as Map)
        1 * MOCK_OF_RESPONSE_HISTORY.clear()
    }

    def "execute with mockableAssertionMandatory is true"() {
        when:
        def target = new IntegrationExecutor(objectMapper, MOCK_OF_MOCK_SUPPORTER, true, MOCK_OF_RESPONSE_HISTORY)
        def assertionSupporterFactory = Stub(AssertionSupporterFactory) {
            create(_ as Map) >> Stream.of()
        }
        target.execute(assertionSupporterFactory, FILE_FOR_TEST)

        then:
        1 * MOCK_OF_MOCK_SUPPORTER.setup(_ as Map)
        1 * MOCK_OF_MOCK_SUPPORTER.executeMockableAssertion(_ as Map)
        1 * MOCK_OF_MOCK_SUPPORTER.cleanUp(_ as Map, _ as Map)
        1 * MOCK_OF_RESPONSE_HISTORY.clear()
    }

    def "stop interactive mode with 'quit'"() {
        when:
        def target = new IntegrationExecutor(objectMapper, MOCK_OF_MOCK_SUPPORTER, true, MOCK_OF_RESPONSE_HISTORY)
        def assertionSupporterFactory = Stub(AssertionSupporterFactory) {
            create(_ as Map) >> Stream.of()
        }
        def availablePort = TestSocketUtils.findAvailableTcpPort()
        Thread.start { target.execute(assertionSupporterFactory, availablePort) }
        while (true) {
            try {
                def socket = new Socket("localhost", availablePort)
                def writer = new PrintWriter(socket.getOutputStream(), true)
                writer.println("quit")
                break
            } catch (IOException ignored) {
                Thread.sleep(1000)
            }
        }

        then:
        notThrown(Throwable)
    }

    def "interactive mode with valid input"() {
        when:
        def target = new IntegrationExecutor(objectMapper, MOCK_OF_MOCK_SUPPORTER, true, MOCK_OF_RESPONSE_HISTORY)
        def assertionSupporterFactory = Stub(AssertionSupporterFactory) {
            create(_ as Map) >> Stream.of()
        }
        def availablePort = TestSocketUtils.findAvailableTcpPort()
        Thread.start { target.execute(assertionSupporterFactory, availablePort) }
        while (true) {
            try {
                def socket = new Socket("localhost", availablePort)
                def writer = new PrintWriter(socket.getOutputStream(), true)
                writer.println(this.getClass().getResource("/integration.json").getPath())
                break
            } catch (IOException ignored) {
                Thread.sleep(1000)
            }
        }

        then:
        notThrown(Throwable)
    }
}
