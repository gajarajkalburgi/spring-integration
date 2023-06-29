package spring.integration.core.property.port

import spock.lang.Specification
import spring.integration.core.support.AvailablePortPropertySource

class AvailablePortPropertySourceSpec extends Specification {

    def target = new AvailablePortPropertySource()

    def "test getProperty"() {
        when:
        def result1 = target.getProperty("test")
        def result2 = target.getProperty("port.test")
        def result3 = target.getProperty("port.test")

        then:
        result1 == null
        result2 == result3
    }

}
