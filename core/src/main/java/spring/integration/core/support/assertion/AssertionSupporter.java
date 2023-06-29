package spring.integration.core.support.assertion;


import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import spring.integration.core.beans.AssertData;
import spring.integration.core.enums.HeaderAssertion;
import spring.integration.core.support.TestException;
import spring.integration.core.utils.ObjectConverter;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class AssertionSupporter {

	/**
	 * Start character of file path which will be loaded in integration.
	 */
	static final String START_CHARACTER_OF_FILE_PATH = "@";

	private static final String MESSAGE_FORMAT_OF_REGEX = "Tried regex matching with '%s', but it's %s actually.";

	protected final ObjectMapper objectMapper;

	List<AssertData> assertList;

	@Getter
	Class responseType;

	/**
	 * Assert data setup.
	 *
	 * @param metaData Map object
	 * @param actual   ResponseEntity object
	 * @param expected Map object
	 *
	 */
	void setUpAssertList(Map<String, Object> metaData, ResponseEntity actual, Map<String, Object> expected) {
		Map<String, Object> assertOptions = ObjectConverter.toMap(objectMapper, metaData.get("assert"));
		List<AssertData> assertList = new ArrayList<>();
		assertList.add(httpStatus(actual.getStatusCodeValue(), expected.get("status")));
		assertList.addAll(header(actual.getHeaders(), expected.get("headers"), assertOptions));
		Optional.ofNullable(body(actual.getBody(), expected.get("body"), assertOptions)).ifPresent(assertList::add);
		this.assertList = assertList;
	}

	/**
	 * Execute assert.
	 */
	public abstract void executeAssert();

	/**
	 * Body data setup.
	 *
	 * @param assertOptions Map object
	 * @param actualBody    Object
	 * @param expectedBody  Object
	 *
	 * @return Assert Data
	 */
	protected abstract AssertData body(Object actualBody, Object expectedBody, Map<String, Object> assertOptions);


	/**
	 * Header data setup.
	 *
	 * @param assertOptions   Map object
	 * @param expectedHeaders HttpHeaders object
	 * @param actualHeaders   Object
	 *
	 * @return Assert Data List
	 */
	protected List<AssertData> header(HttpHeaders actualHeaders, Object expectedHeaders, Map<String, Object> assertOptions) {
		Boolean isRegex = HeaderAssertion.REGEX.equals(HeaderAssertion.fromAssert(assertOptions));
		List<AssertData> assertDataList = new ArrayList<>();
		Map<String, Object> expectedHeadersMap = ObjectConverter.toMap(objectMapper, expectedHeaders);
		for (Map.Entry<String, Object> expectedHeader : expectedHeadersMap.entrySet()) {
			String actual = Optional.ofNullable(actualHeaders.get(expectedHeader.getKey()))
					// Due to RFC 7230 (Header can have multiple fields with same name), we merge it to single string before assertion.
					.map(a -> a.stream().map(Object::toString).collect(Collectors.joining(","))).orElse(null);
			assertDataList.add(new AssertData(actual, expectedHeader.getValue(), isRegex, JSONCompareMode.STRICT));
		}
		return assertDataList;
	}

	/**
	 * Assert by regex pattern.
	 */
	protected void assertWithRegex(AssertData assertData) {
		String regex = assertData.getExpected().toString();
		Object actual = assertData.getActual();
		if (actual == null) {
			fail(String.format(MESSAGE_FORMAT_OF_REGEX, regex, "NULL"));
		}
		if (!actual.toString().matches(regex)) {
			fail(String.format(MESSAGE_FORMAT_OF_REGEX, regex, actual.toString()));
		}
	}

	/**
	 * HTTP status data setup.
	 *
	 * @param actualStatus   Integer object
	 * @param expectedStatus Integer object
	 *
	 * @return Assert data list
	 */
	private AssertData httpStatus(Integer actualStatus, Object expectedStatus) {
		if (expectedStatus instanceof Integer) {
			return new AssertData(actualStatus, expectedStatus, false, JSONCompareMode.STRICT);
		} else {
			throw new TestException("Expected http Status is required in test.");
		}
	}

}
