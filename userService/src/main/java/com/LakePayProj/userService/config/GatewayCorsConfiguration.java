//package com.LakePayProj.userService.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.cors.reactive.CorsConfigurationSource;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//
//import java.util.List;
//
//@Configuration
//public class GatewayCorsConfiguration {
//
//    @Bean
//    public CorsWebFilter websocketCorsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOriginPatterns(List.of("*"));
//        config.setAllowedMethods(List.of("GET", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/ws/**", config);
//
//        return new CorsWebFilter((CorsConfigurationSource) source);
//    }
//}