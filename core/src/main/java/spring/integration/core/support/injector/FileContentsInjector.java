package spring.integration.core.support.injector;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import spring.integration.core.support.TestException;
import spring.integration.core.utils.ResourceUtility;

class FileContentsInjector extends ReferenceValueInjector {

	/**
	 * Matching pattern for key string of reference value from another file.
	 * The first matching part will be used as file path, the second matching part will be used as key string.
	 */
	private static final Pattern REFERENCE_ANOTHER_MATCH_PATTERN = Pattern.compile("^(.+)/#/(.*)$");
	/**
	 * To convert to object.
	 */
	protected ObjectMapper objectMapper;

	protected FileContentsInjector(ObjectMapper objectMapper) {
		super(objectMapper, null);
		this.objectMapper = objectMapper;
	}

	/**
	 * Scan map to inject value.
	 */
	@Override
	protected void scanMapToInject(Map<String, Object> map) {
		super.scanMapToInject(map);
		if (map.containsKey(REFERENCE_TARGET_KEY)) {
			Object object = findValueWithTargetPath(String.valueOf(map.get(REFERENCE_TARGET_KEY)));
			if (object instanceof Map) {
				map.remove(REFERENCE_TARGET_KEY);
				// noinspection unchecked
				map.putAll(Map.class.cast(object));
			}
		}
	}

	/**
	 * Convert to inject value which has "$ref" as key.
	 */
	@Override
	protected Object convertToInjectValue(Map map) {
		Object injectValue = null;
		if (map.containsKey(REFERENCE_TARGET_KEY)) {
			injectValue = findValueWithTargetPath(String.valueOf(map.get(REFERENCE_TARGET_KEY)));
		}
		return copyFieldsFromOriginalMap(injectValue, map);
	}

	private Object findValueWithTargetPath(String targetPath) {
		Object injectValue = null;
		Matcher external = REFERENCE_ANOTHER_MATCH_PATTERN.matcher(targetPath);
		if (external.find()) {
			injectValue = findValueFromAnotherFile(external.group(1), external.group(2));
		}
		return injectValue;
	}

	private Object findValueFromAnotherFile(String filePath, String keyString) {
		String contentsFromFile = ResourceUtility.getResourceAsString(filePath);
		try {
			String ext = FilenameUtils.getExtension(filePath);
			if (ext.equals("xml") || ext.equals("xsd")) {
				return contentsFromFile;
			} else {
				return find(objectMapper.readValue(contentsFromFile, Object.class), createKeys(keyString));
			}
		} catch (IOException ie) {
			throw new TestException("Fail to read value : " + contentsFromFile, ie);
		}
	}

	private List<String> createKeys(String keyString) {
		return StringUtils.isEmpty(keyString) ? Collections.emptyList() : Arrays.asList(keyString.split(KEY_SEPARATOR));
	}

}
