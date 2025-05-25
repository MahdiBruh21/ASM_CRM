package com.example.crm.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @Value("${spring.security.oauth2.client.registration.keycloak.redirect-uri}")
    private String redirectUri;

    @GetMapping("/keycloak-auth")
    public ResponseEntity<String> proxyKeycloakAuth(
            @RequestParam(value = "redirect_uri", required = false) String redirectUriParam,
            @RequestParam(value = "state", defaultValue = "default-state") String state,
            @RequestParam(value = "nonce", defaultValue = "default-nonce") String nonce) {
        HttpHeaders headers = new HttpHeaders();
        String authUrl = UriComponentsBuilder
                .fromUriString("http://localhost:8080/oauth2/authorization/keycloak")
                .queryParam("redirect_uri", redirectUriParam != null ? redirectUriParam : redirectUri)
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .encode()
                .build()
                .toUriString();
        headers.setLocation(URI.create(authUrl));
        return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }
}