package spring.integration.core.support.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import spring.integration.core.beans.AssertData;
import spring.integration.core.enums.BodyAssertion;
import spring.integration.core.support.TestException;
import spring.integration.core.utils.ObjectConverter;

class JsonAssertionSupporter extends AssertionSupporter {

	JsonAssertionSupporter(ObjectMapper objectMapper) {
		super(objectMapper);
		this.responseType = Object.class;
	}

	@Override
	protected AssertData body(Object actualBody, Object expectedBody, Map<String, Object> assertOptions) {
		AssertData assertData = null;
		switch (BodyAssertion.fromAssert(assertOptions)) {
			case IGNORE:
				break;
			case SCHEMA:
				ProcessingReport report = validationSchema(actualBody, expectedBody);
				if (!report.isSuccess()) {
					assertData = new AssertData(report, null, false, JSONCompareMode.STRICT);
				}
				break;
			case EQUALS:
				Object bodyExcludingIgnoreField = removeIgnoredFields(actualBody, ObjectConverter.toStringList(objectMapper, assertOptions.get("ignore")));
				assertData = new AssertData(bodyExcludingIgnoreField, expectedBody, false, getJsonCompareMode(assertOptions));
				break;
			default:
		}
		return assertData;
	}

	@Override
	public void executeAssert() {
		assertList.forEach(assertData -> {
			if (assertData.isRegex()) {
				assertWithRegex(assertData);
			} else {
				if (JSONCompareMode.STRICT.equals(assertData.getJsonCompareMode())) {
					if (!Objects.deepEquals(assertData.getExpected(), assertData.getActual())) {
						try {
							// The following assertion must be failure, but it is required to show actual differences.
							assertEquals(objectMapper.writeValueAsString(assertData.getExpected()),
									objectMapper.writeValueAsString(assertData.getActual()));
						} catch (JsonProcessingException ex) {
							throw new TestException(ex.getMessage(), ex);
						}
					}
				} else {
					assertWithJsonAssert(assertData);
				}
			}
		});
	}

	private void assertWithJsonAssert(AssertData assertData) {
		try {
			Object jsonObject = convert(assertData.getExpected());
			if (jsonObject instanceof JSONObject) {
				JSONAssert.assertEquals(
						JSONObject.class.cast(jsonObject),
						JSONObject.class.cast(convert(assertData.getActual())),
						assertData.getJsonCompareMode());
			} else if (jsonObject instanceof JSONArray) {
				JSONAssert.assertEquals(
						JSONArray.class.cast(jsonObject),
						JSONArray.class.cast(convert(assertData.getActual())),
						assertData.getJsonCompareMode());
			}
		} catch (JSONException ex) {
			throw new TestException(ex.getMessage(), ex);
		}
	}

	private Object convert(Object target) throws JSONException {
		if (target instanceof Map) {
			return new JSONObject(Map.class.cast(target));
		} else if (target instanceof List) {
			return new JSONArray(List.class.cast(target));
		} else {
			return new JSONObject(String.valueOf(target));
		}
	}

	private ProcessingReport validationSchema(Object actual, Object schema) {
		try {
			return JsonSchemaFactory.byDefault()
					.getJsonSchema(objectMapper.valueToTree(schema))
					.validate(objectMapper.valueToTree(actual), true);
		} catch (ProcessingException ex) {
			throw new TestException(ex.getMessage(), ex);
		}
	}

	/**
	 * Remove ignore fields from json body.
	 *
	 * @param ignoredFields JsonPath
	 *
	 * @return Object
	 */
	private Object removeIgnoredFields(Object responseBody, List<String> ignoredFields) {
		if (ignoredFields.isEmpty() || Objects.isNull(responseBody)) {
			return responseBody;
		}
		DocumentContext jsonContext = JsonPath.parse(responseBody);
		try {
			ignoredFields.forEach(ignoredField -> jsonContext.delete(ignoredField));
		} catch (InvalidPathException ex) {
			throw new TestException(ex.getMessage(), ex);
		}
		return jsonContext.json();
	}

	/**
	 * Determine JSONCompareMode based on assertOption.
	 *
	 * @param assertOption map of assert option
	 *
	 * @return JSONCompareMode
	 */
	private JSONCompareMode getJsonCompareMode(Map<String, Object> assertOption) {
		if (Objects.nonNull(assertOption) && Objects.nonNull(assertOption.get("order")) && !(boolean) assertOption.get("order")) {
			return JSONCompareMode.NON_EXTENSIBLE;
		}
		return JSONCompareMode.STRICT;
	}
}
