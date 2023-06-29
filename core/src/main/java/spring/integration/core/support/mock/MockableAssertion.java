package spring.integration.core.support.mock;

public interface MockableAssertion<T> {

    void verifyUtilizedMock(T input);
}
