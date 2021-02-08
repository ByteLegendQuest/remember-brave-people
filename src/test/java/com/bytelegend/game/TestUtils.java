package com.bytelegend.game;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.function.Executable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    static void assertTileWritten(File outputImage, int x, int y, String rgba) throws IOException {
        int originX = x * TILE_WITH_BORDER_WIDTH;
        int originY = y * TILE_WITH_BORDER_HEIGHT;
        // corners are #000, others are opaque
        assertEquals(rgba, readPixel(outputImage, originX, originY).toString());
        assertEquals(rgba, readPixel(outputImage, originX + TILE_WITH_BORDER_WIDTH - 1, originY + TILE_WITH_BORDER_HEIGHT - 1).toString());
        assertEquals(255, readPixel(outputImage, originX + TILE_BORDER_PIXEL, originY + TILE_BORDER_PIXEL).a);
        assertEquals(255, readPixel(outputImage, originX + TILE_WITH_BORDER_WIDTH / 2, originY + TILE_WITH_BORDER_HEIGHT / 2).a);
    }

    static RGBA readPixel(File imageFile, int x, int y) throws IOException {
        BufferedImage img = ImageIO.read(imageFile);
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
