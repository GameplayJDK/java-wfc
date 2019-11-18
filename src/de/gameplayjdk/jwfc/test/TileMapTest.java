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

package de.gameplayjdk.jwfc.test;

import de.gameplayjdk.jwfc.api.Tile;
import de.gameplayjdk.jwfc.api.TileInterface;
import de.gameplayjdk.jwfc.api.TileMapAdapter;
import de.gameplayjdk.jwfc.api.TileMapGenerator;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public class TileMapTest implements TileMapAdapter.TileAdapter<TileWithIdAndName> {

    private static final String[] TILE_NAME = {
            "Border",
            "Content",
            "Center",
    };

    private static final int TILE_MAP_WIDTH = 5;
    private static final int TILE_MAP_HEIGHT = 5;

    private static final int[] TILE_MAP = {
            0, 0, 0, 0, 0,
            0, 1, 1, 1, 0,
            0, 1, 2, 1, 0,
            0, 1, 1, 1, 0,
            0, 0, 0, 0, 0,
    };

    private static final int TILE_MAP_WIDTH_NEW = 10;
    private static final int TILE_MAP_HEIGHT_NEW = 10;

    private final TileMapAdapter<TileWithIdAndName> tileMapAdapter;

    private TileWithIdAndName[] tileMap;

    public TileMapTest() {
        TileMapGenerator tileMapGenerator = new TileMapGenerator(TileMapTest.TILE_MAP_WIDTH, TileMapTest.TILE_MAP_HEIGHT);

        this.tileMapAdapter = new TileMapAdapter<TileWithIdAndName>(tileMapGenerator, this);
    }

    private void initialize() {
        this.tileMap = TileMapTest.createTileArrayFromIntegerArray(TileMapTest.TILE_MAP_WIDTH, TileMapTest.TILE_MAP_HEIGHT, TileMapTest.TILE_MAP);

        System.out.println(String.format(":: input (width=%1$d, height=%2$d)", TileMapTest.TILE_MAP_WIDTH, TileMapTest.TILE_MAP_HEIGHT));

        this.printTileMap(TileMapTest.TILE_MAP_WIDTH, TileMapTest.TILE_MAP_HEIGHT, this.tileMap);
    }

    public void execute() {
        if (null == this.tileMap) {
            this.initialize();
        }

        this.tileMapAdapter.setTileMap(this.tileMap);

        this.generateAndPrint(TileMapTest.TILE_MAP_WIDTH_NEW, TileMapTest.TILE_MAP_HEIGHT_NEW, false);
        this.generateAndPrint(TileMapTest.TILE_MAP_WIDTH_NEW, TileMapTest.TILE_MAP_HEIGHT_NEW, true);
    }

    private void generateAndPrint(int width, int height, boolean periodic) {
        System.out.println(String.format(":: output (width=%1$d, height=%2$d, periodic=%3$b)", width, height, periodic));

        this.tileMapAdapter.generate(width, height, periodic);

        TileWithIdAndName[] tileMap = this.tileMapAdapter.getTileMap();

        this.printTileMap(width, height, tileMap);
    }

    private void printTileMap(int width, int height, TileInterface[] tileMap) {
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                System.out.print(tileMap[(h * width) + w].getId());
                System.out.print("  ");
            }

            System.out.println();
        }
    }

    @Override
    public TileInterface[] toTileMap(TileWithIdAndName[] tileArray) {
        TileInterface[] tileMap = new TileInterface[tileArray.length];

        for (int index = 0; index < tileArray.length; index++) {
            TileWithIdAndName tile = tileArray[index];

            tileMap[index] = new Tile(tile.getId());

            // Alternatively, since the TileWithIdAndName implements TileInterface:
            //tileMap[index] = tileArray[index];
        }

        return tileMap;
    }

    @Override
    public TileWithIdAndName[] fromTileMap(TileInterface[] tileArray) {
        int[] integerArray = Arrays.stream(tileArray)
                .mapToInt(TileInterface::getId)
                .toArray();

        return TileMapTest.createTileArrayFromIntegerArray(TileMapTest.TILE_MAP_WIDTH_NEW, TileMapTest.TILE_MAP_HEIGHT_NEW, integerArray);
    }

    private static TileWithIdAndName[] createTileArrayFromIntegerArray(int width, int height, int[] integerArray) {
        if (width * height != integerArray.length) {
            throw new IllegalArgumentException(String.format("The array length %3$d does not match the preset width (%1$d) and height (%2$d)!", width, height, integerArray.length));
        }

        TileWithIdAndName[] tileMap = new TileWithIdAndName[integerArray.length];

        for (int index = 0; index < integerArray.length; index++) {
            int id = integerArray[index];

            tileMap[index] = new TileWithIdAndName(id, TileMapTest.TILE_NAME[id]);
        }

        return tileMap;
    }
}
