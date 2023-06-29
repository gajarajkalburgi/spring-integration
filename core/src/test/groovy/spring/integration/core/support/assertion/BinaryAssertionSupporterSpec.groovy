package spring.integration.core.support.assertion

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spring.integration.core.support.TestException

class BinaryAssertionSupporterSpec extends Specification {

    private BinaryAssertionSupporter binaryAssertionSupporter = new BinaryAssertionSupporter(new ObjectMapper())

    def testBytesActual = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("media/spring.png"))

    def "test successful"() {
        given:
        def actualResponse = new ResponseEntity(testBytesActual, HttpStatus.OK)
        def expectedResponse = [status: 200, body: "@media/spring.png"]

        when:
        binaryAssertionSupporter.setUpAssertList([:], actualResponse, expectedResponse)
        def assertList = binaryAssertionSupporter.assertList
        binaryAssertionSupporter.executeAssert()

        then:
        assertList.size() == 2
        assertList.get(0).actual == 200
        assertList.get(0).expected == 200
        assertList.get(1).actual == testBytesActual
        assertList.get(1).expected == "@media/spring.png"
        noExceptionThrown()
    }

    def "test failure"() {
        given:
        def actualResponse = new ResponseEntity("Test Response".getBytes(), HttpStatus.OK)
        def expectedResponse = [status: 200, body: "@media/spring.png"]

        when:
        binaryAssertionSupporter.setUpAssertList([:], actualResponse, expectedResponse)
        binaryAssertionSupporter.executeAssert()

        then:
        thrown(TestException)
    }

    def "test failure with not exist path"() {
        given:
        def actualResponse = new ResponseEntity(testBytesActual, HttpStatus.OK)
        def expectedResponse = [status: 200, body: "@media/running.gif"]

        when:
        binaryAssertionSupporter.setUpAssertList([:], actualResponse, expectedResponse)
        binaryAssertionSupporter.executeAssert()

        then:
        def ex = thrown(TestException)
        ex.getMessage() == "@media/running.gif is not found. Please check the path in your test spec."
    }

    def "test failure with invalid file path"() {
        given:
        def actualResponse = new ResponseEntity(testBytesActual, HttpStatus.OK)
        def expectedResponse = [status: 200, body: "media/running.gif"]

        when:
        binaryAssertionSupporter.setUpAssertList([:], actualResponse, expectedResponse)
        binaryAssertionSupporter.executeAssert()

        then:
        thrown(TestException)
    }

    def "test with Ignore"() {
        given:
        def metadata = [assert: [body: "IGNORE"]]
        def actualResponse = new ResponseEntity(testBytesActual, HttpStatus.OK)
        def expectedResponse = [status: 200, body: "@media/spring.png"]

        when:
        binaryAssertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = binaryAssertionSupporter.assertList
        binaryAssertionSupporter.executeAssert()

        then:
        assertList.size() == 1
        assertList.get(0).actual == 200
        assertList.get(0).expected == 200
        noExceptionThrown()
    }

}
