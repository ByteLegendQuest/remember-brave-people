package com.bytelegend.game;

import java.util.ArrayList;
import java.util.List;

public class TilesInfo {
    private int currentPage;
    private List<AllInfoTile> tiles = new ArrayList<>();

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<AllInfoTile> getTiles() {
        return tiles;
    }

    public void setTiles(List<AllInfoTile> tiles) {
        this.tiles = tiles;
    }
}
