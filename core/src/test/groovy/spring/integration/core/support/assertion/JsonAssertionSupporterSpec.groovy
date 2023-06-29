package spring.integration.core.support.assertion

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ListProcessingReport
import org.opentest4j.AssertionFailedError
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification
import spock.lang.Unroll
import spring.integration.core.support.TestException

class JsonAssertionSupporterSpec extends Specification {

    private JsonAssertionSupporter assertionSupporter = new JsonAssertionSupporter(new ObjectMapper())

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
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: [properties: [active: [type: "boolean"]]]]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 1
    }

    def "test FailWithBodySchema"() {
        given:
        def metadata = [assert: [body: "SCHEMA"]]
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: [properties: [active: [type: "string"]]]]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.size() == 2
        assertList.get(1).actual.getClass() == ListProcessingReport.class
        assertList.get(1).expected == null
        !assertList.get(1).isRegex
    }

    @Unroll
    def "test #test"() {
        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)

        then:
        thrown(TestException)

        where:
        test                    | metadata                   || actualResponse                                       || expectedResponse
        "StatusIsNullException" | [assert: [header: null]]   || new ResponseEntity(HttpStatus.CREATED)               || [status: null]
        "ProcessingException"   | [assert: [body: "SCHEMA"]] || new ResponseEntity("actualBody", HttpStatus.CREATED) || [status: 201, body: "expectedBody"]
    }

    def "test with ignored fields"() {
        given:
        def metadata = [
                assert: [
                        ignore: [
                                '$..id',
                                '$.._links',
                                '$.children[1]'
                        ]
                ]
        ]
        def actualResponse = new ResponseEntity(
                [
                        id      : "c1be6d4a-7820-11e7-b5a5-be2e44b06b34",
                        name    : "Ned Stark",
                        children: [
                                [
                                        id    : "2222667b-4fbd-44bb-bf96-d4a6f3a2b3dd",
                                        name  : "John Snow",
                                        _links: [
                                                self:
                                                        [
                                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ/children/2222667b-4fbd-44bb-bf96-d4a6f3a2b3dd"
                                                        ]
                                        ]
                                ],
                                [
                                        id    : "a8bfa587-4465-4c9d-991f-f3e2d04291a3",
                                        name  : "Arya Stark",
                                        _links: [
                                                self:
                                                        [
                                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ/children/a8bfa587-4465-4c9d-991f-f3e2d04291a3"
                                                        ]
                                        ]
                                ]
                        ],
                        _links  : [
                                self:
                                        [
                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ"
                                        ]
                        ]
                ],
                HttpStatus.OK)

        def expectedResponse = [
                status: 200,
                body  : [
                        name    : "Ned Stark",
                        children: [
                                [
                                        name: "John Snow"
                                ]
                        ]
                ]
        ]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.get(1).actual == assertList.get(1).expected

    }

    @Unroll
    def "test with invalid ignored parameter"() {
        given:
        def metadata = [
                assert: [
                        ignore: [
                                invalidJsonPath
                        ]
                ]
        ]
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: [properties: [active: [type: "boolean"]]]]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        thrown(TestException)

        where:
        invalidJsonPath << ['$..id.', '$..id..']
    }

    def "test with empty ignored parameter"() {
        given:
        def metadata = [
                assert: [
                        ignore: []
                ]
        ]
        def actualResponse = new ResponseEntity(
                [
                        id      : "c1be6d4a-7820-11e7-b5a5-be2e44b06b34",
                        name    : "Ned Stark",
                        children: [
                                [
                                        id    : "2222667b-4fbd-44bb-bf96-d4a6f3a2b3dd",
                                        name  : "John Snow",
                                        _links: [
                                                self:
                                                        [
                                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ/children/2222667b-4fbd-44bb-bf96-d4a6f3a2b3dd"
                                                        ]
                                        ]
                                ],
                                [
                                        id    : "a8bfa587-4465-4c9d-991f-f3e2d04291a3",
                                        name  : "Arya Stark",
                                        _links: [
                                                self:
                                                        [
                                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ/children/a8bfa587-4465-4c9d-991f-f3e2d04291a3"
                                                        ]
                                        ]
                                ]
                        ],
                        _links  : [
                                self:
                                        [
                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ"
                                        ]
                        ]
                ],
                HttpStatus.OK)

        def expectedResponse = [
                status: 200,
                body  : [
                        id      : "c1be6d4a-7820-11e7-b5a5-be2e44b06b34",
                        name    : "Ned Stark",
                        children: [
                                [
                                        id    : "2222667b-4fbd-44bb-bf96-d4a6f3a2b3dd",
                                        name  : "John Snow",
                                        _links: [
                                                self:
                                                        [
                                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ/children/2222667b-4fbd-44bb-bf96-d4a6f3a2b3dd"
                                                        ]
                                        ]
                                ],
                                [
                                        id    : "a8bfa587-4465-4c9d-991f-f3e2d04291a3",
                                        name  : "Arya Stark",
                                        _links: [
                                                self:
                                                        [
                                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ/children/a8bfa587-4465-4c9d-991f-f3e2d04291a3"
                                                        ]
                                        ]
                                ]
                        ],
                        _links  : [
                                self:
                                        [
                                                href: "http://localhost:8080/sample/lmf-cgbqRSyGXRcUdrrpxQ"
                                        ]
                        ]
                ]
        ]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList.get(1).actual == assertList.get(1).expected

    }

    @Unroll
    def "test getJsonCompareMode"() {
        given:
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: "body"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        def assertList = assertionSupporter.assertList

        then:
        assertList[1].getJsonCompareMode() == jsonCompareMode

        where:
        metadata                 || jsonCompareMode
        [assert: [order: true]]  || JSONCompareMode.STRICT
        [assert: [order: false]] || JSONCompareMode.NON_EXTENSIBLE
        [assert: [order: null]]  || JSONCompareMode.STRICT
        [assert: null]           || JSONCompareMode.STRICT
    }

    @Unroll
    def "test getJsonCompareMode with invalid parameter"() {
        given:
        def actualResponse = new ResponseEntity([active: true], HttpStatus.CREATED)
        def expectedResponse = [status: 201, body: "body"]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)

        then:
        thrown(Exception)

        where:
        metadata << [[assert: [order: "true"]], [assert: [order: 1]]]

    }

    def "test executeAssert with header regex"() {
        given:
        def metadata = [assert: ["body": "IGNORE", "header": "REGEX"]]
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

    def "test executeAssert with ignoring order of map"() {
        given:
        def metadata = [assert: [order: false]]
        def actualResponse = new ResponseEntity(["foo": "bar", "hoge": "piyo"], HttpStatus.OK)
        def expectedResponse = [status: 200, body: ["hoge": "piyo", "foo": "bar"]]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        noExceptionThrown()

    }

    def "test executeAssert with ignoring order of array"() {
        given:
        def metadata = [assert: [order: false]]
        def actualResponse = new ResponseEntity([8, 4, 1, 3], HttpStatus.OK)
        def expectedResponse = [status: 200, body: [1, 3, 4, 8]]

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

    def "test executeAssert with unmatched data type"() {
        given:
        def metadata = [:]
        def actualResponse = new ResponseEntity(["name": "foo", "age": "0"], HttpStatus.OK)
        def expectedResponse = [status: 200, body: ["name": "foo", "age": 0]]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        thrown(AssertionFailedError)

    }

    def "test executeAssert with Object"() {
        given:
        def metadata = [:]
        def actualResponse = new ResponseEntity(["name": "foo", "age": "0"], HttpStatus.OK)
        def expectedResponse = [status: 200, body: new Object()]

        when:
        assertionSupporter.setUpAssertList(metadata, actualResponse, expectedResponse)
        assertionSupporter.executeAssert()

        then:
        thrown(TestException)

    }

}
