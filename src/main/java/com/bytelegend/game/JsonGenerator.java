package com.bytelegend.game;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Constants.PRETTY_PRINTER;
import static com.bytelegend.game.Utils.parseHeroesCurrentJson;
import static com.bytelegend.game.Utils.readString;
import static com.bytelegend.game.Utils.writeString;

/**
 * Generate heroes-current.json
 */
class JsonGenerator {
    private final Environment environment;
    private final Downloader downloader;

    public static void main(String[] args) throws IOException {
        List<AllInfoTile> tiles = OBJECT_MAPPER.readValue(new File("/Users/zhb/Projects/quests/build/challenges/JavaIsland/remember-brave-people/2.json"), new TypeReference<List<AllInfoTile>>() {
        });
//        System.out.println("FUCK: " + tiles.size());
//        System.out.println("FUCK: " + tiles.stream().map(AllInfoTile::getUsername).collect(Collectors.toSet()).size());
        System.out.println(tiles.stream().collect(Collectors.groupingBy(AllInfoTile::getUsername)).entrySet().stream().filter(it -> it.getValue().size() != 1).collect(Collectors.toList()));
//        List<String> list2 = OBJECT_MAPPER.readValue(new File("/Users/zhb/Projects/quests/build/challenges/JavaIsland/remember-brave-people/2.json"), new TypeReference<List<AllInfoTile>>() {
//        }).stream().map(AllInfoTile::getUsername).collect(Collectors.toList());
//
//        list2.removeAll(list1);
//        System.out.println(list2);


    }

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

        writeString(environment.getOutputHeroesCurrentJson(), PRETTY_PRINTER.writeValueAsString(tilesInfo));
        return tilesInfo;
    }
}
