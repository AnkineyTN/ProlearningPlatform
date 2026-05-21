package com.cabybara.prolearningplatform.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RestHttpClientUtil {
    private final RestTemplate restTemplate;

    public RestHttpClientUtil(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    private HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public <T, R> R post(String url, T body, Class<R> responseType) {
        HttpEntity<T> entity = new HttpEntity<>(body, buildJsonHeaders());
        log.info("POST {} | body: {}", url, body);

        ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
        log.info("✅ Response [{}]: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    public <R> R postMultipart(String url, MultiValueMap<String, Object> body, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        log.info("POST MULTIPART {} | body: {}", url, body);

        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                responseType
        );

        log.info("✅ Response [{}]: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    public <R> R get(String url, Class<R> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(buildJsonHeaders());
        log.info("GET {}", url);

        ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        log.info("✅ Response [{}]: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    public <T, R> R put(String url, T body, Class<R> responseType) {
        HttpEntity<T> entity = new HttpEntity<>(body, buildJsonHeaders());
        log.info("PUT {} | body: {}", url, body);

        ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
        log.info("Response [{}]: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    public <T, R> R post(String url, T body, HttpHeaders extraHeaders, Class<R> responseType) {
        HttpHeaders headers = buildJsonHeaders();
        headers.addAll(extraHeaders);
        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        log.info("POST {} | body: {}", url, body);

        ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
        log.info("Response [{}]: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    public <R> R get(String url, HttpHeaders extraHeaders, Class<R> responseType) {
        HttpHeaders headers = buildJsonHeaders();
        headers.addAll(extraHeaders);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        log.info("GET {}", url);

        ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        log.info("Response [{}]: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    public void delete(String url) {
        HttpEntity<Void> entity = new HttpEntity<>(buildJsonHeaders());
        log.info("DELETE {}", url);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        log.info("✅ DELETE {} completed", url);
    }
}
