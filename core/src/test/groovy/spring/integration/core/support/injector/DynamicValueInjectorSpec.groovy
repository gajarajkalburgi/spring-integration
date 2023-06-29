package spring.integration.core.support.injector


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spring.integration.core.support.TestException

class DynamicValueInjectorSpec extends Specification {

    @Shared
    def LIST_DOES_NOT_HAVE_INJECTION = ["abc", "efg", "hij"]
    @Shared
    def LIST_HAS_INJECTION = ["```increment(1)```", "```increment(2)```", "```increment(3)```"]
    @Shared
    def NESTED_LIST_HAS_INJECTION = [LIST_HAS_INJECTION, LIST_HAS_INJECTION, LIST_HAS_INJECTION]
    @Shared
    def MAP_DOES_NOT_HAVE_INJECTION = ["test": "test"]
    @Shared
    def MAP_HAS_INJECTION = ["test": "```increment(1)```"]
    @Shared
    def LIST_HAS_MAP_INJECTION = [MAP_HAS_INJECTION, MAP_HAS_INJECTION, MAP_HAS_INJECTION]
    @Shared
    def MAP_LIST_HAS_INJECTION = ["test": LIST_HAS_INJECTION]
    @Shared
    def NESTED_MAP_HAS_INJECTION = ["test": MAP_HAS_INJECTION]
    @Shared
    def MIXED_NESTED_OBJECT = ["test": MAP_HAS_INJECTION, "test2": MAP_HAS_INJECTION, "test3": ["test": MAP_HAS_INJECTION], "testArray": [MAP_DOES_NOT_HAVE_INJECTION, MAP_HAS_INJECTION, MAP_HAS_INJECTION, MAP_HAS_INJECTION, MAP_HAS_INJECTION], "test4": MAP_DOES_NOT_HAVE_INJECTION]
    @Shared
    def TOO_NESTED_MAP = ["test": ["test": ["test": ["test": ["test": ["test": ["test": ["test": MAP_HAS_INJECTION]]]]]]]]
    @Shared
    def TOO_NESTED_LIST = [[[[[[[NESTED_LIST_HAS_INJECTION]]]]]]]

    @Shared
    def SCRIPT_STRING = "def increment(num) { ++num }"

    @Shared
    def TEST_TARGET = new DynamicValueInjector(SCRIPT_STRING)

    @Unroll
    def "Parameterized test with #inputObject"() {
        expect:
        TEST_TARGET.inject(inputObject)
        inputObject == exptected // Because parameter object will be updated when it has injection target.

        where:
        test | inputObject                  || exptected
        1    | new HashMap<>()              || [:]
        2    | new ArrayList<>()            || []
        3    | LIST_DOES_NOT_HAVE_INJECTION || ["abc", "efg", "hij"]
        4    | MAP_DOES_NOT_HAVE_INJECTION  || ["test": "test"]
        4    | MAP_HAS_INJECTION            || ["test": 2]
        5    | LIST_HAS_INJECTION           || [2, 3, 4]
        6    | MAP_LIST_HAS_INJECTION       || ["test": [2, 3, 4]]
        7    | NESTED_MAP_HAS_INJECTION     || ["test": ["test": 2]]
        8    | NESTED_LIST_HAS_INJECTION    || [[2, 3, 4], [2, 3, 4], [2, 3, 4]]
        9    | LIST_HAS_MAP_INJECTION       || [["test": 2], ["test": 2], ["test": 2]]
        10   | MIXED_NESTED_OBJECT          || ["test": ["test": 2], "test2": ["test": 2], "test3": ["test": ["test": 2]], "testArray": [["test": "test"], ["test": 2], ["test": 2], ["test": 2], ["test": 2]], "test4": ["test": "test"]]
        11   | TOO_NESTED_MAP               || ["test": ["test": ["test": ["test": ["test": ["test": ["test": ["test": ["test": 2]]]]]]]]]
        12   | TOO_NESTED_LIST              || [[[[[[[[[2, 3, 4], [2, 3, 4], [2, 3, 4]]]]]]]]]
    }

    def "Nothing to happen when DynamicParameterSupportSpec#inject get null"() {
        when:
        def input = null
        TEST_TARGET.inject(input)

        then:
        input == null
    }

    def "Exception with undefined function"() {
        when:
        // Try to execute undefined function.
        TEST_TARGET.inject(["test": "```decrement(1)```"])

        then:
        def ex = thrown(TestException)
        ex.getMessage() == "Failed to create dynamic value with test : ```decrement(1)```"
    }

    def "Exception with constructor"() {
        when:
        new DynamicValueInjector("FooBar")

        then:
        def ex = thrown(TestException)
        ex.getMessage() == "Failed to load Groovy script."
    }
}
