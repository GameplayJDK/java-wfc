package de.gameplayjdk.jwfc.api;

public class TileMapAdapter<T> {

    private final TileMapGeneratorInterface tileMapGenerator;

    private final TileMapAdapter.TileAdapter<T> tileAdapter;

    private TileInterface[] tileMap;

    public TileMapAdapter(TileMapGeneratorInterface tileMapGenerator, TileAdapter<T> tileAdapter) {
        this.tileMapGenerator = tileMapGenerator;
        this.tileAdapter = tileAdapter;
    }

    public TileMapAdapter<T> setTileMap(T[] tileMap) {
        TileInterface[] tileMapReal = this.tileAdapter.toTileMap(tileMap);

        this.tileMapGenerator.analyze(tileMapReal);

        return this;
    }

    public TileMapAdapter<T> generate(int width, int height) {
        return this.generate(width, height, false);
    }

    public TileMapAdapter<T> generate(int width, int height, boolean periodic) {
        this.tileMap = this.tileMapGenerator.generate(width, height, periodic);

        return this;
    }

    public T[] getTileMap() {
        return this.tileAdapter.fromTileMap(this.tileMap);
    }

    public static interface TileAdapter<T> {

        public TileInterface[] toTileMap(T[] tileMap);

        public T[] fromTileMap(TileInterface[] tileMap);
    }
}
