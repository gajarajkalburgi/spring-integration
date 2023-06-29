package spring.integration.core;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import spring.integration.core.support.JsonSchemaValidator;
import spring.integration.core.support.ResponseHistoryService;
import spring.integration.core.support.assertion.AssertionSupporter;
import spring.integration.core.support.assertion.AssertionSupporterFactory;
import spring.integration.core.support.injector.ValueInjectionManager;
import spring.integration.core.support.mock.MockSupporter;
import spring.integration.core.utils.ObjectConverter;

@Slf4j
@RequiredArgsConstructor
class IntegrationExecutor {

	private static final String ANSI_RESET = "\u001B[0m";

	private static final String ANSI_GREEN = "\u001B[32m";

	private static final String ANSI_BLUE = "\u001B[34m";

	private static final String ANSI_RED = "\u001B[31m";

	private final ObjectMapper objectMapper;

	private final MockSupporter mockSupporter;

	private final boolean mockableAssertionMandatory;

	private final ResponseHistoryService responseHistoryService;

	/**
	 * Test Execution.
	 *
	 * @param assertionSupporterFactory Factory of instance for assertion.
	 * @param file                      Test specification file.
	 */
	void execute(AssertionSupporterFactory assertionSupporterFactory, File file) throws Exception {
		log.info("(Integration) Starting test : {}", file.getPath());
		JsonSchemaValidator.validate(objectMapper, file);
		Map<String, Object> injectedSpecificationData =
				ValueInjectionManager.inject(objectMapper, ObjectConverter.toMap(objectMapper, file));
		Map<String, Object> setup = ObjectConverter.toMap(objectMapper, injectedSpecificationData.get("setup"));
		try {
			mockSupporter.setup(setup);
			assertionSupporterFactory.create(injectedSpecificationData).forEach(AssertionSupporter::executeAssert);
			if (mockableAssertionMandatory) {
				mockSupporter.executeMockableAssertion(setup);
			}
		} finally {
			mockSupporter.cleanUp(setup, ObjectConverter.toMap(objectMapper, injectedSpecificationData.get("cleanup")));
			responseHistoryService.clear();
			log.info("(Integration) Finished test : {}", file.getPath());
		}
	}

	void execute(AssertionSupporterFactory assertionSupporterFactory, int port) {
		// To reflect test resources in interactive mode
		new Thread(this::watchTestResources).start();
		try (ServerSocket listener = new ServerSocket()) {
			listener.setReuseAddress(true);
			listener.bind(new InetSocketAddress(port));
			while (true) {
				try (Socket socket = listener.accept()) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String input = reader.readLine();
					if ("quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
						return;
					}
					if (StringUtils.hasLength(input)) {
						File testSpec = new File(input);
						System.out.println(ANSI_BLUE + "\nExecute Integration with " + testSpec.getName() + ANSI_RESET);
						execute(assertionSupporterFactory, testSpec);
						System.out.println(ANSI_GREEN + testSpec.getName() + " is successful.");
					}
				} catch (Throwable th) {
					System.err.println(ANSI_RED + "Test has been failed." + ANSI_RESET);
					th.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void watchTestResources() {
		String resourcePath = "src" + File.separator + "test" + File.separator + "resources" + File.separator;
		String destinationPath = "target" + File.separator + "test-classes" + File.separator;
		WatchService watcher;
		WatchKey watchKey;
		try {
			watcher = FileSystems.getDefault().newWatchService();
			Watchable path = Paths.get(resourcePath);
			path.register(watcher, ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			watchKey = watcher.take();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		while (true) {
			watchKey.pollEvents().forEach(watchEvent -> {
				if (StandardWatchEventKinds.ENTRY_CREATE.equals(watchEvent.kind())
					|| StandardWatchEventKinds.ENTRY_MODIFY.equals(watchEvent.kind())) {
					Object context = watchEvent.context();
					try {
						FileUtils.copyDirectory(new File(resourcePath + context), new File(destinationPath + context));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			if (!watchKey.reset()) {
				return;
			}
		}
	}
}
