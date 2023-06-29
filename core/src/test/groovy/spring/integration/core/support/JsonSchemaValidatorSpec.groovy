package spring.integration.core.support

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.Unroll

class JsonSchemaValidatorSpec extends Specification {

    def objectMapper = new ObjectMapper(new JsonFactory() {
        {
            this.enable(JsonParser.Feature.ALLOW_COMMENTS)
        }
    })


    @Unroll
    def "test validate with valid specification file"() {
        given:
        def specificationFile = new File(this.getClass().getResource(targetJsonFile).getFile())

        when:
        JsonSchemaValidator.validate(objectMapper, specificationFile)

        then:
        notThrown(TestException)

        where:
        targetJsonFile << ["/json_schema_validator_test/validForJson.json", "/json_schema_validator_test/validForXml.json"]
    }

    def "test validate with invalid specification file"() {
        given:
        def specificationFile = new File(this.getClass().getResource("/json_schema_validator_test/invalid.json").getFile())

        when:
        JsonSchemaValidator.validate(objectMapper, specificationFile)

        then:
        def ex = thrown(TestException)
        ex.getMessage() =~ "invalid.json contains violation of json schema. .*"
    }

    def "test validate with broken specification file"() {
        given:
        def specificationFile = new File(this.getClass().getResource("/referenced_value_test/broken_001.json").getFile())

        when:
        JsonSchemaValidator.validate(objectMapper, specificationFile)

        then:
        thrown(TestException)
    }

}
