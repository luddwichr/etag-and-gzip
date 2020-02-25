package com.example.demo;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void largeVersionedFile() {
        String requestPath = "/versioned/some-large-file-" + getMd5HashOfResource("static/versioned/some-large-file.js") + ".js";

        ResponseEntity<String> response = fireRequest(requestPath);

        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING)).contains("gzip");
    }

    @Test
    void smallVersionedFile() {
        String requestPath = "/versioned/some-small-file-" + getMd5HashOfResource("static/versioned/some-small-file.js") + ".js";

        ResponseEntity<String> response = fireRequest(requestPath);

        assertThat(response.getHeaders()).doesNotContainKey(HttpHeaders.CONTENT_ENCODING);
    }

    @Test
    void largeUnversionedFile() {
        String requestPath = "/unversioned/some-large-file.js";

        ResponseEntity<String> response = fireRequest(requestPath);

        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING)).contains("gzip");

    }

    @Test
    void smallUnversionedFile() {
        String requestPath = "/unversioned/some-small-file.js";

        ResponseEntity<String> response = fireRequest(requestPath);

        assertThat(response.getHeaders()).doesNotContainKey(HttpHeaders.CONTENT_ENCODING);
    }

    private String getMd5HashOfResource(String resourcePath) {
        Resource resource = new DefaultResourceLoader().getResource(resourcePath);
        try {
            try (InputStream inputStream = resource.getInputStream()) {
                return DigestUtils.md5Hex(inputStream);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private ResponseEntity<String> fireRequest(String requestPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/javascript");
        String url = "http://localhost:" + port + requestPath;
        return testRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

}
