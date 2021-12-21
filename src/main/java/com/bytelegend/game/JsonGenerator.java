package com.bytelegend.game;

import java.time.Instant;

import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Utils.parseHeroesCurrentJson;
import static com.bytelegend.game.Utils.readString;
import static com.bytelegend.game.Utils.writeString;

/**
 * Generate heroes-current.json
 */
class JsonGenerator {
    private final Environment environment;
    private final Downloader downloader;

    JsonGenerator(Environment environment) {
        this.environment = environment;
        this.downloader = new Downloader(environment);
    }

    TilesInfo generate(TileDataDiff diff) throws Exception {
        downloader.download(environment.getPublicHeroesCurrentJsonUrl(), environment.getInputHeroesCurrentJson());
        TilesInfo tilesInfo = parseHeroesCurrentJson(readString(environment.getInputHeroesCurrentJson()));

        AllInfoTile tile = tilesInfo.getTiles().stream()
                .filter(it ->
                        it.getUsername().equals(diff.getChangedTile().getUsername()))
                .findFirst()
                .orElse(null);

        if (tile == null) {
            AllInfoTile newTile = AllInfoTile.fromSimpleTile(diff.getChangedTile());
            newTile.setChangedAt(Instant.now());
            newTile.setCreatedAt(Instant.now());
            tilesInfo.getTiles().add(newTile);
        } else {
            tile.setColor(diff.getChangedTile().getColor());
            tile.setChangedAt(Instant.now());
        }

        writeString(environment.getOutputHeroesCurrentJson(),
                OBJECT_MAPPER.writeValueAsString(tilesInfo));
        return tilesInfo;
    }
}
