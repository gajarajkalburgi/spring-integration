package spring.integration.core.enums;

import java.util.Map;
import java.util.Objects;

public enum XmlCompareMode {

	IDENTICAL,
	SIMILAR;

	/**
	 * Get XmlCompareMode from MetaData.
	 *
	 * @param assertion Map object
	 *
	 * @return XmlCompareMode
	 */
	public static XmlCompareMode fromAssert(Map<String, Object> assertion) {
		if (Objects.nonNull(assertion) && Objects.nonNull(assertion.get("order")) && !(boolean) assertion.get("order")) {
			return SIMILAR;
		}
		return IDENTICAL;
	}
}
