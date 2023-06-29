package spring.integration.core.support.injector

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ReferenceValueInjectorSpec extends Specification {

    def OBJECT_MAPPER = new ObjectMapper()

    @Shared
    def PROPERTY_MAP = [
            "foo"       : "FOO",
            "bar"       : "BAR",
            "boo"       : true,
            "num"       : 123,
            "arr"       : [0, "xyz", false],
            "sub_obj"   : ["sub1": "sub_val1"],
            "nested_arr": [['$ref': "#/arr"],
                           ['$ref': "#/arr"],
                           ['$ref': "#/sub_obj",
                            "foo" : "bar"]]
    ]

    @Unroll
    def "Parameterized test with #inputObject"() {
        expect:
        new ReferenceValueInjector(OBJECT_MAPPER, properties).inject(inputObject)
        inputObject == exptected // Because parameter object will be updated when it has injection target.

        where:
        test | properties   | inputObject                                                              || exptected
        1    | PROPERTY_MAP | [:]                                                                      || [:]
        2    | PROPERTY_MAP | []                                                                       || []
        3    | PROPERTY_MAP | ["key1": ['$ref': "#/foo"],
                               "key2": ['$ref': "#/bar"],
                               "key3": ['$ref': "#/boo"],
                               "key4": ['$ref': "#/num"]]                                              || ["key1": "FOO", "key2": "BAR", "key3": true, "key4": 123]
        3    | PROPERTY_MAP | [['$ref': "#/foo"],
                               ['$ref': "#/bar"],
                               ['$ref': "#/boo"],
                               ['$ref': "#/num"]]                                                      || ["FOO", "BAR", true, 123]
        4    | PROPERTY_MAP | ["key1": [['$ref': "#/foo"],
                                        ['$ref': "#/bar"]],
                               "key2": [['$ref': "#/boo"],
                                        ['$ref': "#/num"]],
                               "key3": ['$ref': "#/hoge"]]                                             || ["key1": ["FOO", "BAR"], "key2": [true, 123], "key3": ['$ref': "#/hoge"]]
        5    | PROPERTY_MAP | [[['$ref': "#/foo"],
                                ['$ref': "#/bar"]],
                               [['$ref': "#/boo"],
                                ['$ref': "#/num"]]]                                                    || [["FOO", "BAR"], [true, 123]]
        6    | PROPERTY_MAP | [['$ref': "#/arr/0"],
                               ['$ref': "#/arr/1"],
                               ['$ref': "#/arr/X"],
                               ['$ref': "#/arr/2"],
                               ['$ref': "555555"]]                                                     || [0, "xyz", ['$ref': "#/arr/X"], false, ['$ref': "555555"]]
        7    | PROPERTY_MAP | ["obj1": ['$ref': "#/sub_obj"]]                                          || ["obj1": ["sub1": "sub_val1"]]
        8    | PROPERTY_MAP | ["arr1": ['$ref': "#/arr"]]                                              || ["arr1": [0, "xyz", false]]
        9    | null         | ["key1": ['$ref': "#/foo"]]                                              || ["key1": ['$ref': "#/foo"]]
        10   | [:]          | ["key1": ['$ref': "#/foo"]]                                              || ["key1": ['$ref': "#/foo"]]
        11   | PROPERTY_MAP | ["boo": ['$ref': "#/sub_obj",
                                       "foo" : "bar"]]                                                 || ["boo": ["sub1": "sub_val1", "foo": "bar"]]
        12   | PROPERTY_MAP | ["key1": ['$ref': "#/sub_obj",
                                        "bar" : ['$ref': "#/num"]]]                                    || ["key1": ["sub1": "sub_val1", "bar": 123]]
        13   | PROPERTY_MAP | ["key1": ['$ref'  : "#/sub_obj",
                                        "foobar": ["foobar": [['$ref': "#/foo"], ['$ref': "#/bar"]],
                                                   "barfoo": [['$ref': "#/bar"], ['$ref': "#/foo"]]]]] || ["key1": ["sub1"  : "sub_val1",
                                                                                                                    "foobar": ["foobar": ["FOO", "BAR"],
                                                                                                                               "barfoo": ["BAR", "FOO"]]]]
        14   | PROPERTY_MAP | ["nested_arr": ['$ref': "#/nested_arr"]]                                 || ["nested_arr": [[0, "xyz", false],
                                                                                                                          [0, "xyz", false],
                                                                                                                          ["sub1": "sub_val1", "foo": "bar"]]]
    }

}
