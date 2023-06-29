package spring.integration.core.utils;

import static org.apache.commons.codec.CharEncoding.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spring.integration.core.support.TestException;

/**
 * Utility class for creating uri.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UriUtility {

	private static final String PATH_SEPARATOR = "/";

	/**
	 * Create URI string with path parameters.
	 */
	public static String createUriString(String baseUri, List<String> pathList) {
		if (!baseUri.endsWith(PATH_SEPARATOR)) {
			baseUri += PATH_SEPARATOR;
		}
		return baseUri + pathList.stream().map(path -> {
			try {
				return URLEncoder.encode(path, UTF_8);
			} catch (UnsupportedEncodingException ex) {
				throw new TestException(ex.getMessage(), ex);
			}
		}).collect(Collectors.joining(PATH_SEPARATOR));
	}

	/**
	 * Create new URI with replacing path parameters.
	 */
	public static URI createUriWithReplacingPath(URI originalUri, List<String> pathList) {
		try {
			return new URI(originalUri.getScheme(),
					originalUri.getUserInfo(),
					originalUri.getHost(),
					originalUri.getPort(),
					createUriString(PATH_SEPARATOR, pathList),
					originalUri.getQuery(),
					originalUri.getFragment()
			);
		} catch (URISyntaxException ex) {
			throw new TestException(ex.getMessage(), ex);
		}
	}

}
