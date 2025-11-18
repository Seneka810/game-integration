package com.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public final class JwtUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtUtil() {}

    public static Map<String, Object> decodePayload(String jwt) {
        try {
            String[] jwtParts = jwt.split("\\.");
            if (jwtParts.length < 2) {
                throw new IllegalArgumentException("Malformed JWT");
            }
            byte[] json = Base64.getUrlDecoder().decode(jwtParts[1]);
            return MAPPER.readValue(new String(json, StandardCharsets.UTF_8), Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode JWT payload", e);
        }
    }

    public static String claimAsString(Map<String, Object> claims, String name) {
        Object claimValue = claims.get(name);
        return claimValue == null ? null : String.valueOf(claimValue);
    }
}
