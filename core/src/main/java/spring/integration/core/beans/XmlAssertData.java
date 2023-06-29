package spring.integration.core.beans;

import lombok.Getter;
import org.skyscreamer.jsonassert.JSONCompareMode;
import spring.integration.core.enums.XmlCompareMode;

@Getter
public class XmlAssertData extends AssertData {

	private XmlCompareMode xmlCompareMode;

	/**
	 * constructor of xml AssertData.
	 *
	 * @Param jsonCompareMode is used for asserting header part
	 * @Param xmlCompareMode is used for asserting body part
	 */
	public XmlAssertData(Object actual, Object expected, boolean isRegex,
			JSONCompareMode jsonCompareMode, XmlCompareMode xmlCompareMode) {
		super(actual, expected, isRegex, jsonCompareMode);
		this.xmlCompareMode = xmlCompareMode;
	}
}
