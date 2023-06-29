package spring.integration.core.support

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import spock.lang.Specification

class ResponseHistoryServiceSpec extends Specification {

    def target = new ResponseHistoryService(new ObjectMapper())

    def "Add response successfully"() {
        when:
        target.addToResponseHistory(new ResponseEntity(HttpStatus.OK))
                .addToResponseHistory(new ResponseEntity(HttpStatus.OK))

        then:
        target.responseHistory.size() == 2
    }

    def "Clear response successfully"() {
        when:
        target.addToResponseHistory(new ResponseEntity(HttpStatus.OK)).clear()

        then:
        target.responseHistory.size() == 0
    }

    def "Execute executeExchangeWithHistory successfully without history"() {
        given:
        def requestMap = [
                "headers": ["Content-Type": "application/json"],
                "body"   : ["firstName": "hoge", "lastName": "fuga"]
        ]

        when:
        def result = target.executeExchangeWithHistory(requestMap)

        then:
        result == [
                "headers": ["Content-Type": "application/json"],
                "body"   : ["firstName": "hoge", "lastName": "fuga"]
        ]
    }

    def "Execute executeExchangeWithHistory successfully"() {
        given:
        def responseHeader = new LinkedMultiValueMap()
        responseHeader.set("Content-Type", "application/json")
        def responseBody = ["firstName": "foo", "lastName": "bar"]
        def responseEntity = new ResponseEntity(responseBody, responseHeader, HttpStatus.OK)
        def requestMap = [
                "headers": ["Content-Type": "%{\$[0].headers.Content-Type[0]}"],
                "body"   : ["firstName": "%{\$[0].body.firstName}", "lastName": "%{\$[0].body.lastName}"]
        ]

        when:
        def result = target.addToResponseHistory(responseEntity)
                .executeExchangeWithHistory(requestMap)

        then:
        result == [
                "headers": ["Content-Type": "application/json"],
                "body"   : ["firstName": "foo", "lastName": "bar"]
        ]
    }

    def "Execute executeExchangeWithHistory with query string successfully"() {
        given:
        def responseHeader = new LinkedMultiValueMap()
        responseHeader.set("Content-Type", "application/json")
        def responseBody = ["firstName": "foo", "lastName": "bar"]
        def responseEntity = new ResponseEntity(responseBody, responseHeader, HttpStatus.OK)
        def requestMap = [
                "headers": ["Content-Type": "%{\$[0].headers.Content-Type[0]}"],
                "query"  : ["firstName": "%{\$[0].body.firstName}", "lastName": "%{\$[0].body.lastName}"]
        ]

        when:
        def result = target.addToResponseHistory(responseEntity)
                .executeExchangeWithHistory(requestMap)

        then:
        result == [
                "headers": ["Content-Type": "application/json"],
                "query"  : ["firstName": "foo", "lastName": "bar"]
        ]
    }

    def "Execute executeExchangeWithHistory with path variable successfully"() {
        given:
        def responseHeader = new LinkedMultiValueMap()
        responseHeader.set("Content-Type", "application/json")
        def responseBody = ["firstName": "foo", "lastName": "bar"]
        def responseEntity = new ResponseEntity(responseBody, responseHeader, HttpStatus.OK)
        def requestMap = [
                "headers": ["Content-Type": "%{\$[0].headers.Content-Type[0]}"],
                "path"   : ["%{\$[0].body.firstName}", "%{\$[0].body.lastName}"]
        ]

        when:
        def result = target.addToResponseHistory(responseEntity)
                .executeExchangeWithHistory(requestMap)

        then:
        result == [
                "headers": ["Content-Type": "application/json"],
                "path"   : ["foo", "bar"]
        ]
    }


    def "Execute executeExchangeWithHistory with nested request and response successfully"() {
        given:
        def responseHeader = new LinkedMultiValueMap()
        responseHeader.set("Content-Type", "application/json")
        def responseBody = ["firstName": "foo", "lastName": "bar",
                            "address"  : ["post": "100-0001", "country": "Japan"], "active": [["year": 2016], ["year": 2017], ["year": 2018]]]
        def responseEntity = new ResponseEntity(responseBody, responseHeader, HttpStatus.OK)

        def requestMap = [
                "headers": ["Content-Type": "%{\$[0].headers.Content-Type[0]}"],
                "body"   : ["firstName": "%{\$[0].body.firstName}", "lastName": "%{\$[0].body.lastName}",
                            "address"  : ["post": "\$%{[0].body.address.post}", "country": "%{\$[0].body.address.country}"],
                            "active"   : ["%{\$[0].body.active[0]}", "%{\$[0].body.active[1]}", "%{\$[0].body.active[2]}"]]
        ]

        when:
        def result = target.addToResponseHistory(responseEntity)
                .executeExchangeWithHistory(requestMap)

        then:
        result == [
                "headers": ["Content-Type": "application/json"],
                "body"   : [
                        "firstName": "foo", "lastName": "bar",
                        "address"  : ["post": "100-0001", "country": "Japan"],
                        "active"   : [["year": 2016], ["year": 2017], ["year": 2018]]
                ]
        ]
    }

    def "Execute executeExchangeWithHistory with nested list successfully"() {
        given:
        def responseHeader = new LinkedMultiValueMap()
        responseHeader.set("Content-Type", "application/json")
        def responseBody = [[1, 2, 3, 4], ["foo": "bar"], [10, 20, 30, ["x", "y", "z"]]]
        def responseEntity = new ResponseEntity(responseBody, responseHeader, HttpStatus.OK)
        def requestBody = ["body": [
                "%{\$[0].body.[0].[0]}",
                "%{\$[0].body.[0].[3]}",
                ["foo": "%{\$[0].body.[1].foo}"],
                ["%{\$[0].body.[2].[0]}", "%{\$[0].body.[2].[1]}", "%{\$[0].body.[2].[2]}"],
                "%{\$[0].body.[2].[3]}"]
        ]

        when:
        def result = target.addToResponseHistory(responseEntity)
                .executeExchangeWithHistory(requestBody)

        then:
        result == [
                "body": [1, 4, ["foo": "bar"], [10, 20, 30], ["x", "y", "z"]]
        ]
    }

    def "Execute executeExchangeWithHistory with JsonProcessingException "() {
        given:
        def mockObjectMapper = Mock(ObjectMapper)
        def responseHistoryService = new ResponseHistoryService(mockObjectMapper)
        def responseHeader = new LinkedMultiValueMap()
        responseHeader.set("Content-Type", "application/json")
        def responseBody = ["firstName": "foo", "lastName": "bar"]
        def responseEntity = new ResponseEntity(responseBody, responseHeader, HttpStatus.OK)
        def requestMap = [
                "headers": ["Content-Type": "%{\$[0].headers.Content-Type[0]}"],
                "body"   : ["firstName": "%{\$[0].body.firstName}", "lastName": "%{\$[0].body.lastName}"]
        ]

        when:
        def result = responseHistoryService.addToResponseHistory(responseEntity)
                .executeExchangeWithHistory(requestMap)

        then:
        mockObjectMapper.writeValueAsString(_) >> { throw new JsonProcessingException("Test for exception") }
        // Should not replaced.
        result == [
                "headers": ["Content-Type": "%{\$[0].headers.Content-Type[0]}"],
                "body"   : ["firstName": "%{\$[0].body.firstName}", "lastName": "%{\$[0].body.lastName}"]
        ]
    }

}
