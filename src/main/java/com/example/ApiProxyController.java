package com.example;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

@RestController
@Validated
public class ApiProxyController {

    @Autowired
    private RestTemplate restTemplate;
    
    private final String DEV_WAS_IP = "10.7.138.26";
    private final int DEV_WAS_PORT = 8088;
    
    @RequestMapping("/**")
    public ResponseEntity proxyDev(@RequestBody(required = false) String body,
                                   @RequestHeader HttpHeaders headers,
                                   HttpMethod method,
                                   HttpServletRequest request) throws URISyntaxException {
    	
    	HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
    	
    	String queryString = HtmlUtils.htmlUnescape(request.getQueryString());
    	URI uri = new URI("http", null, DEV_WAS_IP, DEV_WAS_PORT, 
    			request.getRequestURI(), queryString, null);

    	return proxyRequest(method, uri, httpEntity);
    }

//    @RequestMapping("/proxy")
//    public ResponseEntity proxy(@RequestBody(required = false) String body,
//                                   @RequestHeader HttpHeaders headers,
//                                   @RequestParam @NotEmpty String target,
//                                   HttpMethod method,
//                                   HttpServletRequest request)
//            throws URISyntaxException, MalformedURLException {
//
//        URI uri = buildProxyUri(request, target);
//        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
//        return proxyRequest(method, uri, httpEntity);
//    }
//
//    private URI buildProxyUri(HttpServletRequest request, String target)
//            throws MalformedURLException, URISyntaxException {
//        String targetParamUrl = extractTargetUrlFromQueryString(request.getQueryString(), target);
//        String unescapedUrl = HtmlUtils.htmlUnescape(targetParamUrl);
//        return new URL(unescapedUrl).toURI();
//    }
//
//    /**
//     * target 의 query param 이 2개 이상일 때 '&' 가 escape 없이 들어오면 뒤에 param 이 누락됨.
//     * request 의 query string 에서 target 으로 시작하는 위치 부터 문자열 끝까지 target url 로 추출
//     */
//    private String extractTargetUrlFromQueryString(String queryString, String target) {
//        int urlStartIndex = queryString.indexOf(target);
//        return queryString.substring(urlStartIndex);
//    }

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
