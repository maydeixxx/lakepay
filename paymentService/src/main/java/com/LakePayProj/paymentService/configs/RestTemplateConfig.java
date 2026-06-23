package com.LakePayProj.paymentService.configs;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() throws Exception {
        var sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true)
                .build();
        var socketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setTlsVersions("TLSv1.2")
                .build();
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();
        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        return new RestTemplate(requestFactory);
    }
}