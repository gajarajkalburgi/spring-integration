package spring.integration.core.support.assertion

import com.fasterxml.jackson.databind.ObjectMapper
import org.opentest4j.AssertionFailedError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spring.integration.core.beans.AssertData
import spring.integration.core.support.TestException

class TextAssertionSupporterSpec extends Specification {

    private TextAssertionSupporter textAssertionSupporter = new TextAssertionSupporter(new ObjectMapper())

    def "test successful"() {
        given:
        def metadata = [:]
        def actualResponse = new ResponseEntity("TEXT_TEXT", HttpStatus.OK)
        def expectedResponse = [status: 200, body: "TEXT_TEXT"]

        when:
        textAssertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = textAssertionSupporter.assertList
        textAssertionSupporter.executeAssert()

        then:
        assertList.size() == 2
        assertList.get(0).actual == 200
        assertList.get(0).expected == 200
        !assertList.get(0).isRegex
        assertList.get(1).actual == "TEXT_TEXT"
        assertList.get(1).expected == "TEXT_TEXT"
        !assertList.get(1).isRegex
        noExceptionThrown()
    }

    def "test failure"() {
        given:
        def metadata = [:]
        def actualResponse = new ResponseEntity("TEXT_TEXT", HttpStatus.OK)
        def expectedResponse = [status: 200, body: "FOO_BAR"]

        when:
        textAssertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        textAssertionSupporter.executeAssert()

        then:
        thrown(TestException)
    }

    def "test successful with Regex"() {
        given:
        def metadata = [assert: [body: "REGEX"]]
        def actualResponse = new ResponseEntity("TEXT_001", HttpStatus.OK)
        def expectedResponse = [status: 200, body: '^.*_\\d+$']

        when:
        textAssertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = textAssertionSupporter.assertList
        textAssertionSupporter.executeAssert()

        then:
        assertList.size() == 2
        assertList.get(0).actual == 200
        assertList.get(0).expected == 200
        !assertList.get(0).isRegex
        assertList.get(1).actual == "TEXT_001"
        assertList.get(1).expected == '^.*_\\d+$'
        assertList.get(1).isRegex
        noExceptionThrown()
    }

    def "test failure with Regex"() {
        given:
        def metadata = [assert: [body: "REGEX"]]
        def actualResponse = new ResponseEntity("TEXT_001", HttpStatus.OK)
        def expectedResponse = [status: 200, body: '^\\d+_.*$']

        when:
        textAssertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        textAssertionSupporter.executeAssert()

        then:
        thrown(TestException)
    }

    def "test failure with NULL"() {
        given:
        def assertData = new AssertData(null, ".*", true)

        when:
        textAssertionSupporter.assertWithRegex(assertData)

        then:
        thrown(AssertionFailedError)
    }

    def "test with Ignore"() {
        given:
        def metadata = [assert: [body: "IGNORE"]]
        def actualResponse = new ResponseEntity("TEXT_TEXT", HttpStatus.OK)
        def expectedResponse = [status: 200, body: "FOO_BAR"]

        when:
        textAssertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = textAssertionSupporter.assertList
        textAssertionSupporter.executeAssert()

        then:
        assertList.size() == 1
        assertList.get(0).actual == 200
        assertList.get(0).expected == 200
        !assertList.get(0).isRegex
        noExceptionThrown()
    }
}
