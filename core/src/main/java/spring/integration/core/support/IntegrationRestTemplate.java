package spring.integration.core.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class IntegrationRestTemplate extends RestTemplate {

    @Value("${integration.connect.connectTimeout:30000}")
    private int connectTimeout;

    public IntegrationRestTemplate() {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setSupportedMediaTypes(MediaType.parseMediaTypes(MediaType.APPLICATION_XML_VALUE));
        getMessageConverters().add(stringConverter);
    }

    public ResponseEntity exchange(ObjectMapper objectMapper, RequestEntity requestEntity, Class clazz) {
        try {

            return exchange(requestEntity, clazz);
        } catch (HttpStatusCodeException ex) {
            return getResponseEntity(objectMapper, ex, clazz);
        } catch (ResourceAccessException ex) {
            throw new TestException("Exception while calling api with " + requestEntity, ex);
        }
    }

    private ResponseEntity getResponseEntity(ObjectMapper objectMapper, HttpStatusCodeException ex, Class clazz) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            HttpHeaders headers = ex.getResponseHeaders();
            HttpStatusCode httpStatus = ex.getStatusCode();

            // Response body is empty
            if (StringUtils.isEmpty(responseBody)) {
                return new ResponseEntity(headers, httpStatus);
            }
            // Response body type is Object. Then return response body as json format.
            if (clazz == Object.class) {
                return new ResponseEntity<Object>(objectMapper.readValue(responseBody, clazz), headers, httpStatus);
            }
            // Response body type is NOT Object. Then return response body as String.
            return new ResponseEntity<>(responseBody, headers, httpStatus);

        } catch (IOException ioException) {
            throw new TestException("Exception while parsing response json.", ioException);
        }
    }
}
