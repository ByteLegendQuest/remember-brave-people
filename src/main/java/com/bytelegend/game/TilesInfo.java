package com.bytelegend.game;

import java.util.ArrayList;
import java.util.List;

public class TilesInfo {
    private int page;
    private List<AllInfoTile> tiles = new ArrayList<>();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<AllInfoTile> getTiles() {
        return tiles;
    }

    public void setTiles(List<AllInfoTile> tiles) {
        this.tiles = tiles;
    }
}
