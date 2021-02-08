package com.bytelegend.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.bytelegend.game.Constants.IMAGE_GRID_HEIGHT;
import static com.bytelegend.game.Constants.IMAGE_GRID_WIDTH;
import static com.bytelegend.game.Utils.parse;

/**
 * Compare player's JSON and previous version to avoid malicious change.
 */
class TileDataDiff {
    private static final Pattern COLOR_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private final List<InputTileData> oldTiles;
    private final List<InputTileData> newTiles;
    private final InputTileData changedTile;
    private final String playerGitHubUsername;

    TileDataDiff(String oldDataJson, String newDataJson, String playerGitHubUsername) throws Exception {
        this.oldTiles = parse(oldDataJson);
        this.newTiles = parse(newDataJson);
        this.playerGitHubUsername = playerGitHubUsername;
        this.changedTile = diff();
    }

    List<InputTileData> getOldTiles() {
        return oldTiles;
    }

    List<InputTileData> getNewTiles() {
        return newTiles;
    }

    InputTileData getChangedTile() {
        return changedTile;
    }

    private Set<InputTileData> removeAll(Collection<InputTileData> set1, Collection<InputTileData> set2) {
        Set<InputTileData> tmp1 = new HashSet<>(set1);
        Set<InputTileData> tmp2 = new HashSet<>(set2);
        tmp1.removeAll(tmp2);
        return tmp1;
    }

    /**
     * @return the tile changed with same username as {@link #playerGitHubUsername}
     */
    private InputTileData diff() {
        Set<InputTileData> addedTiles = removeAll(newTiles, oldTiles);
        Set<InputTileData> removedTiles = removeAll(oldTiles, newTiles);

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
        InputTileData removedTile = removedTiles.stream()
                .filter(this::checkTileUsername)
                .findFirst()
                .orElse(null);
        InputTileData addedTile = addedTiles.stream()
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

    private boolean checkTileUsername(InputTileData tile) {
        return playerGitHubUsername == null || playerGitHubUsername.equals(tile.getUsername());
    }
}

