package com.bytelegend.game;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class AllInfoTile extends SimpleTile {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant changedAt;

    public static AllInfoTile fromSimpleTile(SimpleTile simpleTile) {
        AllInfoTile ret = new AllInfoTile();
        ret.setX(simpleTile.getX());
        ret.setY(simpleTile.getY());
        ret.setColor(simpleTile.getColor());
        ret.setUserid(simpleTile.getUserid());
        return ret;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return "AllInfoTile{" +
                "createdAt=" + createdAt +
                ", changedAt=" + changedAt +
                ", x=" + getX() +
                ", y=" + getY() +
                ", color='" + getColor() + '\'' +
                ", username='" + getUserid() + '\'' +
                '}';
    }
}
