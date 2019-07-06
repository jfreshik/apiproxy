package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@RestController
@Validated
public class ApiProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/api")
    public ResponseEntity apiProxy(@RequestBody(required = false) String body,
                                   @RequestHeader HttpHeaders headers,
                                   @RequestParam @NotEmpty String target,
                                   HttpMethod method,
                                   HttpServletRequest request) throws URISyntaxException, MalformedURLException {

        URI uri = buildUri(request, target);
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        return proxyRequest(method, uri, httpEntity);
    }

    private URI buildUri(HttpServletRequest request, String target) throws MalformedURLException, URISyntaxException {
        String queryString = request.getQueryString();
        int urlStartIndex = queryString.indexOf(target);
        URL targetUrl = new URL(queryString.substring(urlStartIndex));
        return new URI(targetUrl.getProtocol(), null, targetUrl.getHost(), targetUrl.getPort(),
                targetUrl.getPath(), targetUrl.getQuery(), null);
    }

    private ResponseEntity proxyRequest(HttpMethod method, URI uri, HttpEntity<String> httpEntity) {
        try{
            return restTemplate.exchange(uri, method, httpEntity, String.class);
        } catch(HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }
}
