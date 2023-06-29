package spring.integration.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for object converting.
 *
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectConverter {

	/**
	 * Tries to convert parameter obj to a List.
	 */
	public static <T> List<T> toList(ObjectMapper mapper, Object obj) {
		if (Objects.isNull(obj)) {
			return Collections.emptyList();
		}
		return mapper.convertValue(obj, new TypeReference<List<T>>() {});
	}

	/**
	 * Tries to convert parameter obj to a List of String.
	 */
	public static List<String> toStringList(ObjectMapper mapper, Object obj) {
		return toList(mapper, obj).stream().map(String.class::cast).collect(Collectors.toList());
	}

	/**
	 * Tries to convert parameter obj to a Map of Object with String keys.
	 */
	public static <T> Map<String, T> toMap(ObjectMapper mapper, Object obj) {
		if (Objects.isNull(obj)) {
			return Collections.emptyMap();
		}
		return mapper.convertValue(obj, new TypeReference<LinkedHashMap<String, T>>() {});
	}

	/**
	 * Tries to read file to a Map of Object with String keys.
	 */
	public static <T> Map<String, T> toMap(ObjectMapper mapper, File file) throws IOException {
		if (Objects.isNull(file)) {
			return Collections.emptyMap();
		}
		return mapper.readValue(file, new TypeReference<LinkedHashMap<String, T>>() {});
	}
}
