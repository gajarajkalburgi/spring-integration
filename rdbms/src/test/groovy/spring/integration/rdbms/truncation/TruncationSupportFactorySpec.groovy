package spring.integration.rdbms.truncation

import spock.lang.Specification
import spock.lang.Unroll
import spring.integration.core.support.TestException
import spring.integration.rdbms.truncation.h2.H2TruncationSupport
import spring.integration.rdbms.truncation.hsql.HsqlTruncationSupport

import javax.sql.DataSource

class TruncationSupportFactorySpec extends Specification {

    def dataSource = Mock(DataSource)

    @Unroll
    def "Parameterized test with getTruncateSupport : #expected"() {
        when:
        def result = TruncationSupportFactory.getTruncateSupport(dataSourceUrl, dataSource)

        then:
        result in expected

        where:
        test | dataSourceUrl                                    | expected
        1    | 'jdbc:hsqldb:mem:dataSource;sql.syntax_ora=true' | HsqlTruncationSupport.class
        2    | 'jdbc:h2:mem:dataSource;sql.syntax_ora=true'     | H2TruncationSupport.class
    }

    def "Unsupported test with #getTruncateSupport"() {
        when:
        TruncationSupportFactory.getTruncateSupport("", dataSource)

        then:
        def ex = thrown(TestException.class)
        ex.getMessage() == "UnSupported database type detected."
    }

}
