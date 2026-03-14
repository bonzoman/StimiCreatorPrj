package com.stimi.creator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc 추가설정(설정 커스터마이징)
 * 기본 리소스 위치 classpath: /static /public /resources /META-INF/resources
 */
@Configuration
//@EnableWebMvc 이거 붙이면 springboot가 제공하는 모든 mvc기능은 사라지고 web mvc 기능 모두 구현해야 함
public class WebConfig implements WebMvcConfigurer {



}
