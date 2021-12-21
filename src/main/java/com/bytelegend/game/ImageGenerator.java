package com.bytelegend.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.bytelegend.game.Constants.IMAGE_GRID_HEIGHT;
import static com.bytelegend.game.Constants.IMAGE_GRID_WIDTH;
import static com.bytelegend.game.Constants.PUBLIC_HEROES_CURRENT_IMAGE_URL;
import static com.bytelegend.game.Constants.TILE_BORDER_PIXEL;
import static com.bytelegend.game.Constants.TILE_HEIGHT_PIXEL;
import static com.bytelegend.game.Constants.TILE_WIDTH_PIXEL;
import static com.bytelegend.game.Constants.TILE_WITH_BORDER_HEIGHT;
import static com.bytelegend.game.Constants.TILE_WITH_BORDER_WIDTH;
import static com.bytelegend.game.Utils.parseSimpleTiles;
import static com.bytelegend.game.Utils.readString;
import static com.bytelegend.game.Utils.writeToPngAndCompress;

abstract class ImageGenerator {
    protected final Environment environment;
    protected final Downloader downloader;

    protected ImageGenerator(Environment environment) {
        this.environment = environment;
        this.downloader = new Downloader(environment);
    }

    protected void writeTile(Graphics graphics, SimpleTile tile, Map<String, File> usernameToAvatarImage) {
        try {
            graphics.clearRect(
                tile.getX() * TILE_WITH_BORDER_WIDTH,
                tile.getY() * TILE_WITH_BORDER_HEIGHT,
                TILE_WITH_BORDER_WIDTH,
                TILE_WITH_BORDER_HEIGHT
            );

            graphics.setColor(new Color(
                Integer.parseInt(tile.getColor().substring(1, 3), 16),
                Integer.parseInt(tile.getColor().substring(3, 5), 16),
                Integer.parseInt(tile.getColor().substring(5, 7), 16)
            ));

            graphics.fillRect(
                tile.getX() * TILE_WITH_BORDER_WIDTH,
                tile.getY() * TILE_WITH_BORDER_HEIGHT,
                TILE_WITH_BORDER_WIDTH,
                TILE_WITH_BORDER_HEIGHT
            );

            BufferedImage avatarImage = ImageIO.read(usernameToAvatarImage.get(tile.getUsername()));

            graphics.drawImage(avatarImage,
                tile.getX() * TILE_WITH_BORDER_WIDTH + TILE_BORDER_PIXEL,
                tile.getY() * TILE_WITH_BORDER_HEIGHT + TILE_BORDER_PIXEL,
                tile.getX() * TILE_WITH_BORDER_WIDTH + TILE_BORDER_PIXEL + TILE_WIDTH_PIXEL,
                tile.getY() * TILE_WITH_BORDER_HEIGHT + TILE_BORDER_PIXEL + TILE_HEIGHT_PIXEL,
                0, 0, avatarImage.getWidth(), avatarImage.getHeight(),
                null
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class IncrementalImageGenerator extends ImageGenerator {
    IncrementalImageGenerator(Environment environment) {
        super(environment);
    }

    void generate(TileDataDiff diff) throws Exception {
        downloader.download(PUBLIC_HEROES_CURRENT_IMAGE_URL, environment.getInputHeroesCurrentImage());

        BufferedImage bufferedImage = ImageIO.read(environment.getInputHeroesCurrentImage());
        Graphics graphics = bufferedImage.getGraphics();
        writeTile(graphics, diff.getChangedTile(), downloader.downloadAvatars(Arrays.asList(diff.getChangedTile())));
        graphics.dispose();
        writeToPngAndCompress(bufferedImage, environment.getOutputHeroesCurrentImage());
    }
}

class FullImageGenerator extends ImageGenerator {
    FullImageGenerator(Environment environment) {
        super(environment);
    }

    void generate() throws Exception {
        generate(parseSimpleTiles(readString(environment.getHeroesJson())));
    }

    void generate(List<SimpleTile> inputTiles) throws Exception {
        BufferedImage bufferedImage = new BufferedImage(
            IMAGE_GRID_WIDTH * TILE_WITH_BORDER_WIDTH,
            IMAGE_GRID_HEIGHT * TILE_WITH_BORDER_HEIGHT,
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics graphics = bufferedImage.getGraphics();

        Map<String, File> usernameToAvatars = downloader.downloadAvatars(inputTiles);

        inputTiles.forEach(tile -> writeTile(graphics, tile, usernameToAvatars));
        graphics.dispose();
        writeToPngAndCompress(bufferedImage, environment.getOutputHeroesCurrentImage());
    }
}
