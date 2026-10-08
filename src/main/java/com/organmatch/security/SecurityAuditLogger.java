package com.organmatch.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Event listener providing SLF4J audit logging for login success and failure events.
 */
@Component
@Slf4j
public class SecurityAuditLogger {

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("LOGIN SUCCESS: user={}", username);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String error = event.getException() != null ? event.getException().getMessage() : "Bad credentials";
        log.warn("LOGIN FAILURE: user={}, reason={}", username, error);
    }
}
