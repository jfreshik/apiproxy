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
import org.springframework.web.util.HtmlUtils;

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

    @RequestMapping("/proxy")
    public ResponseEntity proxy(@RequestBody(required = false) String body,
                                   @RequestHeader HttpHeaders headers,
                                   @RequestParam @NotEmpty String target,
                                   HttpMethod method,
                                   HttpServletRequest request)
            throws URISyntaxException, MalformedURLException {

        URI uri = buildProxyUri(request, target);
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        return proxyRequest(method, uri, httpEntity);
    }

    private URI buildProxyUri(HttpServletRequest request, String target)
            throws MalformedURLException, URISyntaxException {
        String targetParamUrl = extractTargetUrlFromQueryString(request.getQueryString(), target);
        String unescapedUrl = HtmlUtils.htmlUnescape(targetParamUrl);
        return new URL(unescapedUrl).toURI();
    }

    /**
     * target 의 query param 이 2개 이상일 때 '&' 가 escape 없이 들어오면 뒤에 param 이 누락됨.
     * request 의 query string 에서 target 으로 시작하는 위치 부터 문자열 끝까지 target url 로 추출
     */
    private String extractTargetUrlFromQueryString(String queryString, String target) {
        int urlStartIndex = queryString.indexOf(target);
        return queryString.substring(urlStartIndex);
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
