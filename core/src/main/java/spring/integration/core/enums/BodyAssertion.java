package spring.integration.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public enum BodyAssertion {

	EQUALS,
	SCHEMA,
	IGNORE,
	REGEX;

	/**
	 * Get BodyAssertion from MetaData.
	 *
	 * @param assertion Map object
	 *
	 * @return BodyAssertion
	 */
	public static BodyAssertion fromAssert(Map<String, Object> assertion) {
		if (Objects.nonNull(assertion)) {
			return Arrays.stream(BodyAssertion.values())
					.filter(bodyAssertion -> bodyAssertion.name().equals(assertion.get("body")))
					.findFirst().orElse(EQUALS);
		}
		return EQUALS;
	}
}
