package com.example.crm.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "OAUTH2_AUTHORIZATION_REQUEST";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "REDIRECT_URI";
    private static final int cookieExpireSeconds = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookie.isPresent()) {
            System.out.println("Loading cookie: " + OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME + "=" + cookie.get().getValue());
            return deserialize(cookie.get().getValue());
        }
        System.out.println("No cookie found: " + OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME + " for request: " + request.getRequestURI());
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookies(request, response);
            System.out.println("Deleting cookies as authorizationRequest is null for request: " + request.getRequestURI());
            return;
        }

        // Save auth request
        Cookie authRequestCookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest));
        configureCookie(authRequestCookie, request);
        response.addCookie(authRequestCookie);
        System.out.println("Saving cookie: " + OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME + "=" + authRequestCookie.getValue() + " for request: " + request.getRequestURI());

        // Save redirect URI
        String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUri)) {
            Cookie redirectUriCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME,
                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            configureCookie(redirectUriCookie, request);
            response.addCookie(redirectUriCookie);
            System.out.println("Saving cookie: " + REDIRECT_URI_PARAM_COOKIE_NAME + "=" + redirectUriCookie.getValue() + " for request: " + request.getRequestURI());
        } else {
            System.out.println("No redirect_uri parameter found for request: " + request.getRequestURI());
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        deleteCookies(request, response);
        System.out.println("Removing cookies, authRequest: " + (authRequest != null) + " for request: " + request.getRequestURI());
        return authRequest;
    }

    private void deleteCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        getCookie(request, name).ifPresent(cookie -> {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setSecure(false);
            response.addCookie(cookie);
            System.out.println("Deleting cookie: " + name + " for request: " + request.getRequestURI());
        });
    }

    private void configureCookie(Cookie cookie, HttpServletRequest request) {
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(cookieExpireSeconds);
        cookie.setSecure(false); // For localhost testing
        cookie.setAttribute("SameSite", "Lax"); // Allow cookies in redirect flow
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            return Base64.getUrlEncoder()
                    .encodeToString(SerializationUtils.serialize(authorizationRequest));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize authorization request", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String serialized) {
        try {
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                    Base64.getUrlDecoder().decode(serialized));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize authorization request", e);
        }
    }

    public Optional<String> getRedirectUri(HttpServletRequest request) {
        return getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(cookie -> URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8));
    }
}