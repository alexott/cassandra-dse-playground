package com.datastax.alexott.demos.spark;

import java.util.UUID;

public class UUIDData {
    private UUID u;
    private int id;

    public UUIDData() {
    }
    public UUIDData(int id, UUID u) {
        this.u = u;
        this.id = id;
    }

    public UUID getU() {
        return u;
    }

    public void setU(UUID u) {
        this.u = u;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
