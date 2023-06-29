package spring.integration.core.utils;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import spring.integration.core.support.TestException;

/**
 * Utility class for resource access.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtility {

	/**
	 * For caching contents of file.
	 */
	private static final Map<String, String> CACHE = new Hashtable<>();

	/**
	 * Read resource as string.
	 *
	 * @param resourcePath Resource Path.
	 *
	 * @return returns resource contents.
	 */
	public static String getResourceAsString(String resourcePath) {
		if (CACHE.containsKey(resourcePath)) {
			return CACHE.get(resourcePath);
		}
		String contents;
		try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				fail(String.format("'%s' is not found. Please make sure to exist it.", resourcePath));
			}
			contents = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		} catch (IOException ex) {
			throw new TestException("Exception while reading file.", ex);
		}
		// Utilize cache when it has NOT interactive mode.
		if (StringUtils.isEmpty(System.getProperty("integration.interactive.port"))) {
			CACHE.put(resourcePath, contents);
		}
		return contents;
	}

	/**
	 * List files from directory recursively.
	 *
	 * @param filePath Base path.
	 * @param filter   filter for files.
	 *
	 * @return List.
	 */
	public static List<File> getFilesRecursively(String filePath, String filter) {
		List<File> files = new ArrayList<>();
		if (!StringUtils.isEmpty(filePath)) {
			Arrays.asList(new File(filePath).listFiles()).forEach(file -> {
				if (file.isDirectory()) {
					files.addAll(getFilesRecursively(file.getPath(), filter));
				} else if (file.getName().matches(filter)) {
					files.add(file);
				}
			});
		}
		return files;
	}
}
