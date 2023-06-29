package spring.integration.core.support.injector;

import groovy.util.GroovyScriptEngine;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import spring.integration.core.support.TestException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Class for dynamic value injection.
 */
class DynamicValueInjector extends ValueInjector {

    /**
     * Default string for surrounding for script part to support dynamic value in test.
     * Use to detect script part to inject dynamic value.
     */
    private static final String SCRIPT_SURROUNDING_PATTERN = "```(.+)?```";

    private final ScriptEngine scriptEngine;

    /**
     * Matching pattern for detecting script part.
     */
    private final Pattern pattern = Pattern.compile(SCRIPT_SURROUNDING_PATTERN);

    /**
     * Create new instance of DynamicValueInjector.
     *
     * @param scriptString Groovy script string which is used in JSON test case file.
     */
    protected DynamicValueInjector(String scriptString) {

        try {

            ScriptEngineManager factory = new ScriptEngineManager();
            scriptEngine = factory.getEngineByName("groovy");
            scriptEngine.eval(scriptString);
        } catch (Throwable ex) {
            throw new TestException("Failed to load Groovy script.", ex);
        }
    }

    /**
     * Scan map to inject value.
     */
    @Override
    protected void scanMapToInject(Map<String, Object> map) {
        map.forEach((String key, Object value) -> {
            // It might contain syntax for dynamic value when it's instance of String.
            if (value instanceof String) {
                map.replace(key, createInjectValue(key, String.valueOf(value)));
            } else {
                inject(value);
            }
        });
    }

    /**
     * Scan list to inject value.
     */
    @Override
    protected void scanListToInject(List<Object> list) {
        IntStream.range(0, list.size()).forEach(i -> {
            // It might contain syntax for dynamic value when it's instance of String.
            if (list.get(i) instanceof String) {
                list.set(i, createInjectValue(i, String.valueOf(list.get(i))));
            } else {
                inject(list.get(i));
            }
        });
    }

    /**
     * Create injection value with embedded groovy script.
     */
    private Object createInjectValue(Object key, String value) {
        Object ret = value;
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            try {
                ret = scriptEngine.eval(matcher.group(1));
            } catch (Throwable ex) {
                throw new TestException(String.format("Failed to create dynamic value with %s : %s", key, value), ex);
            }
        }
        return ret;
    }
}
