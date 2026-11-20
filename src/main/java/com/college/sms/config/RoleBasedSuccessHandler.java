package com.college.sms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * After a successful login, sends teachers to /teacher/dashboard and students to /student/dashboard.
 */
@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.sendRedirect(request.getContextPath() + dashboardFor(authentication));
    }

    /** Dashboard URL for the logged-in user's role. */
    public static String dashboardFor(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_TEACHER".equals(authority.getAuthority())) {
                return "/teacher/dashboard";
            }
        }
        return "/student/dashboard";
    }
}
