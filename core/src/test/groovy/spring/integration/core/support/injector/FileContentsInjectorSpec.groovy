package spring.integration.core.support.injector

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import spock.lang.Specification
import spock.lang.Unroll
import spring.integration.core.support.TestException

import java.nio.charset.StandardCharsets

class FileContentsInjectorSpec extends Specification {

    def OBJECT_MAPPER = new ObjectMapper()

    @Unroll
    def "Parameterized test with #inputObject"() {
        expect:
        new FileContentsInjector(OBJECT_MAPPER).inject(inputObject)
        inputObject == exptected // Because parameter object will be updated when it has injection target.

        where:
        test | inputObject                                                    || exptected
        1    | ["key1": ['$ref': "referenced_value_test/values_001.json/#/obj1/str1"],
                "key2": ['$ref': "referenced_value_test/values_001.json/#/obj1/arr1/2"],
                "key3": "val3",
                "key4": ['$ref': "ref_val4"]]                                 || ["key1": "string_one", "key2": 7, "key3": "val3", "key4": ['$ref': "ref_val4"]]
        2    | ["key1": ['$ref': "referenced_value_test/values_001.json/#/obj1", "num2": 2],
                "key2": ['$ref': "referenced_value_test/values_001.json/#/obj1/arr1"],
                "key3": ['$ref': "referenced_value_test/values_002.json/#/"]] || ["key1": ["str1": "string_one",
                                                                                           "boo1": true,
                                                                                           "num1": 1,
                                                                                           "arr1": [3, 5, 7],
                                                                                           "num2": 2],
                                                                                  "key2": [3, 5, 7],
                                                                                  "key3": ["ABC", "OPQ", "XYZ"]]
        3    | ["key1": ['$ref': "xml_support_test/note.xsd/#/"]]             || ["key1": IOUtils.toString(ClassLoader.getSystemResourceAsStream("xml_support_test/note.xsd"), StandardCharsets.UTF_8.name())]
        4    | ["key1": ['$ref': "xml_support_test/note1.xml/#/"]]            || ["key1": IOUtils.toString(ClassLoader.getSystemResourceAsStream("xml_support_test/note1.xml"), StandardCharsets.UTF_8.name())]
        5    | ['$ref': "referenced_value_test/values_001.json/#/"]           || ["obj1": ["str1": "string_one", "boo1": true, "num1": 1, "arr1": [3, 5, 7]],
                                                                                  "obj2": ["obj2_2": ["boo2": true, "num2": 2, "arr2": [3, 5, 7]]]]
    }

    def "Exception with broken json"() {
        when:
        new FileContentsInjector(OBJECT_MAPPER).inject([['$ref': "referenced_value_test/broken_001.json/#/obj1/str1"]])

        then:
        def ex = thrown(TestException)
        ex.getMessage().contains("Fail to read value : ")
    }

}
