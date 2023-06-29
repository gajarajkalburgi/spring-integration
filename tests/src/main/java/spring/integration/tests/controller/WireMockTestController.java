package spring.integration.tests.controller;

import java.net.URISyntaxException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WireMockTestController {

	private final RestTemplate restTemplate;

	@Value("${integration.wiremock.port:5000}")
	private int externalServerPort;

	/**
	 * Get from external for CoreTest.
	 * This sends request to external web API which constructed with wiremock.
	 */
	@RequestMapping(value = "/coreTestsWithExternal/{id}", method = RequestMethod.GET)
	public String getFromExternal(@PathVariable String id, HttpServletResponse response) throws Exception {
		try {
			return restTemplate.getForEntity(new URIBuilder(String.format("http://localhost:%d/external/", externalServerPort) + id).build(), String.class).getBody();
		} catch (HttpStatusCodeException ex) {
			response.setStatus(ex.getStatusCode().value());
			return ex.getResponseBodyAsString();
		}
	}

	/**
	 * Get xml from external for CoreTest.
	 * This sends request to external web API which constructed with wiremock.
	 */
	@RequestMapping(value = "/getXmlFromExternal/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getXmlFromExternal(@PathVariable String id) throws Exception {
		try {
			return restTemplate.getForEntity(new URIBuilder(String.format("http://localhost:%d/xmlFromExternal/", externalServerPort) + id).build(), String.class);
		} catch (HttpStatusCodeException ex) {
			return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getResponseHeaders(), ex.getStatusCode());
		}
	}

	/**
	 * Get from external for CoreTest.
	 * This sends request to external web API which constructed with wiremock.
	 */
	@RequestMapping(value = "/wiremockWithQuery", method = RequestMethod.GET)
	public String wiremockWithQuery() throws Exception {
		return restTemplate.getForEntity(new URIBuilder(String.format("http://localhost:%d/external/", externalServerPort) + "findBy?year=2017&month=10&day=7&time=19:00").build(), String.class).getBody();
	}

	/**
	 * Get from external for CoreTest.
	 * This sends request to external web API which constructed with wiremock.
	 */
	@RequestMapping(value = "/wiremockWithRegex", method = RequestMethod.GET)
	public String wiremockWithRegex(@RequestParam String key, @RequestParam String timestamp) throws Exception {
		String uri = "/maps/api/timezone/json?key=" + key + "&location=35.01412100%2C135.67659200&timestamp=" + timestamp;
		return restTemplate.getForEntity(new URIBuilder(String.format("http://localhost:%d", externalServerPort) + uri).build(), String.class).getBody();
	}

	@RequestMapping(value = "/accounting/availability", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity getAccountingAvailability(@RequestHeader("auth-provider") String providerId,
													@RequestHeader("auth-authenticated") boolean authenticated) {
		String url = String.format("http://localhost:%d/accounting-api/external/availability", externalServerPort);
		return requestToAccounting(providerId, authenticated, url, List.class);
	}

	@RequestMapping(value = "/accounting/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity searchAccounting(@RequestHeader("auth-provider") String providerId,
										   @RequestHeader("auth-authenticated") boolean authenticated,
										   @RequestParam String yearMonth) {
		String url = String.format("http://localhost:%d/accounting-api/external/search?yearMonth=" + yearMonth, externalServerPort);
		return requestToAccounting(providerId, authenticated, url, List.class);
	}

	@RequestMapping(value = "/accounting/csv/{id}", method = RequestMethod.GET, produces = "text/csv")
	public ResponseEntity getAccountingCsv(@RequestHeader("auth-provider") String providerId,
										   @RequestHeader("auth-authenticated") boolean authenticated,
										   @PathVariable String id) {
		String url = String.format("http://localhost:%d/accounting-api/external/csv/" + id, externalServerPort);
		return requestToAccounting(providerId, authenticated, url, String.class);
	}


	private ResponseEntity requestToAccounting(String providerId, boolean authenticated, String url, Class clazz) {
		try {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("auth-provider", providerId);
			requestHeaders.set("auth-authenticated", Boolean.toString(authenticated));
			HttpEntity<Void> requestEntity = new HttpEntity<>(requestHeaders);
			return restTemplate.exchange(new URIBuilder(url).build(), HttpMethod.GET, requestEntity, clazz);
		} catch (HttpStatusCodeException ex) {
			return new ResponseEntity(ex.getStatusCode());
		} catch (URISyntaxException ex) {
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
