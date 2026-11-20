package com.college.sms.controller;

import com.college.sms.config.RoleBasedSuccessHandler;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Login page, home redirect and access-denied page.
 * The POST /login and POST /logout requests are handled by Spring Security itself.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:" + RoleBasedSuccessHandler.dashboardFor(authentication);
        }
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:" + RoleBasedSuccessHandler.dashboardFor(authentication);
        }
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
