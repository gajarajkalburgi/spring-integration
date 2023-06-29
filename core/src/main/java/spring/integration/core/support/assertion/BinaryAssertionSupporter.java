package spring.integration.core.support.assertion;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import spring.integration.core.beans.AssertData;
import spring.integration.core.enums.BodyAssertion;
import spring.integration.core.support.TestException;

class BinaryAssertionSupporter extends AssertionSupporter {

	BinaryAssertionSupporter(ObjectMapper objectMapper) {
		super(objectMapper);
		this.responseType = byte[].class;
	}

	@Override
	public void executeAssert() {
		assertList.forEach(assertData -> {
			try {
				if (assertData.getActual().getClass().equals(this.responseType)) {
					assertAsBinary(assertData);
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
			default:
				assertData = new AssertData(actualBody, expectedBody);
				break;
		}
		return assertData;
	}

	private void assertAsBinary(AssertData assertData) throws IOException {
		String filePath = String.valueOf(assertData.getExpected());
		if (filePath.startsWith(START_CHARACTER_OF_FILE_PATH)) {
			InputStream inputStream = ClassLoader.getSystemResourceAsStream(filePath.replaceFirst(START_CHARACTER_OF_FILE_PATH, ""));
			if (inputStream == null) {
				fail(filePath + " is not found. Please check the path in your test spec.");
			}
			assertArrayEquals(IOUtils.toByteArray(inputStream), (byte[]) assertData.getActual());
		} else {
			fail("Expected contents file path is invalid : ." + filePath);
		}
	}
}
