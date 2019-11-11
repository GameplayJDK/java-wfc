/*
 * The MIT License (MIT)
 * Copyright (c) 2019 GameplayJDK
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
