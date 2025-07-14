package com.example.keycloak.component;


import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Component // * Marks this class as a Spring-managed component (for dependency injection)
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> { // * <S,T>

    // * Default converter that extracts authorities from the "scope" or "scp" claim in JWT
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    // * Reads the principal claim name (e.g. "preferred_username") from application properties
    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;

    // * Reads the resource ID (usually the client ID) to extract roles for that specific resource
    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    // * Main conversion logic: transforms the incoming Jwt token into an Authentication object
    // * overrides the Convert Functional Interface Method
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

        // * Combine authorities from both default scopes and custom resource roles
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(), // * scopes
                this.extractResourceRoles(jwt).stream()              // * roles from resource_access
        ).collect(Collectors.toSet()); // * Use Set to eliminate duplicates

        // * Return a JwtAuthenticationToken containing the jwt, combined authorities, and principal name
        return new JwtAuthenticationToken(
                jwt,
                authorities,
                this.getPrincipleClaimName(jwt)
        );
    }

    // * Extracts the principal name from the JWT using the configured attribute or fallback to "sub"
    // * With reference to the application.properties declaration
    private String getPrincipleClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB; // * default to "sub" claim (subject)
        if (this.principleAttribute != null) {
            claimName = this.principleAttribute; // * override if configured
        }
        return jwt.getClaim(claimName); // * fetch the actual value from the JWT
    }

    // * Extracts roles from the "resource_access" section of the JWT
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        // * If "resource_access" is missing, return empty role set
        if (jwt.getClaim("resource_access") == null) {
            return Set.of();
        }
        resourceAccess = jwt.getClaim("resource_access");

        // * If the specific resource ID (e.g. client ID) is not present, return empty
        if (resourceAccess.get(this.resourceId) == null) {
            return Set.of();
        }
        // * Get the role data under the specified resource
        resource = (Map<String, Object>) resourceAccess.get(this.resourceId);

        // * Get the "roles" list (as strings)
        resourceRoles = (Collection<String>) resource.get("roles");

        // * Prefix each role with "ROLE_" and map to Spring Security's GrantedAuthority
        return resourceRoles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet()); // * again use Set to prevent duplicates
    }
}
