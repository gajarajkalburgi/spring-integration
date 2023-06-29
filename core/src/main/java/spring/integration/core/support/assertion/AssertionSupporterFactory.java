package spring.integration.core.support.assertion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import spring.integration.core.support.IntegrationRestTemplate;
import spring.integration.core.support.ResponseHistoryService;
import spring.integration.core.support.TestException;
import spring.integration.core.utils.ObjectConverter;
import spring.integration.core.utils.UriUtility;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class AssertionSupporterFactory {

    private static final String REQUEST_URL = "http://localhost:%d%s%s";

    private final int port;

    private final String serverContextPath;

    private final ObjectMapper objectMapper;

    private final IntegrationRestTemplate integrationRestTemplate;

    private final ResponseHistoryService responseHistoryService;

    public AssertionSupporterFactory(int port, String serverContextPath, ObjectMapper objectMapper,
                                     IntegrationRestTemplate integrationRestTemplate, ResponseHistoryService responseHistoryService) {
        this.port = port;
        this.serverContextPath = serverContextPath;
        this.objectMapper = objectMapper;
        this.integrationRestTemplate = integrationRestTemplate;
        this.responseHistoryService = responseHistoryService;
    }

    public Stream<AssertionSupporter> create(Map<String, Object> specificationData) {
        Map<String, Object> metadataMap = ObjectConverter.toMap(objectMapper, specificationData.get("metadata"));
        if (specificationData.containsKey("scenario")) {
            List<Map<String, Object>> requestsAndResponses = ObjectConverter.toList(objectMapper, specificationData.get("scenario"));
            return requestsAndResponses
                    .stream()
                    .map(requestAndResponse -> create(metadataMap, requestAndResponse));
        } else {
            return Stream.of(create(metadataMap, specificationData));
        }
    }

    private AssertionSupporter create(Map<String, Object> metadataMap, Map<String, Object> requestAndResponseMap) {
        Map<String, Object> responseMap = ObjectConverter.toMap(objectMapper, requestAndResponseMap.get("response"));
        String contentType = getContentType(responseMap);
        AssertionSupporter assertionSupporter = createAssertionSupporter(contentType);
        Map<String, Object> requestMap = responseHistoryService.executeExchangeWithHistory(
                ObjectConverter.toMap(objectMapper, requestAndResponseMap.get("request")));
        ResponseEntity response = sendRequest(requestMap, assertionSupporter.getResponseType());
        Map<String, String> assertFieldMap = ObjectConverter.toMap(objectMapper, metadataMap.get("assert"));
        if (assertFieldMap.containsKey("swagger")) {
            executeSwaggerValidation(assertFieldMap.get("swagger"), requestMap, response);
        }
        assertionSupporter.setUpAssertList(metadataMap, response, responseMap);
        return assertionSupporter;
    }

    private void executeSwaggerValidation(String swaggerPath, Map<String, Object> requestMap, ResponseEntity response) {
        String swaggerSpecificationUrl = swaggerPath.matches("^/.*$")
                // This will be used in the case of project has swagger specification on itself or local.
                ? String.format(REQUEST_URL, port, serverContextPath, swaggerPath)
                : swaggerPath;
        Map<String, Map<String, Object>> requestMapForValidation = ObjectConverter.toMap(objectMapper, requestMap);
    }

    private ResponseEntity sendRequest(Map<String, Object> requestMap, Class responseType) {
        RequestEntity request = requestEntityBuilder(requestMap);
        log.debug("(Integration) Requesting with RequestEntity : {}", request);
        ResponseEntity response = integrationRestTemplate.exchange(objectMapper, request, responseType);
        responseHistoryService.addToResponseHistory(response);
        log.debug("(Integration) Responded with ResponseEntity : {}", response);
        return response;
    }

    private RequestEntity requestEntityBuilder(Map<String, Object> requestMap) {
        URI requestUri = createRequestUri(requestMap);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String, String> headerMap = objectMapper.convertValue(requestMap.get("headers"),
                new TypeReference<LinkedHashMap<String, String>>() {
                });
        if (!CollectionUtils.isEmpty(headerMap)) {
            headerMap.forEach(headers::add);
        }
        return new RequestEntity<>(
                createRequestBody(requestMap, headerMap),
                headers,
                HttpMethod.valueOf(String.valueOf(requestMap.get("method"))),
                requestUri);
    }

    private URI createRequestUri(Map<String, Object> requestMap) {
        URI ret;
        if (requestMap.containsKey("path")) {
            List<String> pathParams = ObjectConverter.toList(objectMapper, requestMap.get("path"));
            String path = UriUtility.createUriString(String.valueOf(requestMap.get("url")), pathParams);
            ret = URI.create(String.format(REQUEST_URL, port, serverContextPath, path));
        } else {
            ret = URI.create(String.format(REQUEST_URL, port, serverContextPath, requestMap.get("url")));
        }
        if (requestMap.containsKey("query")) {
            try {
                Map<String, Object> queryMap = ObjectConverter.toMap(objectMapper, requestMap.get("query"));
                URIBuilder uriBuilder = new URIBuilder(ret);
                queryMap.forEach((key, value) -> uriBuilder.addParameter(key, String.valueOf(value)));
                return uriBuilder.build();
            } catch (Exception ex) {
                throw new TestException(ex.getMessage(), ex);
            }
        }
        return ret;
    }

    private Object createRequestBody(Map<String, Object> requestMap, Map<String, String> requestHeadersMap) {
        if (requestMap.get("body") instanceof List) {
            return createRequestBodyAsList(requestMap, requestHeadersMap);
        }
        if (requestMap.get("body") instanceof Map) {
            return createRequestBodyAsMap(requestMap, requestHeadersMap);
        }
        return requestMap.get("body");
    }

    private Map<String, Object> createRequestBodyAsMap(Map<String, Object> requestMap, Map<String, String> requestHeadersMap) {
        Map<String, Object> requestBodyMap = ObjectConverter.toMap(objectMapper, requestMap.get("body"));
        if (!CollectionUtils.isEmpty(requestBodyMap) && !CollectionUtils.isEmpty(requestHeadersMap)
                && requestHeadersMap.containsValue(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            // noinspection unchecked
            return createRequestMapWithFileSystemResource(requestBodyMap);
        }
        return requestBodyMap;
    }

    private List<Map<String, Object>> createRequestBodyAsList(Map<String, Object> requestMap, Map<String, String> requestHeadersMap) {
        List<Map<String, Object>> requestBodyList = ObjectConverter.toList(objectMapper, requestMap.get("body"));
        if (!CollectionUtils.isEmpty(requestBodyList) && !CollectionUtils.isEmpty(requestHeadersMap)
                && requestHeadersMap.containsValue(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return requestBodyList.stream()
                    .map(this::createRequestMapWithFileSystemResource)
                    .collect(Collectors.toList());
        }
        return requestBodyList;
    }

    private Map<String, Object> createRequestMapWithFileSystemResource(Map<String, Object> requestBodyMap) {
        LinkedMultiValueMap<String, Object> linkedMultiValueMap = new LinkedMultiValueMap<>();
        requestBodyMap.forEach((key, value) -> {
            if (value instanceof String && ((String) value).startsWith(AssertionSupporter.START_CHARACTER_OF_FILE_PATH)) {
                String filePathString = ((String) value).replaceFirst(AssertionSupporter.START_CHARACTER_OF_FILE_PATH, "");
                URL filePath = ClassLoader.getSystemResource(filePathString);
                if (filePath != null) {
                    linkedMultiValueMap.add(key, new FileSystemResource(filePath.getPath()));
                } else {
                    fail(filePathString + " does not exist.");
                }
            } else {
                linkedMultiValueMap.add(key, value);
            }
        });
        // noinspection unchecked
        return Map.class.cast(linkedMultiValueMap);
    }

    private AssertionSupporter createAssertionSupporter(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return new JsonAssertionSupporter(objectMapper);
        }
        if (contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return new JsonAssertionSupporter(objectMapper);
        }
        if (contentType.contains(MediaType.APPLICATION_XML_VALUE)
                || contentType.contains(MediaType.APPLICATION_ATOM_XML_VALUE)
                || contentType.contains(MediaType.APPLICATION_RSS_XML_VALUE)
                || contentType.contains(MediaType.TEXT_XML_VALUE)) {
            return new XmlAssertionSupporter(objectMapper);
        }
        if (contentType.contains(MediaType.APPLICATION_XHTML_XML_VALUE)
                || contentType.contains("text/")) {
            return new TextAssertionSupporter(objectMapper);
        }
        if (contentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                || contentType.contains(MediaType.APPLICATION_PDF_VALUE)
                || contentType.contains("image/")
                || contentType.contains("video/")
                || contentType.contains("audio/")
                || contentType.contains("zip")
                || contentType.contains("protobuf")) {
            return new BinaryAssertionSupporter(objectMapper);
        }
        throw new TestException(String.format("%s : is not supported Content-Type.", contentType));
    }

    private String getContentType(Map<String, Object> responseMap) {
        if (responseMap.containsKey("headers")) {
            Map<String, Object> headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            headerMap.putAll(ObjectConverter.toMap(objectMapper, responseMap.get("headers")));
            if (headerMap.containsKey(HttpHeaders.CONTENT_TYPE)) {
                return String.valueOf(headerMap.get(HttpHeaders.CONTENT_TYPE)).toLowerCase();
            }
        }
        return null;
    }
}
