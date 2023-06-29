package spring.integration.core.support.injector;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import spring.integration.core.utils.ObjectConverter;

/**
 * Class for referenced value injection.
 */
@Slf4j
class ReferenceValueInjector extends ValueInjector {

	/**
	 * Key of Reference target.
	 */
	static final String REFERENCE_TARGET_KEY = "$ref";

	/**
	 * Key separator, because key string for nested objects is split by '/' like 'foo/bar/1'.
	 */
	static final String KEY_SEPARATOR = "/";

	/**
	 * Matching pattern for key string of reference value from itself.
	 * The first matching part will be used as key string.
	 */
	private static final Pattern REFERENCE_ITSELF_MATCH_PATTERN = Pattern.compile("^#/(.+)$");

	/**
	 * To convert to object.
	 */
	protected ObjectMapper objectMapper;

	/**
	 * Map for properties.
	 */
	private final Map definedProperties;

	/**
	 * Create new instance of ReferenceValueInjector.
	 *
	 * @param objectMapper      To convert to object from JSON file.
	 * @param definedProperties To store and get referenced values.
	 */
	protected ReferenceValueInjector(ObjectMapper objectMapper, Map definedProperties) {
		this.objectMapper = objectMapper;
		this.definedProperties = definedProperties;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void scanMapToInject(Map<String, Object> map) {
		map.forEach((String key, Object value) -> {
			Object injectValue = null;
			if (value instanceof Map) {
				injectValue = convertToInjectValue((Map) value);
			}
			if (injectValue != null) {
				map.replace(key, injectValue);
			}
			inject(map.get(key));
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void scanListToInject(List<Object> list) {
		IntStream.range(0, list.size()).forEach(i -> {
			Object injectValue = null;
			if (list.get(i) instanceof Map) {
				injectValue = convertToInjectValue((Map) list.get(i));
			}
			if (injectValue != null) {
				list.set(i, injectValue);
			}
			inject(list.get(i));
		});
	}

	/**
	 * Convert to inject value which has "$ref" as key.
	 */
	protected Object convertToInjectValue(Map map) {
		Object injectValue = null;
		if (map.containsKey(REFERENCE_TARGET_KEY)) {
			String replaceableString = (String) map.get(REFERENCE_TARGET_KEY);
			Matcher internal = REFERENCE_ITSELF_MATCH_PATTERN.matcher(replaceableString);
			if (internal.find() && !CollectionUtils.isEmpty(definedProperties)) {
				injectValue = findValueFromItself(internal.group(1));
			}
		}
		return copyFieldsFromOriginalMap(injectValue, map);
	}

	/**
	 * Find value from target object recursively by key list for each "$ref" values.
	 */
	Object find(Object target, List<String> keys) {
		// It should return target itself, because there is no way to find.
		if (keys.isEmpty()) {
			return target;
		}
		// Try to find the first key.
		Object ret = find(target, keys.get(0));
		if ((ret instanceof Map<?, ?>) || (ret instanceof List<?>)) {
			// Need to find value recursively by second or later keys when collection has been found.
			return find(ret, keys.subList(1, keys.size()));
		} else {
			// The value is found when value is not array and map.
			return ret;
		}
	}

	/**
	 * Find referenced value with key.
	 */
	private Object find(Object target, String key) {
		Object ret = null;
		if (target instanceof Map<?, ?>) {
			ret = ObjectConverter.toMap(objectMapper, target).get(key);
		} else if (target instanceof List<?>) {
			try {
				ret = ObjectConverter.toList(objectMapper, target).get(Integer.valueOf(key));
			} catch (NumberFormatException ignore) {
				// Ignore NumberFormatException, because it doesn't expect list when this makes.
				log.warn("List is expected but key is not digit. key : " + key);
			}
		}
		return ret;
	}

	/**
	 * Find value from the test case file itself.
	 */
	private Object findValueFromItself(String keyString) {
		// Make key list from key string, just like "abc/efg/.../xyz" => ["abc", "efg" ... "xyz"].
		List<String> keyList = Arrays.asList(keyString.split(KEY_SEPARATOR));
		return find(definedProperties.get(keyList.get(0)), keyList.subList(1, keyList.size()));
	}

	@SuppressWarnings("unchecked")
	Object copyFieldsFromOriginalMap(Object input, Map original) {
		if (input instanceof Map) {
			// Make new instance to avoid to change original.
			Map<String, Object> originalMap = new HashMap<>(original);
			Map<String, Object> newInjectMap = new HashMap<>((Map) input);
			originalMap.remove(REFERENCE_TARGET_KEY);
			newInjectMap.putAll(originalMap);
			return newInjectMap;
		}
		return input;
	}

}
