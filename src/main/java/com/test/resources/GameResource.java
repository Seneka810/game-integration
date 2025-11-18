package com.test.resources;

import com.test.PlatformRegistry;
import com.test.config.JwtUtil;
import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/game")
@Produces(MediaType.TEXT_PLAIN)
public class GameResource {
    private final SecurityIdentity identity;
    private final PlatformRegistry registry;

    public GameResource(SecurityIdentity identity, PlatformRegistry registry) {
        this.identity = identity;
        this.registry = registry;
    }

    @GET
    @Path("{gameId}")
    @Authenticated
    public Response startGame(@PathParam("gameId") String gameId) {
        // Use case 2: without a valid Keycloak token, returns 401.
        String rawToken = identity.getCredential(AccessTokenCredential.class).getToken();
        Map<String, Object> claims = JwtUtil.decodePayload(rawToken);

        String platformId = JwtUtil.claimAsString(claims, "platform_id");
        if (platformId == null || !registry.platformExists(platformId)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Unauthorized: unknown platform")
                    .build();
        }

        // Use case 3: authenticated but not allowed for this game
        if (!registry.isGameAllowed(platformId, gameId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Forbidden: game not allowed for platform " + platformId)
                    .build();
        }

        // Use case 1: success
        String displayPlayer = identity.getPrincipal().getName();
        String msg = "Player " + displayPlayer +
                     " on platform " + platformId +
                     " launched " + gameId;
        return Response.ok(msg).build();
    }

}
