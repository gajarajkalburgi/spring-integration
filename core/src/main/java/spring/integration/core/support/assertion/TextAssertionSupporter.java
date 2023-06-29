package spring.integration.core.support.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import spring.integration.core.beans.AssertData;
import spring.integration.core.enums.BodyAssertion;
import spring.integration.core.support.TestException;

class TextAssertionSupporter extends AssertionSupporter {

	TextAssertionSupporter(ObjectMapper objectMapper) {
		super(objectMapper);
		this.responseType = String.class;
	}

	@Override
	public void executeAssert() {
		assertList.forEach(assertData -> {
			try {
				if (assertData.isRegex()) {
					assertWithRegex(assertData);
				} else {
					assertEquals(assertData.getExpected(), assertData.getActual());
				}
			} catch (Throwable th) {
				throw new TestException(th.getMessage(), th);
			}
		});
	}

	@Override
	protected AssertData body(Object actualBody, Object expectedBody, Map<String, Object> assertOptions) {
		AssertData assertData = null;
		switch (BodyAssertion.fromAssert(assertOptions)) {
			case IGNORE:
				break;
			case REGEX:
				assertData = new AssertData(actualBody, expectedBody, true);
				break;
			case EQUALS:
			default:
				assertData = new AssertData(actualBody, expectedBody);
				break;
		}
		return assertData;
	}

}
