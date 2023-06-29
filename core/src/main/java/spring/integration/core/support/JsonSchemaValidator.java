package spring.integration.core.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import java.io.File;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonSchemaValidator {

	private static final String SCHEMA_FILE_PATH = "/schema.json";

	private static final JsonSchema SCHEMA;

	static {
		try {
			SCHEMA = JsonSchemaFactory.byDefault().getJsonSchema(JsonLoader.fromResource(SCHEMA_FILE_PATH));
		} catch (Exception ex) {
			throw new TestException(ex.getMessage(), ex);
		}
	}

	/**
	 * Validate json file whether it matches Test Specification JSON schema.
	 * It throws TestException when specification file has violation of JSON schema.
	 *
	 * @param objectMapper ObjectMapper instance which contains JsonParser.Feature.ALLOW_COMMENTS
	 * @param specificationFile Test specification file
	 */
	public static void validate(ObjectMapper objectMapper, File specificationFile) {
		try {
			ProcessingReport report = SCHEMA.validate(objectMapper.readTree(specificationFile));
			if (!report.isSuccess()) {
				throw new TestException(String.format("%s contains violation of json schema. %s", specificationFile.getName(), report.toString()));
			}
		} catch (IOException | ProcessingException ex) {
			throw new TestException(ex.getMessage(), ex);
		}

	}

}
