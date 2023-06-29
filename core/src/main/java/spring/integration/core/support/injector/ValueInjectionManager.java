package spring.integration.core.support.injector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import spring.integration.core.utils.ObjectConverter;
import spring.integration.core.utils.ResourceUtility;

/**
 * Initialize classes for value injection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ValueInjectionManager {

	/**
	 * Setup value injectors and execute injection with specificationData.
	 */
	public static Map<String, Object> inject(ObjectMapper objectMapper, Map<String, Object> specificationData) {
		Map<String, Object> injectTargetMap = ObjectConverter.toMap(objectMapper, specificationData);
		// noinspection unchecked
		Map<String, Object> metadata = (Map<String, Object>) injectTargetMap.get("metadata");
		Map<String, Object> userDefinedDynamicValues = ObjectConverter.toMap(objectMapper, metadata.get("properties"));

		FileContentsInjector fileContentsInjector = new FileContentsInjector(objectMapper);
		fileContentsInjector.inject(userDefinedDynamicValues)
				.inject(metadata.get("assert"))
				.inject(injectTargetMap.get("setup"))
				.inject(injectTargetMap.get("request"))
				.inject(injectTargetMap.get("response"))
				.inject(injectTargetMap.get("scenario"))
				.inject(injectTargetMap.get("cleanup"));

		String preDefinedDynamicValues = ResourceUtility.getResourceAsString("script/DynamicValue.groovy");
		DynamicValueInjector dynamicValueInjector = new DynamicValueInjector(preDefinedDynamicValues);
		dynamicValueInjector.inject(userDefinedDynamicValues)
				.inject(injectTargetMap.get("request"))
				.inject(injectTargetMap.get("scenario"));

		ReferenceValueInjector referencedValueInjector = new ReferenceValueInjector(objectMapper, userDefinedDynamicValues);
		referencedValueInjector.inject(metadata.get("assert"))
				.inject(injectTargetMap.get("setup"))
				.inject(injectTargetMap.get("request"))
				.inject(injectTargetMap.get("response"))
				.inject(injectTargetMap.get("scenario"))
				.inject(injectTargetMap.get("cleanup"));
		// Output injection result to log
		logInjectResult(objectMapper, specificationData, injectTargetMap);
		return injectTargetMap;
	}

	/**
	 * Log result of value injection by differences.
	 */
	private static void logInjectResult(ObjectMapper objectMapper, Map<String, Object> before, Map<String, Object> after) {
		if (log.isDebugEnabled()) {
			JsonNode beforeNode = objectMapper.valueToTree(before);
			JsonNode afterNode = objectMapper.valueToTree(after);
			JsonNode diffs = JsonDiff.asJson(beforeNode, afterNode);

			diffs.elements().forEachRemaining(diff -> {
				String path = diff.get("path").asText();
				String beforeValue = beforeNode.at(path).toString();
				JsonNode valueNode = diff.get("value");
				String afterValue = (valueNode != null)
						? valueNode.asText()
						: afterNode.at(path.replaceAll("/\\$ref", "")).toString();
				if (!StringUtils.isEmpty(beforeValue) && !StringUtils.isEmpty(afterValue)) {
					log.debug(String.format("%s : %s -> %s", path, beforeValue, afterValue));
				}
			});
		}
	}
}
