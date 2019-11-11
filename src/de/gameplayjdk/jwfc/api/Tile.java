package de.gameplayjdk.jwfc.api;

public class Tile implements TileInterface {

    private int id;

    public Tile(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Tile) {
            return ((Tile) object).getId() == this.getId();
        }

        return super.equals(object);
    }
}
