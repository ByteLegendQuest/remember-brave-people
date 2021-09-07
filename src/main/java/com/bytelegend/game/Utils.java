package com.bytelegend.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static com.bytelegend.game.Constants.OBJECT_MAPPER;

class Utils {
    static void readSystemPropertiesFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-D")) {
                int equalIndex = arg.indexOf("=");
                if (equalIndex != -1) {
                    System.setProperty(arg.substring(2, equalIndex), arg.substring(equalIndex + 1));
                }
            }
        }
    }




    static List<SimpleTile> parseSimpleTiles(String json) throws JsonProcessingException {
        // @formatter:off
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<SimpleTile>>() {});
        // @formatter:on
    }

    static List<AllInfoTile> parseAllInfoTiles(String json) throws JsonProcessingException {
        // @formatter:off
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<AllInfoTile>>() {});
        // @formatter:on
    }

    static String readString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    static String readString(File dir, String file) throws IOException {
        return readString(new File(dir, file));
    }

    static void writeString(File dir, String name, String content) throws IOException {
        writeString(new File(dir, name), content);
    }

    static void writeString(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}
