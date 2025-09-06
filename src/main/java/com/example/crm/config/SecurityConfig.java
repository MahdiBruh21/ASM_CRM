package com.example.crm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("http://localhost:4200/login?session_expired=true")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/login",
                                "/api/auth/status",
                                "/proxy/**",
                                "/api/webhook",
                                "/login/oauth2/code/**",
                                "/oauth2/**",
                                "/logout"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("http://localhost:4200/login")
                        .defaultSuccessUrl("http://localhost:4200/customers", true)
                        .failureUrl("http://localhost:4200/login?error=true")
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/keycloak")
                        )
                        .tokenEndpoint(token -> token
                                .accessTokenResponseClient(accessTokenResponseClient())
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            HttpSession session = request.getSession(false);
                            if (session == null) {
                                logger.info("No session created during login success");
                                session = request.getSession(true);
                            }
                            session.setMaxInactiveInterval(1800); // 30 minutes
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.sendRedirect("http://localhost:4200/customers");
                        })
                        .failureHandler((request, response, exception) -> {
                            logger.error("OAuth2 authentication failed", exception);
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.sendRedirect("http://localhost:4200/login?error=true");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "OAUTH2_AUTHORIZATION_REQUEST", "REDIRECT_URI")
                        .addLogoutHandler((request, response, authentication) -> {
                            logger.info("Logout handler: Starting logout process");
                            HttpSession session = request.getSession(false);
                            if (session != null) {
                                logger.info("Invalidating session: " + session.getId());
                                session.invalidate();
                            } else {
                                logger.info("No session found to invalidate");
                            }
                            cookieAuthorizationRequestRepository().removeAuthorizationRequest(request, response);
                            response.addHeader("Set-Cookie",
                                    "JSESSIONID=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
                            response.addHeader("Set-Cookie",
                                    "JSESSIONID=; Path=/api; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
                            response.addHeader("Set-Cookie",
                                    "OAUTH2_AUTHORIZATION_REQUEST=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
                            response.addHeader("Set-Cookie",
                                    "REDIRECT_URI=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
                            logger.info("Logout handler: Sent Set-Cookie headers to clear JSESSIONID and OAuth cookies");
                        })
                        .logoutSuccessHandler(keycloakLogoutSuccessHandler())
                );

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler keycloakLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            logger.info("LogoutSuccessHandler: Performing Keycloak logout");
            response.addHeader("Set-Cookie",
                    "JSESSIONID=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
            response.addHeader("Set-Cookie",
                    "JSESSIONID=; Path=/api; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
            response.addHeader("Set-Cookie",
                    "OAUTH2_AUTHORIZATION_REQUEST=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
            response.addHeader("Set-Cookie",
                    "REDIRECT_URI=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
            logger.info("LogoutSuccessHandler: Sent final Set-Cookie headers");

            String redirectUri = URLEncoder.encode(
                    "http://localhost:4200/logout-frontchannel",
                    StandardCharsets.UTF_8
            );
            String logoutUrl = "http://localhost:8081/realms/SphynxRealm/protocol/openid-connect/logout" +
                    "?client_id=SphynxApp" +
                    "&post_logout_redirect_uri=" + redirectUri;

            logger.info("Redirecting to Keycloak logout: " + logoutUrl);
            response.sendRedirect(logoutUrl);
        };
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
        configuration.setAllowCredentials(Boolean.valueOf(true));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}