package de.gameplayjdk.jwfc.test;

import de.gameplayjdk.jwfc.api.TileInterface;

public class TileWithIdAndName implements TileInterface {

    private final int id;

    private final String name;

    public TileWithIdAndName(int id, String name) {
        this.id = id;

        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
