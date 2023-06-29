Spring Boot Integration Testing Framework
=====

* Spring Boot Integration Testing framework.
```
spring.integration (Parent project)
│
├─ core (Spring boot integration automation core)
│
├─ wiremock (Rest API support for Spring boot integration automation)
│
├─ rdbms (RDBMS support for Spring boot integration automation)
│
└─ tests (Integration testing module for core)
```

# Usage
## Setup
You need to import this library to your classpath and prepare a Java class to run the integration test with this library.   
This section explains how to prepare those things.

### `pom.xml`
#### dependency
```xml
<dependency>
    <groupId>spring.integration</groupId>
    <artifactId>core</artifactId>
    <scope>test</scope>
</dependency>
```

### Test Runner
Execute with class extending `IntegrationBase`.  
`IntegrationBase` is an abstract class which implements `executeTest()` method.  You can run this method to execute integration tests.  
The only thing you need to do are to implement `getTestDataLocation()` method and to add `IntegrationEnable` annotation in you extended class, and adding maven plugin to enable it.    
It needs only to specify `maven-failsafe-plugin` on plugin section in pom.
Here is an example of extended class and the pom setup you need.

##### Build part of `pom.xml`
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-failsafe-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

##### Example of test class
```java
import IntegrationBase;
import EnableIntegration;

@EnableIntegration
public class ExampleIntegration extends IntegrationBase {

    /**
	 * Required method to get directory path of test data location.
	 */
	@Override
	protected String getTestDataLocation() {
		return "src/test/resources/integration/test/";
	}

}
```

| Method                                       |Description|Default Value|Required|
|----------------------------------------------|-----|:---:|:---:|
| IntegrationBase#getTestDataLocation          |Specifies base resource directory for Test Specification JSON files. For each `*.json` files under provided base directory will be executed as a single test.|null|Yes|
| IntegrationBase#getIncludePattern            |Specify a RegEx filter for the target Test Specification JSON files. |`".+\\.(?i)json\$"`|No|
| IntegrationBase#isMockableAssertionMandatory |Specify boolean to execute mockable assertion(verify unmatched request to wiremock endpoint not existing) or not.|`false`|No|

## Test Specification JSON
After preparing a Test Runner, you need to create a Test Specification JSON under the package specified by `IntegrationBase#getTestDataLocation` to define a Integration test case. Here is an example Test Specification JSON.
```json
/**
 * This is example of test specification.
 * It's able to contain block and line comment like this example.
 */
{
  "metadata": {
    "name": "Test Name",
    "description": "Test description",
    "assert": {
      "body": "SCHEMA",
      "header": "REGEX",
      "ignore": [
        "$._links",
        "$..id"
      ],
      "order": false,
      "swagger": "http://dev-atrdocs101z.dev.jp.local:8080/api-docs-provider/config/restriction/restriction.yml"
    },
    "properties": {
      "token": "```messageDigest('credential')```",
      "today": "```currentDate(\"yyyy-MM-dd\")```",
      "foo": "bar"
    }
  },
  "setup": {},
  // This example contains multiple request and response with `scenario` feature.
  "scenario": [{
      "request": {
        "url": "/request url for POST",
        "method": "POST",
        "headers": {
          "token": {
            "$ref": "#/token"
          }
        },
        "body": {
          "startDate": {
            "$ref": "#/today"
          },
          "paramFoo": {
            "$ref": "#/foo"
          }
        }
      },
      "response": {
        "status": 201,
        "headers": {
          "location": "^http://localhost:[0-9]+/.*/.+$",
          "Content-Type": "application/json"
        },
        "body": {
          "resultFoo": {
            "$ref": "#/foo"
          }
        }
      }
    },
    {
      "request": {
        "url": "/request url for GET",
        "method": "GET",
        "query": {
          "resultFoo": "%{$[0].body.resultFoo}"
        }
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json"
        },
        "body": {
          "startDate": {
            "$ref": "#/today"
          },
          "paramFoo": {
            "$ref": "#/foo"
          }
        }
      }
    },
    {
      "request": {
        "url": "/request url for DELETE",
        "method": "DELETE",
        "headers": {
          "token": {
            "$ref": "#/token"
          }
        }
      },
      "response": {
        "status": 204,
        "body": null
      }
    },
    {
      "request": {
        "url": "/request url for GET once again",
        "method": "GET",
        "query": {
          "resultFoo": "%{$[0].body.resultFoo}"
        }
      },
      "response": {
        "status": 404,
        "body": null
      }
    }
  ],
  "cleanup": {}
}
```

#### `/metadata` <small>`[Required]`</small>
- `/metadata/name` `[Str:wqing]` `[Required]`  
  Test Name will be shown in the log when the test is executed.
- `/metadata/description` `[String]` `[Required]`  
  Test Description is for describing what the test is about.
- `/metadata/assert` `[Optional]`
    - `/metadata/assert/body` `[String]` `[Optional]`

      |Value|Description|Default|
          |-----|-----|:---:|
      |`"EQUALS"`|It will assert response body if it matches the exact JSON defined in `/response/body`.|&#10003;|
      |`"SCHEMA"`|It will assert response body if it matches the JSON Schema defined in `/response/body`.||
      |`"IGNORE"`|It will not assert response body.||
    - `/metadata/assert/header` `[String]` `[Optional]`

      |Value|Description|Default|
          |-----|-----|:---:|
      |`"EQUALS"`|It will assert only listed response header if it matches the exact JSON defined in `/response/headers`.|&#10003;|
      |`"REGEX"`|It will assert only listed response header if it matches the REGEX defined in `/response/headers`.||

    - `/metadata/assert/ignore` `[String List]` `[Optional]`

      Specific Fields can be ignored by adding [json path](https://github.com/json-path/JsonPath) to "ignore" array.
    - `/metadata/assert/order` `[boolean]` `[Optional]`

      |Value|Description|Default|
          |-----|-----|:---:|
      |`true`|JSON arrays must be in strict sequence|&#10003;|
      |`false`|JSON arrays does not has to be in strict sequence||

    - `/metadata/assert/swagger` `[String]` `[Optional]`  
      Validate request and response with `Swagger Specification` The loadable location depends on pattern of specified value.  
      <font color="Red">Caution</font> : It does not support `XML` response, just only for `JSON` only.

      |Pattern|Description|Note|
          |-----|-----|:---:|
      |`^http.*$`|Load specificaton from remote server via http.|e.g : `http://dev-atrdocs101z.dev.jp.local:8080/api-docs-provider/config/restriction/restriction.yml`|
      |`^/.*$`|Load specificaton on localhost server via http.|e.g : `/v2/api-docs?group=core-it`<br>It's expected to use when project has swagger specification on itself, will be interpreted to `http://localhost:{port}/{context}/v2/api-docs?group=core-it`|
      |Other|Use specificaton on local resource folder.|e.g : `./`|

- `/metadata/properties` `[Optional]`  
  Define values which is able to refer from other field.

#### `/setup` <small>`[Optional]`</small> & `/cleanup` <small>`[Optional]`</small>
As it's obvious from its name, `/setup` and `/cleanup` is used to setup/cleanup the testing environment before sending the request and after receiving the response. You can use several plugins in `/setup` & `/cleanup` element to accomplish that. Currently available plugins are listed below. Please refer each plugin's README for their usage.

| Plugins               |Description|
|-----------------------|-----|
| [wiremock](wiremock/) |Mocks API calls by using wiremock.|
| [rdbms](rdbms/)       |Run H2 database locally and query there.|

#### `/request` <small>`[Required when it doesn't contain scenario]`</small>
You will define a HTTP Request as a JSON object in `/request` element. This `/request` will be sent after `/setup` is executed.
- `/request/url` `[String]` `[Required]`  
  Specifies endpoint **Path**.
- `/request/method` `[String]` `[Required]`  
  Specifies HTTP Method.
- `/request/headers` `[Object]` `[Optional]`  
  Specifies HTTP Request Headers.
- `/request/body` `[Object or List or String]` `[Optional]`  
  Specifies HTTP Request Body.
- `/request/query` `[Object]` `[Optional]`  
  Specifies HTTP Request Query String as key and value.
- `/request/path` `[String or List]` `[Optional]`  
  Specifies HTTP Request path parameter.

#### `/response` <small>`[Required when it doesn't contain scenario]`</small>
You will define an expected HTTP Response as a JSON object in `/response` element. `/cleanup` will be executed after actual HTTP Response is asserted with expected HTTP Response.

- `/response/status` `[Integer]` `[Required]`  
  Specifies expected HTTP Status Code.
- `/response/headers` `[Object]` `[Optional]`  
  Specifies expected HTTP Response Headers. (It only asserts listed HTTP Response Headers.)
- `/response/body` `[Object,Array,String, null, boolean, integer, number]` `[Required when /metadata/assert/body is EQUALS or SCHEMA]`  
  Specifies expected HTTP Response Body. It's only applicable with JSON format response body.

#### `/scenario` <small>`[Required when it doesn't contain request and response]`</small>
You will define multiple `request` and `/response` element as JSON array.  
Specified request and response will be executed with the order of this array.
- `/scenario/response` `[Object]` `[Required]`  
  Specifies HTTP request which you want to send.
- `/scenario/response` `[Object]` `[Required]`  
  Specifies HTTP response which you expected.


## Test Xml based application

The way of writing xml_based cases are the same as json_based cases,  
However you need to set value of **`Content-Type` in response header**.  
It will be executed as xml_based case when the above header field contains `xml` .   
It will skip the similiar parts and only list the diffrences here.  
Xml cases are still described in json but you can include xml files under resources directory as request/response body, you need to create a Test Specification under the package specified by `IntegrationBase#getTestDataLocation` to define a integration test case.
```json
{
  "metadata": {
    "name": "Xml Test Example",
    "description": "This is an example for xml based cases",
    "assert": {
      "body": "EQUALS"
    }
  },
  "setup": {
  },
  "request": {
    "url": "/examples/example01",
    "method": "POST",
    "headers": {
      "content-type": "application/xml"
    },
    "body": {
      "$ref": "requests/req.xml/#/"
    }
  },
  "response": {
    "status": 201,
    "headers": {
      "content-type": "application/xml"
    },
    "body": {
      "$ref": "response/res.xml/#/"
    }
  },
  "cleanup": {
  }
}
```

#### `/metadata` <small>`[Required]`</small>
- `/metadata/assert` `[Optional]`
    - `/metadata/assert/body` `[String]` `[Optional]`

      |Value|Description|Default|
          |-----|-----|:---:|
      |`"EQUALS"`|It will assert response body if it matches the Xml defined in `/response/body`.|&#10003;|
      |`"SCHEMA"`|It will assert response body match the Xsd defined in `/response/body`.||
      |`"IGNORE"`|It will not assert response body.||
    - `/metadata/assert/order` `[boolean]` `[Optional]`

      |Value|Description|Default|
          |-----|-----|:---:|
      |`true`|xml attributes or child nodes must be in strict sequence|&#10003;|
      |`false`|xml attributes or child nodes does not has to be in strict sequence||

## Assert non text response
Support assertion to non text response with complete matching.
Response header should have content-type which matches with the follows.
- `application/octet-stream`
- `application/pdf`
- `image/*`
- `video/*`
- `audio/*`
- `*zip*`
- `*protobuf*`

It needs to specify **file path** with `"@{file path}"` in expected response body when utilize this feature.   
Of course the file should exist on your local actually.    
Because it needs to load expected value when execute assertion.
Here is an example Test Specification JSON.
```json
{
  "metadata": {
    "name": "Assert Binary",
    "description": "Assert Binary"
  },
  "setup": {
  },
  "request": {
    "url": "/media/",
    "method": "GET",
    "headers": {
      "Accept": "application/pdf"
    }
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/pdf"
    },
    "body": "@media/spoke.pdf"
  },
  "cleanup": {
  }
}
```

## Assert plain text and html response
Support assertion to plain text and html response.
Response header should have content-type which matches with the follows.
- `application/xhtml+xml`
- `text/*`
  Here is an example Test Specification JSON.
```json
{
  "metadata": {
    "name": "Assert REGEX",
    "description": "Assert REGEX",
    "assert": {
      "body": "REGEX"
    }
  },
  "setup": {
  },
  "request": {
    "url": "/text/simpletext",
    "method": "GET"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "text/plain"
    },
    "body": "Hello [a-z]+ in Integration."
  },
  "cleanup": {
  }
}
```

#### `/metadata` <small>`[Required]`</small>
- `/metadata/assert` `[Optional]`
    - `/metadata/assert/body` `[String]` `[Optional]`

      |Value|Description|Default|
          |-----|-----|:---:|
      |`"EQUALS"`|It will assert response body if it matches the text defined in `/response/body`.|&#10003;|
      |`"REGEX"`|It will assert response body match the regex pattern defined in `/response/body`.||
      |`"IGNORE"`|It will not assert response body.||

### Value Injection
You would want to have values injecting from other places or something.
Hence we defined a syntax to inject with actual values or dynamic value.

#### Dynamic value injection
In `metadata.properties` and `request` field,  
it's able to use functions which is surrounded with **```** to where you want to inject dynamic value as follow.
```json
{
  "metadata" : {
    ... // omitted
    "properties" : {
      "boo": "```randomBoolean()```"
    }
  },
  "setup": {},
  "request": {
    "url": "/restrictions",
    "method": "POST",
    "body": {
      "count1": "```1 + 1```",
      "count2": "```1 + 2```",
      "today": "```currentDate(\"yyyy-MM-dd'T'HH:mm:ss\")```"
    }
  },
  "response" : {
    ... // omitted
  },
  "cleanup": {}
}
```
- The ```boo``` in ```metadata.properties``` will be ```true``` or ```false``` at ramdom.
- The ```count1, count2``` in ```request.body``` will be caluculated values.
- The ```today``` in ```request.body``` will be current data string with ```yyyy-MM-dd'T'HH:mm:ss``` format.


#### Reference defined value in properties
In all fields which excluding `metadata.properties`,   
it's able to refer value which defined in properties with `{"$ref" : "#/FIELD_NAME"}` as follow.
```json
{
  "metadata" : {
    ... // omitted
    "properties" : {
      "boo": true,
      "obj1": {"str3": "XYZ"},
      "arr": [1,2,3]
    }
  },
  "setup": {},
  "request": {
    "url": "/restrictions",
    "method": "POST",
    "body": {
      "booboo": {
        "$ref" : "#/boo"
      },
      "strstr": {
        "$ref" : "#/obj1/str3"
      },
      "numnum": {
        "$ref" : "#/arr/0"
      }
    }
  },
  "response" : {
    ... // omitted
  },
  "cleanup": {}
}
```
- The value of ```booboo``` in ```request.body``` will be replaced with value of ```metadata.properties.boo```.
- The value of ```strstr``` in ```request.body``` will be replaced with value of ```metadata.properties.obj1.str3```.
- The value of ```numnum``` in ```request.body``` will be replaced with value of ```metadata.properties.arr[0]```.

#### Value injection from external file
All fields which excluding `metadata.properties`, it's able to inject values of another file.  
Assume that it's configured as follows in another file.

**src/test/resources/integration/variables/objective.json**
```json
{
  "query1" : "INSERT INTO EXAMPLE VALUES (1,2,3)",
  "query2" : "DELETE FROM EXAMPLE",
  "obj1": {
    "sub_obj1": {"foo": "bar"},
    "sub_arr1": ["element1","element2"]
  }
}
```
**src/test/resources/integration/test/test.json**
```json
{
  "metadata" : {
    ... // omitted
  },
  "setup": {
    "database" : [
      {
        "$ref": "integration/variables/objective.json/#/query1"
      }
    ],
    "wiremock": {
      "$ref": "integration/variables/wiremockTest.json/#/"
    }
  },
  "request" : {
    "$ref": "integration/variables/objective.json/#/obj1/sub_obj1/foo"
  },
  "response" : {
    "$ref": "integration/variables/objective.json/#/obj1/sub_arr1"
  },
  "cleanup": {
    "database" : [
      {"$ref": "integration/variables/objective.json/#/query2"}
    ]
   }
 }
```
- The first value of ```setup.database``` will be replaced with value of ```query1``` in objective.json file.
- The value of ```request``` will be replaced with value of ```obj1.sub_obj1.foo``` in objective.json file.
- The value of ```response``` will be replaced with value of ```obj1.sub_obj1.sub_arr1``` in objective.json file.
- The first value of ```cleanup.database``` will be replaced with value of ```query2``` in objective.json file.
- The first value of ```wiremock``` in ```setup``` will be replaced with entire JSON contents of specified file.

### Multipart request
Support multipart request in test specification.  
It's expected to use for endpoint which has feature like file uploading.  
The following two points should be implemented to utilize this feature in the test specification file.
1. Set `multipart/form-data` to `Content-Type` in request header.
1. Use `"@{file path}"` syntax to contain file contents in your request.  
   For example, specify `"@foo/bar/image.png"`, when you need to set data of `foo/bar/image.png` to request body.

Here is an example Test Specification JSON.
```json
{
  "metadata" : {
    ... // omitted
  },
  "setup": {
  },
  "request" : {
    "url": "/upload/",
    "method": "POST",
    "headers": {
      "Accept": "*/*",
      // Should be set this to send multipart request.
      "Content-Type": "multipart/form-data"
    },
    "body": {
      // Set "@" + "{filePath}" to contain file contents in your request.
      "media": "@integration/image/spring-boot.png",
      "stringValue": "This is Spring boot logo.",
      "metadata": {
        "url": "https://spring.io/"
      }
    }
  },
  "response": {
    "status": 200,
    "body": [
      "media",
      "stringValue",
      "metadata"
    ]
  },
  "cleanup": {}
}
```

### Reference value from previous responses with `scenario` feature
Support to re utilize values from previous responses in a test specification.  
It's expected to use for scenario test with multiple requests and responses.
1. Specify `scenario` in the test specification.
2. Define list of pair which has request and response.
3. Specify reference from previous response with `JSON Path` like `"%{$[0].body.id}"`.  
   <font color="Red">Caution</font> : This `reference from previous response` feature is supported only JSON response.

Here is an example Test Specification JSON.
```json
{
  "metadata" : {
    ... // omitted
  },
  "setup": {
  },
  "scenario": [{
      "request": {
        "url": "/create",
        "method": "POST",
        "headers": {
          "Accept": "*/*",
          "Content-Type": "application/json"
        },
        "body": {
          "firstName": "foo",
          "lastName": "bar",
          "age": 24
        }
      },
      "response": {
        "status": 201,
        "body": {
          "id": 1234
        }
      }
    },
    {
      "request": {
        "url": "/user",
        "method": "GET",
        "path": [
          // Utilize id from the first response
          "%{$[0].body.id}"
        ]
      },
      "response": {
        "status": 200,
        "body": {
          "firstName": "foo",
          "lastName": "bar",
          "age": 24
        }
      }
    },
    {
      "request": {
        "url": "/search",
        "method": "GET",
        "query": {
          // Utilize age from the second response
          "age": "%{$[1].body.age}"
        }
      },
      "response": {
        "status": 200,
        "body": {
          "firstName": "foo",
          "lastName": "bar",
          "age": 24
        }
      }
    }
  ],
  "cleanup": {}
}
```


### Validate your Test Specification JSON
You can use [schema.json](core/src/main/resources/schema.json) as json schema for validation.  
By [configuring on your IntelliJ](https://www.jetbrains.com/help/idea/json-schema.html), you can get support from IntelliJ.

# How to contribute
You might feel this framework should have more features. In that case, please follow the steps below and you can contribute to this project.

1. Create a ticket in your project and describe your idea. The description should include
    - What feature you want to add/change.
    - Detail of interface change. Example json (you can omit unrelated part).
    - Overview of the strategy to implement it.
2. People in charge of this framework will decide if that feature is okay to implement or not.
3. People in charge of this framework will decide which version can include that feature.
4. People in charge of this framework will decide in which branch it should be implemented.
5. You can start the implementation.

If you want us to implement, you can request it. You can comment so in the ticket.

# Version up in `pom.xml`
We can use `versions-maven-plugin`. it's able to update version in each pom files at once.
```bash
# Update to =1.1.2-SNAPSHOT
$ ./mvnw versions:set -DnewVersion=1.1.2-SNAPSHOT versions:commit
```

# tests
This is a Integration Testing module which is actually use `core` and other modules.
If you implement new feature for `core`, please add test cases on this module.
