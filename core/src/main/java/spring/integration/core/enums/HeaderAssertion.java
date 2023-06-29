package spring.integration.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public enum HeaderAssertion {

	EQUALS,
	REGEX;

	/**
	 * Get HeaderAssertion from assert data.
	 *
	 * @param assertion Map object
	 *
	 * @return HeaderAssertion
	 */
	public static HeaderAssertion fromAssert(Map<String, Object> assertion) {
		if (Objects.nonNull(assertion)) {
			return Arrays.stream(HeaderAssertion.values())
					.filter(headerAssertion -> headerAssertion.name().equals(assertion.get("header")))
					.findFirst().orElse(EQUALS);
		}
		return EQUALS;
	}
}
