package com.example.crm.controller;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User user) {
        Map<String, Object> userInfo = new HashMap<>();
        if (user != null) {
            userInfo.put("id", user.getAttribute("sub"));
            userInfo.put("username", user.getAttribute("preferred_username"));
            userInfo.put("firstName", user.getAttribute("given_name"));
            userInfo.put("lastName", user.getAttribute("family_name"));
            userInfo.put("email", user.getAttribute("email"));
            userInfo.put("name", user.getAttribute("name"));
        } else {
            userInfo.put("error", "No authenticated user found");
        }
        return userInfo;
    }
}
