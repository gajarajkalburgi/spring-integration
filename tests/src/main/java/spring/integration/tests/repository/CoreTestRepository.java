package spring.integration.tests.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import spring.integration.tests.entity.CoreTest;

import java.util.Optional;

@RepositoryRestResource(path = "core-test")
public interface CoreTestRepository extends CrudRepository<CoreTest, Long> {

    @RestResource
    @Override
    Optional<CoreTest> findById(Long aLong);
}
