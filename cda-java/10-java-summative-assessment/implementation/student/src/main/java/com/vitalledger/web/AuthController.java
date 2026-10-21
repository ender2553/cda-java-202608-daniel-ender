package com.vitalledger.web;

import com.vitalledger.dto.MeResponse;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth surface. Authentication itself is HTTP Basic (handled by Spring Security);
 * this endpoint reports the authenticated identity and its granted roles — a simple
 * "who am I" that confirms a credential is valid and shows the caller's authority set.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        // TODO(phase D): return a MeResponse with the authenticated username and its granted role authorities.
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
