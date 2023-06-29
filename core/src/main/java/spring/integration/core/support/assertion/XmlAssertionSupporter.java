package spring.integration.core.support.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;
import spring.integration.core.beans.AssertData;
import spring.integration.core.beans.XmlAssertData;
import spring.integration.core.enums.BodyAssertion;
import spring.integration.core.enums.XmlCompareMode;
import spring.integration.core.support.TestException;

class XmlAssertionSupporter extends AssertionSupporter {

	XmlAssertionSupporter(ObjectMapper objectMapper) {
		super(objectMapper);
		this.responseType = String.class;
	}

	@Override
	protected AssertData body(Object actualBody, Object expectedBody, Map<String, Object> assertOptions) {
		XmlAssertData assertData = null;
		switch (BodyAssertion.fromAssert(assertOptions)) {
			case IGNORE:
				break;
			case SCHEMA:
				boolean success = validationSchema(expectedBody.toString(), actualBody.toString());
				if (!success) {
					assertData = new XmlAssertData(null, "xml schemas should be valid", false, JSONCompareMode.STRICT, null);
				}
				break;
			case EQUALS:
				assertData = new XmlAssertData(actualBody, expectedBody, false, null, XmlCompareMode.fromAssert(assertOptions));
				break;
			default:
		}
		return assertData;
	}

	@Override
	public void executeAssert() {
		assertList.forEach(assertData -> {
			String actual = assertData.getActual() == null ? null : assertData.getActual().toString();
			String expected = assertData.getExpected() == null ? null : assertData.getExpected().toString();
			try {
				// JSONCompareMode.STRICT is for header part only when asserting xml
				if (JSONCompareMode.STRICT.equals(assertData.getJsonCompareMode())) {
					if (assertData.isRegex()) {
						assertWithRegex(assertData);
					} else {
						assertEquals(expected, actual);
					}
				} else {
					// XMLUnit cannot deal Null.
					if (expected == null || actual == null) {
						assertEquals(expected, actual);
						return;
					}
					boolean orderIgnored = XmlCompareMode.SIMILAR.equals(XmlAssertData.class.cast(assertData).getXmlCompareMode());
					DetailedDiff diff = compare(orderIgnored, actual, expected);
					if (orderIgnored) {
						if (!diff.similar()) {
							fail(createMessageFromDiff(diff));
						}
					} else {
						if (!diff.identical()) {
							fail(createMessageFromDiff(diff));
						}
					}
				}
			} catch (SAXException | IOException ex) {
				throw new TestException(ex.getMessage(), ex);
			}
		});
	}

	private DetailedDiff compare(boolean orderIgnored, String actual, String expected) throws IOException, SAXException {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreAttributeOrder(orderIgnored);
		return new DetailedDiff(XMLUnit.compareXML(expected, actual));
	}

	private boolean validationSchema(String xsdContent, String actualContent) {
		Validator validator = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
		validator.setSchemaSource(Input.fromString(xsdContent).build());
		ValidationResult result = validator.validateInstance(Input.fromString(actualContent).build());
		return result.isValid();
	}

	private String createMessageFromDiff(DetailedDiff diff) {
		return diff.getAllDifferences()
				.stream()
				.map(Difference::toString)
				.collect(Collectors.joining(System.lineSeparator()));
	}

}
