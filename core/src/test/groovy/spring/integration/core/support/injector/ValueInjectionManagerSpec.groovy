package spring.integration.core.support.injector

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ValueInjectionManagerSpec extends Specification {

    @Shared
    def OBJECT_MAPPER = new ObjectMapper()

    @Shared
    def SPECIFICATION_DATA_WITH_PROPERTIES = ["metadata": ["properties":
                                                                   ["foo": "FOO",
                                                                    "bar": "BAR"
                                                                   ]],
                                              "setup"   : ["database": [['$ref': "referenced_value_test/values_001.json/#/obj1/str1"]],
                                                           "wiremock": [['$ref': "referenced_value_test/values_001.json/#/"]]],
                                              "request" : [
                                                      "url"        : "/restrictions",
                                                      "method"     : "POST",
                                                      "contentType": "application/json",
                                                      "header"     : [
                                                              "Token": [
                                                                      "\$ref": "#/foo"
                                                              ]
                                                      ],
                                                      "body"       : ["param": [
                                                              "\$ref": "#/bar"
                                                      ]]
                                              ],
                                              "response": ["result": [
                                                      "\$ref": "#/bar"
                                              ]],
                                              "cleanup" : ["database": [['$ref': "referenced_value_test/values_001.json/#/obj1/str1"]]]
    ]

    @Shared
    def SPECIFICATION_DATA_WITH_PROPERTIES_AFTER_INJECTIONS = ["metadata": ["properties":
                                                                                    ["foo": "FOO",
                                                                                     "bar": "BAR"
                                                                                    ]],
                                                               "setup"   : ["database": ["string_one"],
                                                                            "wiremock": [
                                                                                    ["obj1": ["str1": "string_one",
                                                                                              "boo1": true,
                                                                                              "num1": 1,
                                                                                              "arr1": [3, 5, 7]],
                                                                                     "obj2": ["obj2_2": [
                                                                                             "boo2": true,
                                                                                             "num2": 2,
                                                                                             "arr2": [3, 5, 7]]]]]],
                                                               "request" : [
                                                                       "url"        : "/restrictions",
                                                                       "method"     : "POST",
                                                                       "contentType": "application/json",
                                                                       "header"     : [
                                                                               "Token": "FOO"
                                                                       ],
                                                                       "body"       : ["param": "BAR"]
                                                               ],
                                                               "response": ["result": "BAR"],
                                                               "cleanup" : ["database": ["string_one"]]]

    @Shared
    def SPECIFICATION_DATA_NO_PROPERTIES = ["metadata": [:],
                                            "setup"   : ["database": [['$ref': "referenced_value_test/values_001.json/#/obj1/str1"]]],
                                            "request" : [
                                                    "url"        : "/restrictions",
                                                    "method"     : "POST",
                                                    "contentType": "application/json",
                                                    "header"     : [
                                                            "Token": [
                                                                    "\$ref": "#/foo"
                                                            ]
                                                    ],
                                                    "body"       : ["param": [
                                                            "\$ref": "#/bar"
                                                    ]]
                                            ],
                                            "response": ["result": [
                                                    "\$ref": "#/bar"
                                            ]],
                                            "cleanup" : ["database": [['$ref': "referenced_value_test/values_001.json/#/obj1/str1"]]]
    ]

    @Unroll
    def "Parameterized test with #specificationData"() {
        expect:
        ValueInjectionManager.inject(OBJECT_MAPPER, specificationData) == exptected

        where:
        test | specificationData                  || exptected
        1    | SPECIFICATION_DATA_WITH_PROPERTIES || SPECIFICATION_DATA_WITH_PROPERTIES_AFTER_INJECTIONS
        2    | SPECIFICATION_DATA_NO_PROPERTIES   || ["metadata": [:],
                                                      "setup"   : ["database": ["string_one"]],
                                                      "request" : [
                                                              "url"        : "/restrictions",
                                                              "method"     : "POST",
                                                              "contentType": "application/json",
                                                              "header"     : [
                                                                      "Token": [
                                                                              "\$ref": "#/foo"
                                                                      ]
                                                              ],
                                                              "body"       : ["param": [
                                                                      "\$ref": "#/bar"
                                                              ]]
                                                      ],
                                                      "response": ["result": [
                                                              "\$ref": "#/bar"
                                                      ]],
                                                      "cleanup" : ["database": ["string_one"]]]
    }
}
