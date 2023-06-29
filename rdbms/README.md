rdbms
=====
RDBMS support for Spring boot integration automation.

### Usage
Dependency
```xml
<dependency>
  <groupId>spring.integration</groupId>
  <artifactId>rdbms</artifactId>
  <scope>test</scope>
</dependency>
```
Please check maven to see the actual latest version.

Configuration
```yml
spring:
  datasource:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    url: jdbc:hsqldb:mem:dataSource;sql.syntax_ora=true
    username: sa
    password:
    defaultAutoCommit: true
    initialSize: 1
    maxActive: 5
    maxIdle: 3
    maxWait: 60000
    minIdle: 1

integration:
  database:
    init: src/test/resources/database/init.sql
```

* init (Optional)  
  Initialize database.

### Json file

```json
{
  "metadata" : {
    "name" : "Test Name",
    "description" : "Test description"
  },
  "setup": {
    "database" : [
      "INSERT INTO X (A,B,C) VALUES (1,2,3)",
      "INSERT INTO X (A,B,C) VALUES (4,5,6)"
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
  },
  "cleanup": {
    "database": [
      "DELETE FROM Y WHERE ID IN (SELECT ID FROM X)",
      "DELETE FROM Z WHERE ID IN (SELECT ID FROM X)"
    ]
  }
}
```
* database  
  Use database field in setup to setup data into database. (Hsql / H2 both are supported)
  * setup  
    Create setup data for test. All tables data will be removed automatically after test execution finished.

  * cleanup  
    Execute queries which specified.  
    For example, it's able to manipulate data after the test when set update query in this field.

  * Auto truncation  
    Auto truncation works only when json has some setup for database. If you want to truncate data without any setup queries.  
    E.g : Create records by post does not need setup queries but need to cleanup database.  
    Please use empty setup for database.
    ```json
    "setup" : {
        "database" : []
    }
    ```