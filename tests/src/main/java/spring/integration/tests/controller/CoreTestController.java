package spring.integration.tests.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import spring.integration.tests.entity.Book;
import spring.integration.tests.entity.BookStore;
import spring.integration.tests.service.BookStoreService;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CoreTestController {

    private final BookStoreService bookStoreService;

    private Map<String, EchoResponse> echoResponseMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Get from service layer for CoreTest.
     */
    @RequestMapping(value = "/bookStore", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public BookStore getBookStore() {
        return bookStoreService.getBookStore();
    }

    /**
     * Get response Null.
     */
    @RequestMapping(value = "/null", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public BookStore getNull() {
        return null;
    }

    /**
     * Get book for CoreTest.
     */
    @RequestMapping(value = "/findBookById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Book getBook(@RequestParam Integer id, HttpServletResponse response) {
        Book ret = bookStoreService.getBookStore().getBooks().stream().filter(book -> id.equals(book.getId())).findFirst().orElse(null);
        if (ret == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
        return ret;
    }

    /**
     * Post book for CoreTest.
     */
    @RequestMapping(value = "/book/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void postBook(@RequestBody Book book, HttpServletResponse response) {
        int status = book != null ? HttpStatus.CREATED.value() : HttpStatus.NOT_FOUND.value();
        response.setStatus(status);
    }

    /**
     * Post books for CoreTest.
     */
    @RequestMapping(value = "/books/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<Book> postBooks(@RequestBody List<Book> books) {
        return books;
    }

    /**
     * Create xml message.
     */
    @RequestMapping(value = "/createXml", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createXml(@RequestBody String xml) {
        return new ResponseEntity<>(xml, HttpStatus.CREATED);
    }

    /**
     * Endpoint to test text and html data response.
     */
    @RequestMapping(value = "/text/{text}", method = RequestMethod.GET)
    public String text(@RequestHeader(HttpHeaders.ACCEPT) String accept, @PathVariable String text, HttpServletResponse response) {
        String ret;
        switch (accept) {
            case MediaType.TEXT_HTML_VALUE:
                ret = String.format("<html><body><div>Hello %s in Spring Boot Integration Test Framework.</div></body></html>", text);
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
                break;
            case MediaType.APPLICATION_XHTML_XML_VALUE:
                ret = String.format("<html><body><div>Hello %s in Spring Boot Integration Test Framework.</div></body></html>", text);
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XHTML_XML_VALUE);
                break;
            case MediaType.TEXT_PLAIN_VALUE:
            default:
                ret = String.format("Hello %s in Spring Boot Integration Test Framework.", text);
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                break;
        }
        return ret;
    }

    /**
     * Endpoint for multipart/form-data test.
     */
    @ResponseBody
    @RequestMapping(value = "/upload/", method = RequestMethod.POST)
    public List<String> uploadFile(MultipartHttpServletRequest request) throws Exception {
        List<String> ret = new ArrayList<>();
        for (Part part : request.getParts()) {
            log.info("------------------------------------");
            log.info("type : " + part.getContentType());
            log.info("name : " + part.getName());
            log.info("filename :" + part.getSubmittedFileName());
            if (part.getContentType().contains("json")
                    || part.getContentType().contains("xml")
                    || part.getContentType().contains("text")) {
                log.info("Contents :" + IOUtils.toString(part.getInputStream(), UTF_8));
            } else {
                log.info("Contents :" + part.getInputStream().toString());
            }
            ret.add(part.getName());
        }
        return ret;
    }

    /**
     * Endpoint to test binary data response.
     */
    @RequestMapping(value = "/media/", method = RequestMethod.GET)
    public ResponseEntity<Resource> media(@RequestHeader(HttpHeaders.ACCEPT) String accept) {
        String targetFilePath;
        switch (accept) {
            case MediaType.IMAGE_GIF_VALUE:
                targetFilePath = "media/running.gif";
                break;
            case MediaType.IMAGE_PNG_VALUE:
                targetFilePath = "media/spring.png";
                break;
            case MediaType.APPLICATION_PDF_VALUE:
                targetFilePath = "media/spoke.pdf";
                break;
            default:
                targetFilePath = null;
                break;
        }
        return targetFilePath == null
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(new InputStreamResource(ClassLoader.getSystemResourceAsStream(targetFilePath)), HttpStatus.OK);
    }

    @RequestMapping(value = "/echo/{message}/{note}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EchoResponse createEcho(@PathVariable String message, @PathVariable String note) {
        EchoResponse echoResponse = new EchoResponse(UUID.randomUUID().toString(), message, note);
        echoResponseMap.put(echoResponse.getId(), echoResponse);
        return echoResponse;
    }

    @RequestMapping(value = "/echo/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EchoResponse echo(@PathVariable String id) {
        return echoResponseMap.containsKey(id) ? echoResponseMap.get(id) : new EchoResponse(null, null, null);
    }

    @AllArgsConstructor
    @Data
    private static class EchoResponse {
        private String id;
        private String message;
        private String note;
    }

}
