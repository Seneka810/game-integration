package com.test.resources;

import com.test.PlatformRegistry;
import com.test.config.JwtUtil;
import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameResourceTest {

    @Test
    void testStartGameUnauthorizedUnknownPlatform() {
        SecurityIdentity mockIdentity = mock(SecurityIdentity.class);
        PlatformRegistry mockRegistry = mock(PlatformRegistry.class);

        String gameId = "game123";
        String rawToken = "sampleToken";
        Map<String, Object> claims = Collections.emptyMap();

        when(mockIdentity.getCredential(
                AccessTokenCredential.class)).thenReturn(
                new AccessTokenCredential(rawToken));
        try (MockedStatic<JwtUtil> mocked = Mockito.mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.decodePayload(rawToken)).thenReturn(claims);
            mocked.when(() -> JwtUtil.claimAsString(claims, "platform_id"))
                    .thenReturn(null);

            GameResource resource = new GameResource(mockIdentity, mockRegistry);

            Response response = resource.startGame(gameId);

            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                    response.getStatus());
            assertEquals("Unauthorized: unknown platform", response.getEntity());
        }
    }

    @Test
    void testStartGameForbiddenGameNotAllowed() {
        SecurityIdentity mockIdentity = mock(SecurityIdentity.class);
        PlatformRegistry mockRegistry = mock(PlatformRegistry.class);

        String gameId = "game123";
        String rawToken = "sampleToken";
        Map<String, Object> claims = Map.of("platform_id", "platform123");

        when(mockIdentity.getCredential(
                AccessTokenCredential.class)).thenReturn(
                new AccessTokenCredential(rawToken));
        try (MockedStatic<JwtUtil> mocked = Mockito.mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.decodePayload(rawToken)).thenReturn(claims);
            mocked.when(() -> JwtUtil.claimAsString(claims, "platform_id"))
                    .thenReturn("platform123");
            when(mockRegistry.platformExists("platform123")).thenReturn(true);
            when(mockRegistry.isGameAllowed("platform123", gameId)).thenReturn(false);

            GameResource resource = new GameResource(mockIdentity, mockRegistry);

            Response response = resource.startGame(gameId);

            assertEquals(Response.Status.FORBIDDEN.getStatusCode(),
                    response.getStatus());
            assertEquals("Forbidden: game not allowed for platform platform123",
                    response.getEntity());
        }
    }

    @Test
    void testStartGameSuccess() {
        SecurityIdentity mockIdentity = mock(SecurityIdentity.class);
        PlatformRegistry mockRegistry = mock(PlatformRegistry.class);

        String gameId = "game123";
        String rawToken = "sampleToken";
        String platformId = "platform123";
        String playerName = "player1";
        Map<String, Object> claims = Map.of("platform_id", platformId);

        when(mockIdentity.getCredential(
                AccessTokenCredential.class)).thenReturn(
                new AccessTokenCredential(rawToken));
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(playerName);
        when(mockIdentity.getPrincipal()).thenReturn(principal);
        try (MockedStatic<JwtUtil> mocked = Mockito.mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.decodePayload(rawToken)).thenReturn(claims);
            mocked.when(() -> JwtUtil.claimAsString(claims, "platform_id"))
                    .thenReturn(platformId);
            when(mockRegistry.platformExists(platformId)).thenReturn(true);
            when(mockRegistry.isGameAllowed(platformId, gameId)).thenReturn(true);

            GameResource resource = new GameResource(mockIdentity, mockRegistry);

            Response response = resource.startGame(gameId);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals("Player player1 on platform platform123 launched game123",
                    response.getEntity());
        }
    }
}