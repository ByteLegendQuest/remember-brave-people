package com.bytelegend.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.idrsolutions.image.png.PngCompressor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

    /**
     * Format the tiles to
     */
    static String toFormattedJson(List<SimpleTile> tiles) {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append("\n");
        for (int i = 0; i < tiles.size(); i++) {
            sb.append("  {");
            sb.append("\"x\": ").append(tiles.get(i).getX()).append(", ");
            sb.append("\"y\": ").append(tiles.get(i).getY()).append(", ");
            sb.append("\"color\": \"").append(tiles.get(i).getColor()).append("\", ");
            sb.append("\"username\": \"").append(tiles.get(i).getUsername()).append("\"");
            sb.append("}");
            if (i != tiles.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append(']').append("\n");
        return sb.toString();
    }

    static List<SimpleTile> parseSimpleTiles(String json) throws JsonProcessingException {
        // @formatter:off
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<SimpleTile>>() {
        });
        // @formatter:on
    }

    static TilesInfo parseHeroesCurrentJson(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, TilesInfo.class);
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

    static void writeToPngAndCompress(BufferedImage bufferedImage, File targetPngFile) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", os);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        try (FileOutputStream output = new FileOutputStream(targetPngFile)) {
            PngCompressor.compress(is, output);
        }
    }

    // heroes-current.json -> heroes-X.json
    // heroes-current.png -> heroes-X.png
    // Returns the renamed file
    static File renameCurrentToPage(File file, int page) {
        return new File(file.getParentFile(), file.getName().replace("-current", "-" + page));
    }
}
