package spring.integration.tests.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Document
public class MongoSupportTest {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private Date dateValue;
    private Long longValue;
    private BigDecimal bigDecimalValue;
    private List<Object> listOfAll;
}
