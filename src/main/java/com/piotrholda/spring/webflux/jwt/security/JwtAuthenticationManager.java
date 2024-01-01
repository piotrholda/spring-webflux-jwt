package com.piotrholda.spring.webflux.jwt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(extractToken(authentication))
                .flatMap(this::convert)
                .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid JWT token.")));
    }

    private Optional<String> extractToken(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .filter(JwtToken.class::isInstance)
                .map(JwtToken.class::cast)
                .map(JwtToken::getToken);
    }

    private Mono<Authentication> convert(String token) {
        String username = jwtService.extractUsername(token);
        return userDetailsService.findByUsername(username)
                .filter(userDetails -> jwtService.isTokenValid(token, userDetails))
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));
    }
}
