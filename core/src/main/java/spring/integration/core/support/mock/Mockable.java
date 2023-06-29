package spring.integration.core.support.mock;

import java.util.List;

public interface Mockable<S, C> {

    void setup(List<S> elements);

    void defaultCleanUp(List<S> elements);

    void cleanup(List<C> elements);

    void destroy();
}
