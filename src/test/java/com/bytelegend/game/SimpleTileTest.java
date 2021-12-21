package com.bytelegend.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class SimpleTileTest {
    @Test
    public void tilesAreEqualIgnoreCase() {
        SimpleTile tile1 = TestUtils.createTile("foo", 1, 2, "#123456");
        SimpleTile tile2 = TestUtils.createTile("Foo", 1, 2, "#123456");

        Assertions.assertEquals(tile1, tile2);
        Assertions.assertEquals(tile1.hashCode(), tile2.hashCode());
    }

    @Test
    public void formatTilesJson() {
        SimpleTile tile1 = TestUtils.createTile("foo", 3, 4, "#654321");
        SimpleTile tile2 = TestUtils.createTile("bar", 1, 2, "#123456");

        Assertions.assertEquals("[\n" +
            "  {\"x\": 3, \"y\": 4, \"color\": \"#654321\", \"username\": \"foo\"},\n" +
            "  {\"x\": 1, \"y\": 2, \"color\": \"#123456\", \"username\": \"bar\"}\n" +
            "]", Utils.toFormattedJson(Arrays.asList(tile1, tile2)).trim());
    }
}
