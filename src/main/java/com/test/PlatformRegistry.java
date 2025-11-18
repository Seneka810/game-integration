package com.test;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PlatformRegistry {
    // In-memory data
    private final Set<String> knownPlatforms = Set.of("platformA", "platformB");

    private final Map<String, Set<String>> platformAllowedGames = Map.of(
            "platformA", Set.of("game-1", "game-2"),
            "platformB", Set.of("game-2")
    );

    public boolean platformExists(String platformId) {
        return knownPlatforms.contains(platformId);
    }

    public boolean isGameAllowed(String platformId, String gameId) {
        return platformAllowedGames.getOrDefault(platformId, Collections.emptySet()).contains(gameId);
    }

}
