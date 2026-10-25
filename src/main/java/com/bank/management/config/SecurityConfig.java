package com.bank.management.config;

import com.bank.management.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider(
                        CustomUserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

                provider.setPasswordEncoder(passwordEncoder);

                return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        DaoAuthenticationProvider authenticationProvider) throws Exception {

                CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

                csrfRepository.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Lax"));

                http
                                .authenticationProvider(authenticationProvider)

                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(csrfRepository)
                                                .csrfTokenRequestHandler(
                                                                new CsrfTokenRequestAttributeHandler()))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/",
                                                                "/index.html",
                                                                "/favicon.ico",
                                                                "/error")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/auth/register",
                                                                "/api/auth/csrf")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.POST, "/api/auth/login")
                                                .permitAll()

                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/accounts/**")
                                                .hasRole("CUSTOMER")

                                                .requestMatchers("/api/transactions/**")
                                                .hasRole("CUSTOMER")

                                                .requestMatchers("/api/auth/**")
                                                .authenticated()

                                                .anyRequest()
                                                .authenticated())

                                .formLogin(form -> form
                                                .loginProcessingUrl("/api/auth/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .successHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                        response.setContentType("application/json");
                                                        response.setCharacterEncoding("UTF-8");

                                                        response.getWriter().write(
                                                                        "{\"message\":\"Login successful\"}");
                                                })
                                                .failureHandler((request, response, exception) -> {
                                                        response.setStatus(
                                                                        HttpServletResponse.SC_UNAUTHORIZED);

                                                        response.setContentType("application/json");
                                                        response.setCharacterEncoding("UTF-8");

                                                        response.getWriter().write(
                                                                        "{\"error\":\"Invalid username or password\"}");
                                                })
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"message\":\"Logout successful\"}");
                                                }))

                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, exception) -> {
                                                        response.setStatus(
                                                                        HttpServletResponse.SC_UNAUTHORIZED);

                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"error\":\"Authentication required\"}");
                                                })

                                                .accessDeniedHandler((request, response, exception) -> {
                                                        response.setStatus(
                                                                        HttpServletResponse.SC_FORBIDDEN);

                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"error\":\"Access denied\"}");
                                                }));

                return http.build();
        }
}