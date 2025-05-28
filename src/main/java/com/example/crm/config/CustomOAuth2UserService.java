package com.example.crm.config;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        if (oauth2User instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) oauth2User;
            OidcIdToken idToken = oidcUser.getIdToken();

            // Create a new attributes map
            Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
            String name = attributes.getOrDefault("preferred_username",
                    attributes.getOrDefault("given_name", oidcUser.getName())).toString();
            attributes.put("name", name);

            // Return DefaultOidcUser with updated attributes
            return new DefaultOidcUser(
                    oidcUser.getAuthorities(),
                    idToken,
                    "name" // Use 'name' as the principal attribute key
            );
        }

        return oauth2User;
    }
}