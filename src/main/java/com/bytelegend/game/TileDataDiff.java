package com.bytelegend.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.bytelegend.game.Constants.IMAGE_GRID_HEIGHT;
import static com.bytelegend.game.Constants.IMAGE_GRID_WIDTH;
import static com.bytelegend.game.Utils.parseSimpleTiles;

/**
 * Compare player's JSON and previous version to avoid malicious change.
 */
class TileDataDiff {
    private static final Pattern COLOR_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private final List<SimpleTile> oldTiles;
    private final List<SimpleTile> newTiles;
    private final SimpleTile changedTile;
    private final String playerGitHubUsername;

    TileDataDiff(String oldDataJson, String newDataJson, String playerGitHubUsername) throws Exception {
        this.oldTiles = parseSimpleTiles(oldDataJson);
        this.newTiles = parseSimpleTiles(newDataJson);
        this.playerGitHubUsername = playerGitHubUsername;
        this.changedTile = diff();
    }

    List<SimpleTile> getOldTiles() {
        return oldTiles;
    }

    List<SimpleTile> getNewTiles() {
        return newTiles;
    }

    SimpleTile getChangedTile() {
        return changedTile;
    }

    private Set<SimpleTile> removeAll(Collection<SimpleTile> set1, Collection<SimpleTile> set2) {
        Set<SimpleTile> tmp1 = new HashSet<>(set1);
        Set<SimpleTile> tmp2 = new HashSet<>(set2);
        tmp1.removeAll(tmp2);
        return tmp1;
    }

    /**
     * @return the tile changed with same username as {@link #playerGitHubUsername}
     */
    private SimpleTile diff() {
        newTiles.stream().collect(Collectors.groupingBy(SimpleTile::getUsername))
            .values().forEach(tilesWithSameName -> {
                if (tilesWithSameName.size() > 1) {
                    throw new IllegalStateException("Duplicate username: " + tilesWithSameName.get(0).getUsername());
                }
            });

        Set<SimpleTile> addedTiles = removeAll(newTiles, oldTiles);
        Set<SimpleTile> removedTiles = removeAll(oldTiles, newTiles);

        if (addedTiles.isEmpty() && removedTiles.isEmpty()) {
            throw new IllegalStateException("You didn't change anything!");
        }
        if (addedTiles.isEmpty()) {
            throw new IllegalStateException("You are not allowed to remove tile!");
        }
        if (addedTiles.stream().anyMatch(it -> !checkTileUsername(it))
            || removedTiles.stream().anyMatch(it -> !checkTileUsername(it))) {
            throw new IllegalStateException("You are not allowed to change other one's tile!");
        }
        if (addedTiles.size() > 1 || removedTiles.size() > 1) {
            throw new IllegalStateException("You are not allowed to modify more than 1 tile!");
        }
        SimpleTile removedTile = removedTiles.stream()
            .filter(this::checkTileUsername)
            .findFirst()
            .orElse(null);
        SimpleTile addedTile = addedTiles.stream()
            .filter(this::checkTileUsername)
            .findFirst()
            .orElse(null);

        if (removedTile != null && addedTile != null) {
            if (removedTile.getX() != addedTile.getX() || removedTile.getY() != addedTile.getY()) {
                throw new IllegalStateException("You are not allowed to change tile's location!");
            }
        }
        if (newTiles.stream()
            .anyMatch(it -> it.getX() == addedTile.getX() &&
                it.getY() == addedTile.getY()
                && !it.getUsername().equals(addedTile.getUsername()))
        ) {
            throw new IllegalStateException(String.format("Conflict: tile (%d,%d) already exists!", addedTile.getX(), addedTile.getY()));
        }

        if (addedTile.getX() < 0 ||
            addedTile.getX() >= IMAGE_GRID_WIDTH ||
            addedTile.getY() < 0 ||
            addedTile.getY() >= IMAGE_GRID_HEIGHT) {
            throw new IllegalStateException(String.format("Invalid location: (%d,%d)", addedTile.getX(), addedTile.getY()));
        }

        if (!COLOR_PATTERN.matcher(addedTile.getColor()).matches()) {
            throw new IllegalStateException("Invalid tile: " + addedTile);
        }
        return addedTile;
    }

    private boolean checkTileUsername(SimpleTile tile) {
        return playerGitHubUsername == null || playerGitHubUsername.equalsIgnoreCase(tile.getUsername());
    }
}

