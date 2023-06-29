package spring.integration.core.utils

import org.opentest4j.AssertionFailedError
import spock.lang.Specification

class ResourceUtilitySpec extends Specification {

    def "GetResourceAsString is successful"() {
        when:
        def expected = "[" + System.lineSeparator() +
                "  \"ABC\"," + System.lineSeparator() +
                "  \"OPQ\"," + System.lineSeparator() +
                "  \"XYZ\"" + System.lineSeparator() +
                "]"
        def actual = ResourceUtility.getResourceAsString('referenced_value_test/values_002.json')

        then:
        expected == actual
    }

    def "GetResourceAsString is failure"() {
        when:
        ResourceUtility.getResourceAsString('referenced_value_test/values_00X.json')

        then:
        def ex = thrown(AssertionFailedError)
        ex.getMessage() == "'referenced_value_test/values_00X.json' is not found. Please make sure to exist it."
    }

}
