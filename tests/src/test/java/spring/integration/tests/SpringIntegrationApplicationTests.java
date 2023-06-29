package spring.integration.tests;

import spring.integration.core.IntegrationBase;
import spring.integration.core.config.EnableIntegration;

@EnableIntegration
public class SpringIntegrationApplicationTests extends IntegrationBase {

    @Override
    protected String getTestDataLocation() {
        return "src/test/resources/integration/test/";
    }

    @Override
    protected boolean isMockableAssertionMandatory() {
        return true;
    }
}
