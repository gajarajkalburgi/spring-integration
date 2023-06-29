package spring.integration.core.support;

import java.util.HashMap;
import java.util.Objects;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.test.util.TestSocketUtils;

public class AvailablePortPropertySource extends PropertySource {

	private static final String PROPERTY_SOURCE = "PortPropertySource";
	private static final String PROPERTY_PREFIX = "port.";
	private static final HashMap<String, Integer> PROPERTIES = new HashMap<>();

	public AvailablePortPropertySource() {
		super(PROPERTY_SOURCE);
	}

	@Override
	public Object getProperty(String s) {
		if (s.startsWith(PROPERTY_PREFIX)) {
			Integer port = PROPERTIES.get(s);
			if (Objects.isNull(port)) {
				port = TestSocketUtils.findAvailableTcpPort();
				PROPERTIES.put(s, port);
			}
			return port;
		}
		return null;
	}
}
