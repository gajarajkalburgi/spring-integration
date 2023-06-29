package spring.integration.core.support.assertion

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.RandomUtils
import org.opentest4j.AssertionFailedError
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spring.integration.core.support.IntegrationRestTemplate
import spring.integration.core.support.ResponseHistoryService
import spring.integration.core.support.TestException

import java.util.stream.Collectors

class AssertionSupporterFactorySpec extends Specification {

    def port = 1234

    def serverContextPath = "/integration"

    def objectMapper = new ObjectMapper()

    def integrationRestTemplateMock = Mock(IntegrationRestTemplate)

    def responseHistoryService = new ResponseHistoryService(objectMapper)

    def specificationData = ["metadata": ["assert": ["body": "EQUALS"]],
                             "setup"   : [],
                             "request" : ["url"    : "/bookStore",
                                          "method" : "GET",
                                          "headers": ["Content-Type": "application/json; charset=utf-8"]],
                             "response": ["status" : 200,
                                          "headers": ["Content-Type": "application/json; charset=utf-8"],
                                          "body"   : ["category": "マンガ",
                                                      "id"      : 125780,
                                                      "author"  : "手塚治虫",
                                                      "title"   : "ブラック・ジャック",
                                                      "year"    : "1973",
                                                      "price"   : 420]],
                             "cleanup" : []]

    def specificationDataWithQueryString = ["metadata": ["assert": ["body": "EQUALS"]],
                                            "setup"   : [],
                                            "request" : ["url"    : "/bookStore",
                                                         "method" : "GET",
                                                         "query"  : [
                                                                 "category": "マンガ",
                                                                 "year"    : 1973
                                                         ],
                                                         "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                            "response": ["status" : 200,
                                                         "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                         "body"   : ["category": "マンガ",
                                                                     "id"      : 125780,
                                                                     "author"  : "手塚治虫",
                                                                     "title"   : "ブラック・ジャック",
                                                                     "year"    : "1973",
                                                                     "price"   : 420]],
                                            "cleanup" : []]

    def specificationDataWithPath = ["metadata": ["assert": ["body": "EQUALS"]],
                                     "setup"   : [],
                                     "request" : ["url"    : "/bookStore",
                                                  "method" : "GET",
                                                  "path"   : [
                                                          "foo",
                                                          "bar"
                                                  ],
                                                  "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                     "response": ["status" : 200,
                                                  "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                  "body"   : ["category": "マンガ",
                                                              "id"      : 125780,
                                                              "author"  : "手塚治虫",
                                                              "title"   : "ブラック・ジャック",
                                                              "year"    : "1973",
                                                              "price"   : 420]],
                                     "cleanup" : []]

    def specificationDataWithPathAndQuery = ["metadata": ["assert": ["body": "EQUALS"]],
                                             "setup"   : [],
                                             "request" : ["url"    : "/bookStore",
                                                          "method" : "GET",
                                                          "path"   : [
                                                                  "foo",
                                                                  "bar"
                                                          ],
                                                          "query"  : [
                                                                  "category": "マンガ",
                                                                  "year"    : 1973
                                                          ],
                                                          "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                             "response": ["status" : 200,
                                                          "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                          "body"   : ["category": "マンガ",
                                                                      "id"      : 125780,
                                                                      "author"  : "手塚治虫",
                                                                      "title"   : "ブラック・ジャック",
                                                                      "year"    : "1973",
                                                                      "price"   : 420]],
                                             "cleanup" : []]

    def specificationDataWithInvalidQueryString = ["metadata": ["assert": ["body": "EQUALS"]],
                                                   "setup"   : [],
                                                   "request" : ["url"    : "/bookStore",
                                                                "method" : "GET",
                                                                "query"  : [
                                                                        "category",
                                                                        "year"
                                                                ],
                                                                "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                   "response": ["status" : 200,
                                                                "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                                "body"   : ["category": "マンガ",
                                                                            "id"      : 125780,
                                                                            "author"  : "手塚治虫",
                                                                            "title"   : "ブラック・ジャック",
                                                                            "year"    : "1973",
                                                                            "price"   : 420]],
                                                   "cleanup" : []]

    def specificationDataNoContentType = ["metadata": ["assert": ["body": "EQUALS"]],
                                          "setup"   : [],
                                          "request" : ["url"   : "/bookStore",
                                                       "method": "GET"],
                                          "response": ["status": 200,
                                                       "body"  : ["category": "マンガ",
                                                                  "id"      : 125780,
                                                                  "author"  : "手塚治虫",
                                                                  "title"   : "ブラック・ジャック",
                                                                  "year"    : "1973",
                                                                  "price"   : 420]],
                                          "cleanup" : []]

    def specificationDataWithUnsupportedContentType = ["metadata": ["assert": ["body": "EQUALS"]],
                                                       "setup"   : [],
                                                       "request" : ["url"   : "/bookStorejs",
                                                                    "method": "GET"],
                                                       "response": ["status" : 200,
                                                                    "headers": ["Content-Type": "application/js"],
                                                                    "body"   : "console.log('foobar')"],
                                                       "cleanup" : []]

    def specificationDataMultiPart = ["metadata": ["assert": ["body": "EQUALS"]],
                                      "setup"   : [],
                                      "request" : ["url"    : "/book/",
                                                   "method" : "POST",
                                                   "headers": ["Content-Type": "multipart/form-data"],
                                                   "body"   : ["media"   : "@xml_support_test/note.xsd",
                                                               "text"    : "This is test xsd.",
                                                               "metadata": [
                                                                       "type": "xsd"
                                                               ]]
                                      ],
                                      "response": ["status" : 201,
                                                   "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                      "cleanup" : []]

    def specificationDataMultiPartWithNotExistPath = ["metadata": ["assert": ["body": "EQUALS"]],
                                                      "setup"   : [],
                                                      "request" : ["url"    : "/book/",
                                                                   "method" : "POST",
                                                                   "headers": ["Content-Type": "multipart/form-data"],
                                                                   "body"   : ["media"   : "@foo/bar.png",
                                                                               "text"    : "This is bar image.",
                                                                               "metadata": [
                                                                                       "type": "png"
                                                                               ]]
                                                      ],
                                                      "response": ["status" : 201,
                                                                   "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                      "cleanup" : []]

    def specificationDataRequestBodyAsList = ["metadata": ["assert": ["body": "EQUALS"]],
                                              "setup"   : [],
                                              "request" : ["url"    : "/book/",
                                                           "method" : "POST",
                                                           "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                           "body"   : [["category"   : "cartoon",
                                                                        "id"         : 124,
                                                                        "author"     : "手塚治虫",
                                                                        "title"      : "Kimba the White Lion",
                                                                        "year"       : "1950",
                                                                        "price"      : 420,
                                                                        "reservation": true],
                                                                       ["category"   : "cartoon",
                                                                        "id"         : 124,
                                                                        "author"     : "手塚治虫",
                                                                        "title"      : "Black Jack",
                                                                        "year"       : "1973",
                                                                        "price"      : 420,
                                                                        "reservation": true]]],
                                              "response": ["status" : 201,
                                                           "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                              "cleanup" : []]

    def specificationDataMultiPartRequestBodyAsList = ["metadata": ["assert": ["body": "EQUALS"]],
                                                       "setup"   : [],
                                                       "request" : ["url"    : "/book/",
                                                                    "method" : "POST",
                                                                    "headers": ["Content-Type": "multipart/form-data"],
                                                                    "body"   : [
                                                                            [
                                                                                    "media"   : "@xml_support_test/note.xsd",
                                                                                    "text"    : "This is test xsd.",
                                                                                    "metadata": [
                                                                                            "type": "xsd"
                                                                                    ]
                                                                            ],
                                                                            [
                                                                                    "media"   : "@xml_support_test/response.xml",
                                                                                    "text"    : "This is test response.",
                                                                                    "metadata": [
                                                                                            "type": "response"
                                                                                    ]
                                                                            ],
                                                                            [
                                                                                    "media"   : "@xml_support_test/response.xml",
                                                                                    "text"    : "This is string.",
                                                                                    "metadata": [
                                                                                            "type": "string"
                                                                                    ]
                                                                            ]

                                                                    ]
                                                       ],
                                                       "response": ["status" : 201,
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                       "cleanup" : []]

    def specificationDataWithLocalSwagger = ["metadata": ["assert": ["body": "EQUALS", "swagger": "./swagger_validator_test/test_swagger.yaml"]],
                                             "setup"   : [],
                                             "request" : ["url"    : "/book/",
                                                          "method" : "POST",
                                                          "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                          "body"   : ["category"   : "cartoon",
                                                                      "id"         : 124,
                                                                      "author"     : "手塚治虫",
                                                                      "title"      : "Kimba the White Lion",
                                                                      "year"       : "1950",
                                                                      "price"      : 420,
                                                                      "reservation": true]],
                                             "response": ["status" : 201,
                                                          "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                             "cleanup" : []]

    def specificationDataWithRemoteSwagger = ["metadata": ["assert": ["body": "EQUALS", "swagger": "/v2/api-docs?group=t2-test"]],
                                              "setup"   : [],
                                              "request" : ["url"   : "/findBookById?id=12578",
                                                           "method": "GET"],
                                              "response": ["status": 200,
                                                           "body"  : ["category"   : "cartoon",
                                                                      "id"         : 124,
                                                                      "author"     : "手塚治虫",
                                                                      "title"      : "Black Jack",
                                                                      "year"       : "1970",
                                                                      "price"      : 420,
                                                                      "reservation": false]],
                                              "cleanup" : []]

    def specificationDataForXml = ["metadata": ["assert": ["body": "EQUALS"]],
                                   "request" : [
                                           "url"   : "/bookStore",
                                           "method": "GET"
                                   ],
                                   "response": [
                                           "status" : 200,
                                           "headers": [
                                                   "Content-Type": "application/xml"
                                           ],
                                           "body"   : "<?xml version='1.0' encoding='utf-8'?>\n" +
                                                   "<bookstore>\n" +
                                                   "\t<book category=\"WEB\" reservation=\"true\">\n" +
                                                   "\t\t<id>99009</id>\n" +
                                                   "\t\t<author>Erik T. Ray</author>\n" +
                                                   "\t\t<title>Learning XML</title>\n" +
                                                   "\t\t<year>2003</year>\n" +
                                                   "\t\t<price>39.95</price>\n" +
                                                   "\t</book>\n" +
                                                   "</bookstore>"]]

    @Shared
    def specificationDataForXhtml = ["metadata": ["assert": ["body": "EQUALS"]],
                                     "setup"   : [],
                                     "request" : ["url"   : "/xhtml",
                                                  "method": "GET"],
                                     "response": ["status" : 200,
                                                  "headers": ["Content-Type": "application/xhtml+xml; charset=utf-8"],
                                                  "body"   : "<html><body><div>HTML</div></body></html>"]
                                     ,
                                     "cleanup" : []]

    @Shared
    def specificationDataForText = ["metadata": ["assert": ["body": "EQUALS"]],
                                    "setup"   : [],
                                    "request" : ["url"   : "/text",
                                                 "method": "GET"],
                                    "response": ["status" : 200,
                                                 "headers": ["Content-Type": "text/plain"],
                                                 "body"   : "TEXTTEXTTEXT"]
                                    ,
                                    "cleanup" : []]


    @Shared
    def specificationDataForOctetStream = ["metadata": ["assert": ["body": "EQUALS"]],
                                           "setup"   : [],
                                           "request" : ["url"   : "/octet",
                                                        "method": "GET"],
                                           "response": ["status" : 200,
                                                        "headers": ["Content-Type": "application/octet-stream"],
                                                        "body"   : RandomUtils.nextBytes(32)],
                                           "cleanup" : []]
    @Shared
    def specificationDataForPdf = ["metadata": ["assert": ["body": "EQUALS"]],
                                   "setup"   : [],
                                   "request" : ["url"   : "/octet",
                                                "method": "GET"],
                                   "response": ["status" : 200,
                                                "headers": ["Content-Type": "application/pdf"],
                                                "body"   : RandomUtils.nextBytes(32)],
                                   "cleanup" : []]
    @Shared
    def specificationDataForImage = ["metadata": ["assert": ["body": "EQUALS"]],
                                     "setup"   : [],
                                     "request" : ["url"   : "/octet",
                                                  "method": "GET"],
                                     "response": ["status" : 200,
                                                  "headers": ["Content-Type": "image/png"],
                                                  "body"   : RandomUtils.nextBytes(32)],
                                     "cleanup" : []]
    @Shared
    def specificationDataForVideo = ["metadata": ["assert": ["body": "EQUALS"]],
                                     "setup"   : [],
                                     "request" : ["url"   : "/octet",
                                                  "method": "GET"],
                                     "response": ["status" : 200,
                                                  "headers": ["Content-Type": "video/mp4"],
                                                  "body"   : RandomUtils.nextBytes(32)],
                                     "cleanup" : []]
    @Shared
    def specificationDataForAudio = ["metadata": ["assert": ["body": "EQUALS"]],
                                     "setup"   : [],
                                     "request" : ["url"   : "/octet",
                                                  "method": "GET"],
                                     "response": ["status" : 200,
                                                  "headers": ["Content-Type": "audio/aac"],
                                                  "body"   : RandomUtils.nextBytes(32)],
                                     "cleanup" : []]
    @Shared
    def specificationDataForZip = ["metadata": ["assert": ["body": "EQUALS"]],
                                   "setup"   : [],
                                   "request" : ["url"   : "/octet",
                                                "method": "GET"],
                                   "response": ["status" : 200,
                                                "headers": ["Content-Type": "application/zip"],
                                                "body"   : RandomUtils.nextBytes(32)],
                                   "cleanup" : []]

    def specificationDataWithScenario = ["metadata": ["assert": ["body": "EQUALS"]],
                                         "setup"   : [],
                                         "scenario": [["request" : ["url"    : "/bookStore",
                                                                    "method" : "GET",
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                       "response": ["status" : 200,
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                                    "body"   : ["category": "マンガ",
                                                                                "id"      : 125780,
                                                                                "author"  : "手塚治虫",
                                                                                "title"   : "ブラック・ジャック",
                                                                                "year"    : "1973",
                                                                                "price"   : 420]]],
                                                      ["request" : ["url"    : "/bookStore",
                                                                    "method" : "GET",
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                       "response": ["status" : 200,
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                                    "body"   : ["category": "マンガ",
                                                                                "id"      : 125780,
                                                                                "author"  : "手塚治虫",
                                                                                "title"   : "ブラック・ジャック",
                                                                                "year"    : "1973",
                                                                                "price"   : 420]]],
                                                      ["request" : ["url"    : "/bookStore",
                                                                    "method" : "GET",
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                       "response": ["status" : 200,
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                                    "body"   : ["category": "マンガ",
                                                                                "id"      : 125780,
                                                                                "author"  : "手塚治虫",
                                                                                "title"   : "ブラック・ジャック",
                                                                                "year"    : "1973",
                                                                                "price"   : 420]]],
                                                      ["request" : ["url"    : "/bookStore",
                                                                    "method" : "GET",
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"]],
                                                       "response": ["status" : 200,
                                                                    "headers": ["Content-Type": "application/json; charset=utf-8"],
                                                                    "body"   : ["category": "マンガ",
                                                                                "id"      : 125780,
                                                                                "author"  : "手塚治虫",
                                                                                "title"   : "ブラック・ジャック",
                                                                                "year"    : "1973",
                                                                                "price"   : 420]]]],
                                         "cleanup" : []]
    @Shared
    def specificationDataWithXmlRequestBody = ["metadata": ["assert": ["body": "EQUALS"]],
                                               "setup"   : [],
                                               "request" : ["url"   : "/create",
                                                            "method": "POST",
                                                            "body"  : "<foo><bar/></foo>"],
                                               "response": ["status" : 201,
                                                            "headers": ["Content-Type": "application/xml;charset=UTF-8"],
                                                            "body"   : "foo><bar/></foo>"],
                                               "cleanup" : []]

    def target = new AssertionSupporterFactory(port, serverContextPath, objectMapper, integrationRestTemplateMock, responseHistoryService)

    def "Test for create without swagger"() {
        when:
        def result = target.create(specificationData)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationData.response.body)
        result.findFirst().get() instanceof JsonAssertionSupporter
    }

    def "Test for create with query"() {
        when:
        def result = target.create(specificationDataWithQueryString)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithQueryString.response.body)
        result.findFirst().get() instanceof JsonAssertionSupporter
    }

    def "Test for create with path"() {
        when:
        def result = target.create(specificationDataWithPath)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithPath.response.body)
        result.findFirst().get() instanceof JsonAssertionSupporter
    }

    def "Test for create with path and query"() {
        when:
        def result = target.create(specificationDataWithPathAndQuery)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithPath.response.body)
        result.findFirst().get() instanceof JsonAssertionSupporter
    }

    def "Test for create with local swagger specification"() {
        when:
        def result = target.create(specificationDataWithLocalSwagger)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> new ResponseEntity(HttpStatus.CREATED)
        result.findFirst().get() instanceof JsonAssertionSupporter
    }

    def "Test for exception with remote swagger specification"() {
        when:
        target.create(specificationDataWithRemoteSwagger)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithRemoteSwagger.response.body)
    }

    def "Test for create XmlAssertionSupporter"() {
        when:
        def result = target.create(specificationDataForXml)

        then:

        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, String.class) >> ResponseEntity.ok(specificationDataForXml.response.body)
        result.findFirst().get() instanceof XmlAssertionSupporter
    }


    @Unroll
    def "Test create text assertion supporter"() {
        when:
        def result = target.create(inputSpecificationData)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, String.class) >> ResponseEntity.ok(inputSpecificationData.response.body)
        result.findFirst().get() instanceof TextAssertionSupporter

        where:
        test | inputSpecificationData
        1    | specificationDataForXhtml
        2    | specificationDataForText
    }

    def "Test for create binary assertion supporter"() {
        when:
        def result = target.create(inputSpecificationData)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, byte[].class) >> ResponseEntity.ok(inputSpecificationData.response.body)
        result.findFirst().get() instanceof BinaryAssertionSupporter

        where:
        test | inputSpecificationData
        1    | specificationDataForOctetStream
        2    | specificationDataForPdf
        3    | specificationDataForImage
        4    | specificationDataForVideo
        5    | specificationDataForAudio
        6    | specificationDataForZip
    }

    def "Test for create assertion supporter without Content-Type"() {
        when:
        def result = target.create(specificationDataNoContentType)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataNoContentType.response.body)
        result.findFirst().get() instanceof JsonAssertionSupporter
    }

    def "Test for unsupported Content-Type"() {
        when:
        target.create(specificationDataWithUnsupportedContentType)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithUnsupportedContentType.response.body)
        def ex = thrown(TestException)
        ex.getMessage() == "application/js : is not supported Content-Type."
    }

    def "Test with request body which has list"() {
        when:
        def result = target.create(specificationDataRequestBodyAsList)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> new ResponseEntity(HttpStatus.CREATED)
        result.findFirst().get() instanceof JsonAssertionSupporter
        notThrown(Throwable)

    }

    def "Test with multipart request body"() {
        when:
        def result = target.create(specificationDataMultiPart)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> new ResponseEntity(HttpStatus.CREATED)
        result.findFirst().get() instanceof JsonAssertionSupporter
        notThrown(Throwable)
    }

    def "Test with multipart request body which has list"() {
        when:
        def result = target.create(specificationDataMultiPartRequestBodyAsList)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> new ResponseEntity(HttpStatus.CREATED)
        result.findFirst().get() instanceof JsonAssertionSupporter
        notThrown(Throwable)
    }

    def "Test with multipart request body which has not exist file path"() {
        when:
        target.create(specificationDataMultiPartWithNotExistPath)

        then:
        def ex = thrown(AssertionFailedError)
        ex.getMessage() == "foo/bar.png does not exist."
    }

    def "Test for create with invalid query string"() {
        when:
        target.create(specificationDataWithInvalidQueryString)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithInvalidQueryString.response.body)
        thrown(TestException)
    }

    def "Test for create multiple assertion supporter"() {
        when:
        def result = target.create(specificationDataWithScenario).collect(Collectors.toList())

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, Object.class) >> ResponseEntity.ok(specificationDataWithScenario.scenario.head)
        result.size() == specificationDataWithScenario.scenario.size()
    }

    def "Test for create with XML request body"() {
        when:
        def result = target.create(specificationDataWithXmlRequestBody)

        then:
        integrationRestTemplateMock.exchange(objectMapper, _ as RequestEntity, String.class) >> ResponseEntity.ok(specificationDataWithXmlRequestBody.response.body)
        result.findFirst().get() instanceof XmlAssertionSupporter
    }

}
