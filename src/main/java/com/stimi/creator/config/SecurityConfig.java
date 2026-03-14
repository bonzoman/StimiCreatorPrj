package com.stimi.creator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // HTTPS 환경에서 POST 요청이 차단되지 않도록 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/health").permitAll() // Swagger 허용
//                        .requestMatchers("/api/**").permitAll() // Xcode(iOS) API 요청 허용
//                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                        .anyRequest().permitAll()
                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/", false)
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                        .permitAll()
//                )
//                .headers(headers -> headers
//                        .frameOptions(frame -> frame.sameOrigin()) // H2 콘솔이나 iFrame 사용 대비
//                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // HTMX 요청인 경우 전체 페이지 리다이렉트를 위해 HX-Redirect 헤더 사용
                            if ("true".equals(request.getHeader("HX-Request"))) {
                                response.setHeader("HX-Redirect", "/login");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );

        return http.build();
    }

    // private final AuthenticationEntryPoint unauthorizedEntryPoint =
    // (request, response, authException) -> {
    // ErrorResponse fail = new ErrorResponse(HttpStatus.UNAUTHORIZED, "Spring
    // security unauthorized...111");
    // response.setStatus(HttpStatus.UNAUTHORIZED.value());
    // String json = new ObjectMapper().writeValueAsString(fail);
    // response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    // PrintWriter writer = response.getWriter();
    // writer.write(json);
    // writer.flush();
    // };
    //
    // private final AccessDeniedHandler accessDeniedHandler =
    // (request, response, accessDeniedException) -> {
    // ErrorResponse fail = new ErrorResponse(HttpStatus.FORBIDDEN, "Spring security
    // forbidden...222");
    // response.setStatus(HttpStatus.FORBIDDEN.value());
    // String json = new ObjectMapper().writeValueAsString(fail);
    // response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    // PrintWriter writer = response.getWriter();
    // writer.write(json);
    // writer.flush();
    // };

    // @Getter
    // @RequiredArgsConstructor
    // public class ErrorResponse {
    //
    // private final HttpStatus status;
    // private final String message;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
