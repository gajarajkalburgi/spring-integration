package spring.integration.core.support.mock


import spock.lang.Specification
import spring.integration.core.support.TestException

class MockSupporterSpec extends Specification {

    def TARGET = new MockSupporter(new HashMap<String, Mockable>())

    def MOCK_OF_MOCK = Mock(Mockable)

    class MockOfMockableAssertion implements Mockable<Object, Object>, MockableAssertion<Map<String, String>> {
        @Override
        void setup(List<Object> elements) {
        }

        @Override
        void defaultCleanUp(List<Object> elements) {
        }

        @Override
        void cleanup(List<Object> elements) {
        }

        @Override
        void destroy() {
        }

        @Override
        void verifyUtilizedMock(Map<String, String> input) {
        }
    }

    def MOCK_OF_MOCK_ASSERTION = Mock(MockOfMockableAssertion)

    def "Test for setup with Non Null"() {
        when:
        TARGET.addMockable("Test1", MOCK_OF_MOCK)
        TARGET.addMockable("Test2", MOCK_OF_MOCK)
        TARGET.setup(["Test1": [0, 1, 2], "Test2": [0, 1, 2]])

        then:
        2 * MOCK_OF_MOCK.setup([0, 1, 2])

    }

    def "Test for setup with Null"() {
        when:
        TARGET.addMockable("Test1", MOCK_OF_MOCK)
        TARGET.addMockable("Test2", MOCK_OF_MOCK)
        TARGET.setup(null)

        then:
        0 * MOCK_OF_MOCK.setup([0, 1, 2])

    }

    def "Test for cleanUp with Non Null"() {
        when:
        TARGET.addMockable("Test1", MOCK_OF_MOCK)
        TARGET.addMockable("Test2", MOCK_OF_MOCK)
        TARGET.cleanUp(["Test1": [0, 1, 2], "Test2": [0, 1, 2]], ["Test1": [0, 1, 2], "Test2": [0, 1, 2]])

        then:
        2 * MOCK_OF_MOCK.defaultCleanUp([0, 1, 2])
        2 * MOCK_OF_MOCK.cleanup([0, 1, 2])

    }

    def "Test for cleanUp with Null"() {
        when:
        TARGET.addMockable("Test1", MOCK_OF_MOCK)
        TARGET.addMockable("Test2", MOCK_OF_MOCK)
        TARGET.cleanUp(null, null)

        then:
        0 * MOCK_OF_MOCK.defaultCleanUp([0, 1, 2])
        0 * MOCK_OF_MOCK.cleanup([0, 1, 2])

    }

    def "Test for destroy"() {
        when:
        TARGET.addMockable("Test1", MOCK_OF_MOCK)
        TARGET.addMockable("Test2", MOCK_OF_MOCK)
        TARGET.destroy()

        then:
        2 * MOCK_OF_MOCK.destroy()

    }

    def "Exception with undefined function"() {
        when:
        // Try to execute undefined function.
        TARGET.setup(["NotExist": "NotExist"])

        then:
        def ex = thrown(TestException)
        ex.getMessage() == "Mockable not found for NotExist"
    }

    def "Test for executeMockableAssertion"() {
        when:
        TARGET.addMockable("Test1", MOCK_OF_MOCK)
        TARGET.addMockable("Test2", MOCK_OF_MOCK_ASSERTION)
        TARGET.executeMockableAssertion(["Test1": ["k1": "v1"], "Test2": ["k2": "v2"]])

        then:
        1 * MOCK_OF_MOCK_ASSERTION.verifyUtilizedMock(["k2": "v2"])

    }
}
