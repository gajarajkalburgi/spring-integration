package spring.integration.core.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.skyscreamer.jsonassert.JSONCompareMode;

@AllArgsConstructor
@Getter
public class AssertData {

	protected Object actual;

	protected Object expected;

	protected boolean isRegex;

	protected JSONCompareMode jsonCompareMode;

	/**
	 * Constructor with actual and expected.
	 */
	public AssertData(Object actual, Object expected) {
		this(actual, expected, false);
	}

	/**
	 * Constructor with actual, expected and isRegex.
	 */
	public AssertData(Object actual, Object expected, boolean isRegex) {
		this.actual = actual;
		this.expected = expected;
		this.isRegex = isRegex;
		jsonCompareMode = null;
	}
}
