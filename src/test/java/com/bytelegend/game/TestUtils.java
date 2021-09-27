package com.bytelegend.game;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.function.Executable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.bytelegend.game.Constants.TILE_BORDER_PIXEL;
import static com.bytelegend.game.Constants.TILE_WITH_BORDER_HEIGHT;
import static com.bytelegend.game.Constants.TILE_WITH_BORDER_WIDTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestUtils {
    static void assertExceptionWithMessage(String message, Executable runnable) {
        Throwable e = assertThrows(Throwable.class, runnable);
        MatcherAssert.assertThat(e.getMessage(), CoreMatchers.containsString(message));
    }

    static boolean isClose(Instant instant1, Instant instant2) {
        return Math.abs(instant1.getEpochSecond() - instant2.getEpochSecond()) < 60;
    }

    static void assertImageWritten(File outputImage, int x, int y, String rgba) throws IOException {
        int originX = x * TILE_WITH_BORDER_WIDTH;
        int originY = y * TILE_WITH_BORDER_HEIGHT;
        // corners are #000, others are opaque
        assertEquals(rgba, readPixel(outputImage, originX, originY).toString());
        assertEquals(rgba, readPixel(outputImage, originX + TILE_WITH_BORDER_WIDTH - 1, originY + TILE_WITH_BORDER_HEIGHT - 1).toString());
        assertEquals(255, readPixel(outputImage, originX + TILE_BORDER_PIXEL, originY + TILE_BORDER_PIXEL).a);
        assertEquals(255, readPixel(outputImage, originX + TILE_WITH_BORDER_WIDTH / 2, originY + TILE_WITH_BORDER_HEIGHT / 2).a);
    }

    static List<List<RGBA>> readAllPixels(File imageFile) throws IOException {
        List<List<RGBA>> ret = new ArrayList<>();
        BufferedImage img = ImageIO.read(imageFile);
        for (int row = 0; row < img.getHeight(); ++row) {
            List<RGBA> pixelRow = new ArrayList<>();
            for (int col = 0; col < img.getWidth(); ++col) {
                pixelRow.add(readPixel(img, col, row));
            }
            ret.add(pixelRow);
        }
        return ret;
    }

    private static RGBA readPixel(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        if (img.getType() == BufferedImage.TYPE_BYTE_BINARY) {
            return new RGBA(
                ((pixel & 0xff0000) >>> 16),
                ((pixel & 0xff00) >>> 8),
                (pixel & 0xff),
                0xff
            );
        } else {
            return new RGBA(
                ((pixel & 0xff0000) >>> 16),
                ((pixel & 0xff00) >>> 8),
                (pixel & 0xff),
                ((pixel & 0xff000000) >>> 24)
            );
        }
    }

    static RGBA readPixel(File imageFile, int x, int y) throws IOException {
        return readPixel(ImageIO.read(imageFile), x, y);
    }

    static AllInfoTile createTile(SimpleTile tile) {
        AllInfoTile ret = new AllInfoTile();
        ret.setUserid(tile.getUserid());
        ret.setColor(tile.getColor());
        ret.setX(tile.getX());
        ret.setY(tile.getY());
        ret.setCreatedAt(Instant.now());
        ret.setChangedAt(Instant.now());
        return ret;
    }

    static SimpleTile createTile(String userid, int x, int y, String color) {
        SimpleTile tile = new SimpleTile();
        tile.setUserid(userid);
        tile.setX(x);
        tile.setY(y);
        tile.setColor(color);
        return tile;
    }
}

class RGBA {
    final int r;
    final int g;
    final int b;
    final int a;

    RGBA(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public String toString() {
        return String.format("rgba(%d,%d,%d,%d)", r, g, b, a);
    }
}
