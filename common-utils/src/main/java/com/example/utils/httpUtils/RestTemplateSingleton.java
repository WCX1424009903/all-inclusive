package com.example.utils.httpUtils;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 单例构建
 *
 * @author wcx
 * @date 2023/11/24
 */
@Slf4j
public class RestTemplateSingleton {

    private static volatile RestTemplate sharedInstance;

    private RestTemplateSingleton() {
    }

    public static RestTemplate getInstance() {
        RestTemplate sharedInstance = RestTemplateSingleton.sharedInstance;
        if (sharedInstance == null) {
            synchronized (RestTemplateSingleton.class) {
                sharedInstance = RestTemplateSingleton.sharedInstance;
                if (sharedInstance == null) {
                    RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory());
                    restTemplate.getInterceptors().add(new RequestInterceptor());
                    setCharset(restTemplate);
                    sharedInstance = restTemplate;
                    RestTemplateSingleton.sharedInstance = sharedInstance;
                }
            }
        }
        return sharedInstance;
    }

    /**
     * 设置字符集
     */
    private static void setCharset(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * 信任所有ssl证书
     *
     * @return {@link ClientHttpRequestFactory}
     */
    private static ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        HttpClientConnectionManager httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(connectionSocketFactory).build();
        httpClientBuilder.setConnectionManager(httpClientConnectionManager);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        // 超时时间毫秒
        factory.setConnectTimeout(15000);
        factory.setConnectionRequestTimeout(15000);
        return factory;
    }


    /**
     * 请求拦截器
     *
     * @author wcx
     * @date 2023/11/24
     */
    private static class RequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            HttpHeaders headers = request.getHeaders();
            // 统一请求头设置
            headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
            headers.set(HttpHeaders.CONNECTION, "keep-alive");
            String txtBody = new String(body, StandardCharsets.UTF_8);
            log.info("第三方请求地址:{},请求方法:{},请求头:{},请求内容:{}", request.getURI(), request.getMethod(),
                    request.getHeaders(), txtBody);
            ClientHttpResponse clientHttpResponse = execution.execute(request, body);
            if (!HttpStatus.OK.equals(clientHttpResponse.getStatusCode())) {
                log.error("第三方响应错误，请求地址:{},请求方法:{},请求头:{},请求内容:{},响应错误码:{},响应错误码内容:{}", request.getURI()
                        , request.getMethod(), request.getHeaders(), txtBody, clientHttpResponse.getStatusCode()
                        , clientHttpResponse.getStatusText());
            }
            return clientHttpResponse;
        }

        /**
         * get方法参数转换
         */
        private <T> String doFetchUriContent(String urlAddress, T params) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(urlAddress);
            // 不开启驼峰转下划线，忽略空值
            Map<String, Object> queryParams = BeanUtil.beanToMap(params, false, true);
            if (!CollectionUtils.isEmpty(queryParams)) {
                queryParams.forEach(uriComponentsBuilder::queryParam);
            }
            URI uri = uriComponentsBuilder.build().toUri();
            return uri.toString();
        }
    }

}
