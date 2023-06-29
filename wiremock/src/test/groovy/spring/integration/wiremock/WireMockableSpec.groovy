package spring.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.github.tomakehurst.wiremock.verification.FindRequestsResult
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.springframework.test.util.TestSocketUtils
import spock.lang.Specification
import spring.integration.core.support.TestException

class WireMockableSpec extends Specification {


    def TARGET = new WireMockable(TestSocketUtils.findAvailableTcpPort(), new ObjectMapper())

    def MOCK_OF_MOCK_SERVER = Mock(WireMockServer)

    def MOCK_SERVER_DATA = [["request" : ["url"   : "/external/1",
                                          "method": "GET"],
                             "response": ["status"  : 200,
                                          "jsonBody": [
                                                  "flag": true,
                                                  "amt" : 1234,
                                                  "desc": "Something to describe"]]],
                            ["request" : ["url"   : "/external/",
                                          "method": "POST"],
                             "response":
                                     ["status": 201]]]

    def MOCK_SERVER_DATA_CONTAIN_UNUSED = [["request" : ["url"   : "/external/1",
                                                         "method": "GET"],
                                            "response": ["status"  : 200,
                                                         "jsonBody": [
                                                                 "flag": true,
                                                                 "amt" : 1234,
                                                                 "desc": "Something to describe"]]],
                                           ["request" : ["url"   : "/external/",
                                                         "method": "POST"],
                                            "response":
                                                    ["status": 201]],
                                           // The following endpoint never be called.
                                           ["request" : ["url"   : "/external/",
                                                         "method": "DELETE"],
                                            "response":
                                                    ["status": 204]]]

    def MOCK_SERVER_DATA_WITH_SWAGGER = [["swagger" : "http://foobar.com/foobar-api.yml",
                                          "request" : ["url"   : "/external/1",
                                                       "method": "GET"],
                                          "response": ["status"  : 200,
                                                       "jsonBody": [
                                                               "flag": true,
                                                               "amt" : 1234,
                                                               "desc": "Something to describe"]]],
                                         ["swagger" : "http://foobar.com/foobar-api.yml",
                                          "request" : ["url"   : "/external/",
                                                       "method": "POST"],
                                          "response": ["status": 201, "body": "OK"]]]

    def CALLED_SERVER_EVENTS = [


            ServeEvent.of(new LoggedRequest("/external/1", null, new RequestMethod("GET"), null, null, null, false, null, null, null, null, null),
                    ResponseDefinition.ok()),


            ServeEvent.of(new LoggedRequest("/external/", null, new RequestMethod("POST"), null, null, null, false, null, null, null, null, null),
                    ResponseDefinition.created())]


    def "Test for setup"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.setup(MOCK_SERVER_DATA)

        then:
        MOCK_OF_MOCK_SERVER.isRunning() >> false
        2 * MOCK_OF_MOCK_SERVER.addStubMapping(_ as StubMapping)
        1 * MOCK_OF_MOCK_SERVER.start()
    }

    def "Test for setup with Exception"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.setup([[null: "not json string"]])

        then:
        thrown(TestException)
        0 * MOCK_OF_MOCK_SERVER.addStubMapping(_ as StubMapping)
        0 * MOCK_OF_MOCK_SERVER.isRunning()
    }

    def "Test for execution removeStub correctly"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.defaultCleanUp(MOCK_SERVER_DATA)

        then:
        MOCK_OF_MOCK_SERVER.getStubMappings() >> [new StubMapping(), new StubMapping()]
        2 * MOCK_OF_MOCK_SERVER.removeStub(_ as StubMapping)
    }

    def "Test for executePostAssertion"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.verifyUtilizedMock(MOCK_SERVER_DATA)

        then:
        MOCK_OF_MOCK_SERVER.findUnmatchedRequests() >> new FindRequestsResult([], false)
        MOCK_OF_MOCK_SERVER.getAllServeEvents() >> CALLED_SERVER_EVENTS
        notThrown(TestException)
    }

    def "Test for not mocked request"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.verifyUtilizedMock(MOCK_SERVER_DATA_CONTAIN_UNUSED)

        then:
        MOCK_OF_MOCK_SERVER.findUnmatchedRequests() >> new FindRequestsResult([new LoggedRequest("testendoint", null, new RequestMethod("GET"), null, null, null, false, null, null, null, null, null)], false)

        def ex = thrown(TestException)
        ex.getMessage() == "Detect request to non-present mocked endpoint : testendoint. please check your test specification file."
    }

    def "Test for setup with swagger"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.setup(MOCK_SERVER_DATA_WITH_SWAGGER)
        then:
        MOCK_OF_MOCK_SERVER.isRunning() >> false
        2 * MOCK_OF_MOCK_SERVER.addStubMapping(_ as StubMapping)
        1 * MOCK_OF_MOCK_SERVER.start()
    }

    def "Test for setup with "() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.setup(MOCK_SERVER_DATA_WITH_SWAGGER)
        MOCK_SERVER_DATA_WITH_SWAGGER.head().get("swagger")
        then:
        MOCK_OF_MOCK_SERVER.isRunning() >> false
        2 * MOCK_OF_MOCK_SERVER.addStubMapping(_ as StubMapping)
        1 * MOCK_OF_MOCK_SERVER.start()
    }

    def "Test for initWithMappingFiles"() {
        when:
        TARGET.mockServer = MOCK_OF_MOCK_SERVER
        TARGET.initWithMappingFiles("foo")

        then:
        1 * MOCK_OF_MOCK_SERVER.loadMappingsUsing(_ as JsonFileMappingsSource)
    }

}
