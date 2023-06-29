package spring.integration.tests.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import spring.integration.tests.entity.MongoSupportTest;

public interface MongoSupportTestRepository extends MongoRepository<MongoSupportTest, String> {

}
