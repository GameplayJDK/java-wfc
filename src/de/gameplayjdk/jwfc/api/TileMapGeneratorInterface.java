package de.gameplayjdk.jwfc.api;

public interface TileMapGeneratorInterface {

    public void analyze(TileInterface[] tileMap);

    public TileInterface[] generate(int width, int height, boolean periodic);
}
