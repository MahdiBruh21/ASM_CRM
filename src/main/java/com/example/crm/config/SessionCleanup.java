//package com.example.crm.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.security.core.session.SessionRegistry;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SessionCleanup {
//    @Autowired
//    private SessionRegistry sessionRegistry;
//
//    @Scheduled(fixedRate = 60000) // Every minute
//    public void cleanUpExpiredSessions() {
//        sessionRegistry.getAllPrincipals().forEach(principal ->
//                sessionRegistry.getAllSessions(principal, true).forEach(sessionInfo -> {
//                    if (sessionInfo.isExpired()) {
//                        sessionRegistry.removeSessionInformation(sessionInfo.getSessionId());
//                    }
//                }));
//    }
//}