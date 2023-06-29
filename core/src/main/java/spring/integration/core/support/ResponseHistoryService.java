package spring.integration.core.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import spring.integration.core.utils.ObjectConverter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseHistoryService {

	private final ObjectMapper integrationObjectMapper;

	private List<Map<String, Object>> responseHistory = Collections.synchronizedList(new ArrayList<>());

	private static final Pattern REFERENCE_PATTERN = Pattern.compile("%\\{(.+)}");

	/**
	 * Add response entity to response history.
	 */
	public ResponseHistoryService addToResponseHistory(ResponseEntity responseEntity) {
		// Convert responseEntity to Map.
		// Because ResponseEntity isn't able to be converted to DocumentContext directly.
		responseHistory.add(ObjectConverter.toMap(integrationObjectMapper, responseEntity));
		return this;
	}

	/**
	 * Clear response history.
	 */
	public ResponseHistoryService clear() {
		responseHistory.clear();
		return this;
	}

	/**
	 * Exchange request map with response history.
	 */
	public Map<String, Object> executeExchangeWithHistory(Map<String, Object> requestMap) {
		if (!responseHistory.isEmpty() && containReferenceSyntax(requestMap)) {
			DocumentContext documentContext = JsonPath.parse(responseHistory);
			replace(requestMap, documentContext);
		}
		return requestMap;
	}

	private boolean containReferenceSyntax(Object object) {
		try {
			return REFERENCE_PATTERN.matcher(integrationObjectMapper.writeValueAsString(object)).find();
		} catch (JsonProcessingException ex) {
			log.info(ex.getMessage());
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private void replace(Map<String, Object> map, DocumentContext documentContext) {
		map.forEach((String key, Object value) -> {
			// It might contain syntax for dynamic value when it's instance of String.
			if (value instanceof String) {
				Object valueFromHistory = getValueWithJsonPath((String) value, documentContext);
				if (valueFromHistory != null) {
					map.replace(key, valueFromHistory);
				}
			} else if (value instanceof Map) {
				replace((Map<String, Object>) value, documentContext);
			} else if (value instanceof List) {
				replace((List<Object>) value, documentContext);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void replace(List<Object> list, DocumentContext documentContext) {
		IntStream.range(0, list.size()).forEach(i -> {
			// It might contain syntax for dynamic value when it's instance of String.
			if (list.get(i) instanceof String) {
				Object valueFromHistory = getValueWithJsonPath((String) list.get(i), documentContext);
				if (valueFromHistory != null) {
					list.set(i, valueFromHistory);
				}
			} else if (list.get(i) instanceof Map) {
				replace((Map<String, Object>) list.get(i), documentContext);
			} else if (list.get(i) instanceof List) {
				replace((List<Object>) list.get(i), documentContext);
			}
		});
	}

	private Object getValueWithJsonPath(String value, DocumentContext documentContext) {
		Object ret = null;
		Matcher matcher = REFERENCE_PATTERN.matcher(value);
		if (matcher.find()) {
			String jsonPath = matcher.group(1);
			ret = documentContext.read(jsonPath, Object.class);
		}
		return ret;
	}

}
