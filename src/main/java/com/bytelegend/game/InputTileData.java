package com.bytelegend.game;

import java.util.Comparator;
import java.util.Objects;

public class InputTileData {
    static final Comparator<InputTileData> COMPARATOR = Comparator.comparingInt(InputTileData::getX)
            .thenComparing(InputTileData::getY);
    private int x;
    private int y;
    private String color;
    private String username;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InputTileData that = (InputTileData) o;
        return x == that.x && y == that.y && Objects.equals(color, that.color) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, color, username);
    }

    @Override
    public String toString() {
        return "InputTileData{" +
                "x=" + x +
                ", y=" + y +
                ", color='" + color + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
