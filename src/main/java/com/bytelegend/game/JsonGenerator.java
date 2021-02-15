package com.bytelegend.game;

import java.time.Instant;
import java.util.List;

import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Utils.parseAllInfoTiles;
import static com.bytelegend.game.Utils.readString;
import static com.bytelegend.game.Utils.writeString;

/**
 * Generate brave-people-all.json
 */
class JsonGenerator {
    private final Environment environment;
    private final Downloader downloader;

    JsonGenerator(Environment environment) {
        this.environment = environment;
        this.downloader = new Downloader(environment);
    }

    void generate(TileDataDiff diff) throws Exception {
        downloader.download(environment.getPublicBravePeopleAllJsonUrl(), environment.getInputBravePeopleAllJson());

        List<AllInfoTile> allInfoTiles = parseAllInfoTiles(readString(environment.getInputBravePeopleAllJson()));

        AllInfoTile tile = allInfoTiles.stream()
                .filter(it ->
                        it.getUsername().equals(diff.getChangedTile().getUsername()))
                .findFirst()
                .orElse(null);

        if (tile == null) {
            AllInfoTile newTile = AllInfoTile.fromSimpleTile(diff.getChangedTile());
            newTile.setChangedAt(Instant.now());
            newTile.setCreatedAt(Instant.now());
            allInfoTiles.add(newTile);
        } else {
            tile.setColor(diff.getChangedTile().getColor());
            tile.setChangedAt(Instant.now());
        }

        allInfoTiles.sort(SimpleTile.COMPARATOR);
        writeString(environment.getOutputBravePeopleAllJson(),
                OBJECT_MAPPER.writeValueAsString(allInfoTiles));
    }
}
