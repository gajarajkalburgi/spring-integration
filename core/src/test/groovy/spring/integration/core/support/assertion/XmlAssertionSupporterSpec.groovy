package spring.integration.core.support.assertion

import com.fasterxml.jackson.databind.ObjectMapper
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.XMLUnit
import org.opentest4j.AssertionFailedError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification
import spock.lang.Unroll
import spring.integration.core.enums.XmlCompareMode
import spring.integration.core.support.TestException

class XmlAssertionSupporterSpec extends Specification {

    private XmlAssertionSupporter assertionSupporter = new XmlAssertionSupporter(new ObjectMapper())

    def "test NormalSuccess"() {
        given:
        def metadata = [assert: [header: null]]
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>()
        headers.add("headerKey", "firstHeaderValue")
        headers.add("headerKey", "secondHeaderValue")
        def actualResponse = new ResponseEntity([active: true], headers, HttpStatus.CREATED)
        def expectedResponse = [status: 201, headers: [headerKey: "headerValue"], body: "bodyValue"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 3
        assertList.get(0).actual == 201
        assertList.get(0).expected == 201
        !assertList.get(0).isRegex
        assertList.get(1).actual == "firstHeaderValue,secondHeaderValue"
        assertList.get(1).expected == "headerValue"
        !assertList.get(1).isRegex
        assertList.get(2).actual == [active: true]
        assertList.get(2).expected == "bodyValue"
        !assertList.get(2).isRegex
    }

    def "test SuccessWithRegex"() {
        given:
        def metadata = [assert: [header: "REGEX"]]
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, headers: [location: ".*"], body: "body"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 3
        assertList.get(1).expected == ".*"
        assertList.get(1).isRegex
    }

    def "test SuccessWithoutExpectedHeader"() {
        given:
        def metadata = [assert: null]
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: "body"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 2
        assertList.get(1).actual == [active: true]
        assertList.get(1).expected == "body"
        !assertList.get(1).isRegex
    }

    def "test SuccessWithBodyIgnore"() {
        given:
        def assertion = [body: "IGNORE"]
        def metadata = [assert: assertion]
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: "body"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 1
        assertList.get(0).actual == 201
        assertList.get(0).expected == 201
        !assertList.get(0).isRegex
    }

    def "test SuccessWithBodySchema"() {
        given:
        def metadata = [assert: [body: "SCHEMA"]]
        def actualResponse = new ResponseEntity(
                new File("src/test/resources/xml_support_test/note1.xml").text,
                HttpStatus.OK)

        def expectedResponse = [
                status: 200,
                body  : new File("src/test/resources/xml_support_test/note.xsd").text,
        ]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 1
    }

    def "test FailWithBodySchema"() {
        given:
        def metadata = [assert: [body: "SCHEMA"]]
        def actualResponse = new ResponseEntity(
                new File("src/test/resources/xml_support_test/expected.xml").text,
                HttpStatus.OK)

        def expectedResponse = [
                status: 200,
                body  : new File("src/test/resources/xml_support_test/note.xsd").text
        ]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 2
        assertList.get(1).expected == "xml schemas should be valid"
    }

    @Unroll
    def "test #test"() {
        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)

        then:
        thrown(TestException)

        where:
        test                    | metadata                 || actualResponse                         || expectedResponse
        "StatusIsNullException" | [assert: [header: null]] || new ResponseEntity(HttpStatus.CREATED) || [status: null]
    }


    def "test differences size"() {
        def metadata = [assert: [body: "EQUALS"]]
        def actualResponse = new ResponseEntity(
                new File("src/test/resources/xml_support_test/response.xml").text,
                HttpStatus.OK)

        def expectedResponse = [
                status: 200,
                body  : new File("src/test/resources/xml_support_test/expected.xml").text
        ]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(assertList.get(1).actual, assertList.get(1).expected))

        then:
        assert myDiff.getAllDifferences().size() == 5

    }

    @Unroll
    def "test getXmlCompareMode"() {
        given:
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: "body"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList[1].xmlCompareMode == xmlCompareMode

        where:
        metadata                 || xmlCompareMode
        [assert: [order: true]]  || XmlCompareMode.IDENTICAL
        [assert: [order: false]] || XmlCompareMode.SIMILAR
        [assert: [order: null]]  || XmlCompareMode.IDENTICAL
        [assert: null]           || XmlCompareMode.IDENTICAL
    }


    @Unroll
    def "test executeAssert"() {
        given:
        def actualResponseBody = "<?xml version=\"1.0\"?>\n" +
                                    "<note xmlns=\"https://www.w3schools.com\">\n" +
                                    "\t<to>Tove</to>\n" +
                                    "\t<from>Jani</from>\n" +
                                    "\t<heading>Reminder</heading>\n" +
                                    "\t<body>Don't forget me this weekend!</body>\n" +
                                "</note>"
        def actualResponse = new ResponseEntity(actualResponseBody, HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: expectedBody]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        noExceptionThrown()

        where:
        metadata                 || xmlCompareMode           || expectedBody
        [assert: [order: true]]  || XmlCompareMode.IDENTICAL || "<?xml version=\"1.0\"?>\n" +
                                                                "<note xmlns=\"https://www.w3schools.com\">\n" +
                                                                "\t<to>Tove</to>\n" +
                                                                "\t<from>Jani</from>\n" +
                                                                "\t<heading>Reminder</heading>\n" +
                                                                "\t<body>Don't forget me this weekend!</body>\n" +
                                                                "</note>"
        [assert: [order: false]] || XmlCompareMode.SIMILAR   || "<?xml version=\"1.0\"?>\n" +
                                                                "<note xmlns=\"https://www.w3schools.com\">\n" +
                                                                "\t<body>Don't forget me this weekend!</body>\n" +
                                                                "\t<heading>Reminder</heading>\n" +
                                                                "\t<from>Jani</from>\n" +
                                                                "\t<to>Tove</to>\n" +
                                                                "</note>"
    }

    @Unroll
    def "test executeAssert with failure"() {
        given:
        def actualResponseBody = "<?xml version=\"1.0\"?>\n" +
                "<note xmlns=\"https://www.w3schools.com\">\n" +
                "\t<to>foo</to>\n" +
                "\t<from>bar</from>\n" +
                "\t<heading>goodbye</heading>\n" +
                "\t<body>Forget me please</body>\n" +
                "</note>"
        def actualResponse = new ResponseEntity(actualResponseBody, HttpStatus.OK)
        def expectedResponse = [status: 200, body: expectedBody]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        thrown(AssertionFailedError.class)

        where:
        metadata                 || xmlCompareMode           || expectedBody
        [assert: [order: true]]  || XmlCompareMode.IDENTICAL || "<?xml version=\"1.0\"?>\n" +
                "<note xmlns=\"https://www.w3schools.com\">\n" +
                "\t<to>Tove</to>\n" +
                "\t<from>Jani</from>\n" +
                "\t<heading>Reminder</heading>\n" +
                "\t<body>Don't forget me this weekend!</body>\n" +
                "</note>"
        [assert: [order: false]] || XmlCompareMode.SIMILAR   || "<?xml version=\"1.0\"?>\n" +
                "<note xmlns=\"https://www.w3schools.com\">\n" +
                "\t<body>Don't forget me this weekend!</body>\n" +
                "\t<heading>Reminder</heading>\n" +
                "\t<from>Jani</from>\n" +
                "\t<to>Tove</to>\n" +
                "</note>"
    }

    def "test executeAssert with header regex"() {
        given:
        def metadata = [assert: ["body"  : "IGNORE",
                                 "header": "REGEX"]]
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>()
        headers.add("location", "http://localhost:1234/coreTests/xyz")
        def actualResponse = new ResponseEntity(null, headers, HttpStatus.CREATED)
        def expectedResponse = [status : 201,
                                headers: ["location": "^http://localhost:.*/coreTests/.*"]]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        noExceptionThrown()

    }

    def "test executeAssert with null"() {
        given:
        def metadata = [assert: [body: "EQUALS"]]
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>()
        def actualResponse = new ResponseEntity(null, headers, HttpStatus.OK)
        def expectedResponse = [status: 200, body: null]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        noExceptionThrown()

    }

    def "test executeAssert with exception"() {
        given:
        def metadata = [assert: [order: false]]
        def actualResponse = new ResponseEntity("Foobar", HttpStatus.OK)
        def expectedResponse = [status: 200, body: "Foobar"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        thrown(TestException)

    }
}
