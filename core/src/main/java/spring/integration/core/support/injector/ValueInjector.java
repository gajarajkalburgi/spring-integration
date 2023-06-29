package spring.integration.core.support.injector;

import java.util.List;
import java.util.Map;

/**
 * Base class for value injection to test JSON.
 */
public abstract class ValueInjector {

	/**
	 * Required method for scanning map to inject value.
	 *
	 * @param map Injection target map. It will be updated when it contains inject target part.
	 */
	protected abstract void scanMapToInject(Map<String, Object> map);

	/**
	 * Required method for scanning list to inject value.
	 *
	 * @param list Injection target list. It will be updated when it contains inject target part.
	 */
	protected abstract void scanListToInject(List<Object> list);

	/**
	 * Inject values as you defined them in test case JSON file.
	 *
	 * @param object Injection target object. It will be updated when it contains inject target part.
	 */
	@SuppressWarnings("unchecked")
	protected ValueInjector inject(Object object) {
		if (object instanceof Map) {
			scanMapToInject((Map) object);
		} else if (object instanceof List) {
			scanListToInject((List) object);
		}
		return this;
	}

}
