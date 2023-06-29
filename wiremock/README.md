wiremock
=====
Rest API support for Spring boot integration automation. 
It checks actual utilization of mocked endpoint.
The test will be **failure** when it detect request to non-present mocked endpoint.

### Usage
Dependency
```xml
<dependency>
    <groupId>spring.integration</groupId>
    <artifactId>wiremock</artifactId>
    <scope>test</scope>
</dependency>
```
Please check maven to see the actual latest version.

Configuration
```yml
integration:
  wiremock:
    port: 5001
    initialMappings: src/test/resources/wiremock-mappings
```
* port  
  Wiremock server will start using this port number.
* initialMappings (Optional)  
  Set directory path which has initial mappings configuration as json file.  
  Wiremock will load initial mappings from json files.  

### Json file

```json
{
  "metadata" : {
    "name" : "Test Name",
    "description" : "Test description"
  },
  "setup": {
    "wiremock" : [
      {
        "request": {
          "url": "/items?providerId=5",
          "method": "GET"
        },
        "response": {
          "status": 200,
          "jsonBody": [
            {
              "id": 5,
              "providerId" : 5,
              "name": "提供商1Item1",
              "price": 4000.67
            }
          ]
        }
      },
      {
        "request": {
          "url": "/providers?area=cn",
          "method": "GET"
        },
        "response": {
          "status": 200,
          "jsonBody": [
            {
              "id": 5,
              "name": "提供商1",
              "area": "cn"
            }
          ]
        }
      }
    ]
  },
  "request" : {
    "url" : "/request url",
    "method" : "POST",
    "body" : {
          // Request body
     }
  },
  "response" : {
    "status" : 201,
    "body" : {
        // Response Body
    }
  }
}
```
* wiremock  
  Use wiremock field in setup to configure wiremock api call mock.  
  * setup  
    Create endpoint with request and response which specified.  
    `cleanup` doesn't need, because all request provided in setup will be automatically removed from wiremock once test is finished.
    

* `MockableAssertion`  
  Verify unmatched request to wiremock endpoint not existing when IntegrationBase#isMockableAssertionMandatory is overridden as follow.  
  ```
  @EnableIntegration
  public class SpringIntegrationApplicationTests extends IntegrationBase {
  
  	@Override
  	protected String getTestDataLocation() {
  		return "src/test/resources/integration/test/";
  	}
  
  	// Specify true to verify the mocked endpoint has been used actually.    
  	@Override
  	protected boolean isMockableAssertionMandatory() {
  		return true;
  	}
  }
  ```
