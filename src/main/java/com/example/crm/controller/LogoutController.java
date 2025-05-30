package com.example.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    @PostMapping("/api/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("LogoutController: Processing /api/logout request");
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Invalidating session: " + session.getId());
            session.invalidate();
        } else {
            logger.info("No session found to invalidate");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("Clearing authentication for user: " + auth.getName());
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        response.addHeader("Set-Cookie",
                "JSESSIONID=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
        response.addHeader("Set-Cookie",
                "JSESSIONID=; Path=/api; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
        logger.info("LogoutController: Sent Set-Cookie headers to clear JSESSIONID");
    }
}