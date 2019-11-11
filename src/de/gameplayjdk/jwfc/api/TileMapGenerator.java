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

import de.gameplayjdk.jwfc.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntPredicate;

public class TileMapGenerator implements TileMapGeneratorInterface {

    private static final int[] DIRECTION_X = {
            -1,
            0,
            1,
            0,
    };
    private static final int[] DIRECTION_Y = {
            0,
            1,
            0,
            -1,
    };

    private final int width;
    private final int height;

    private TileInterface[] tileMap;

    private TileInterface[] tileArray;
    private double[] tileWeight;

    private int[][][] wavePropagate;

    public TileMapGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public TileMapGenerator(int width, int height, TileInterface[] tileMap) {
        this(width, height);

        this.tileMap = tileMap;
    }

    private void analyze() {
        // Check whether the width and height match the tilemap length and throw an exception if that is not the case.
        if (this.width * this.height != this.tileMap.length) {
            throw new IllegalArgumentException("The array length does not match the preset width and height!");
        }

        // Set up a unique tile array.
        this.tileArray = this.createTileArray();
        // Calculate the weight of each tile.
        this.tileWeight = this.createTileWeight();

        // Generate a wave propagate.
        this.wavePropagate = this.createWavePropagate();
    }

    public void analyze(TileInterface[] tileMap) {
        // Set the input tilemap.
        this.tileMap = tileMap;

        // Call the internal analyze method to do the actual analysis.
        this.analyze();
    }

    public TileInterface[] generate(int width, int height) {
        // Shortcut method for calling generate() with periodic set to false.
        return this.generate(width, height, false);
    }

    public TileInterface[] generate(int width, int height, boolean periodic) {
        // Generate a random seed.
        Random random = new Random();
        long seed = random.nextLong();

        // Create and setup a new model with the input width and height
        Model model = new Model(width, height);
        model.setup(this.tileArray.length, this.tileWeight, this.wavePropagate, periodic);

        // Run the model using the generated seed and with limit set to zero, which basically means without limit.
        if (model.run(seed, 0)) {
            // On success, retrieve the observed wave map.
            int[] result = model.getWaveObserve();

            // Then convert it from an integer array to an object array.
            TileInterface[] tileMap = new TileInterface[result.length];

            for (int index = 0; index < result.length; index++) {
                int id = result[index];
                id = this.getExternalTileId(id);

                // Set the tile at that index based on the integer value from the output. Notice, that some objects are
                // in the array more than once, as they are all taken from the previously created distinct object array.
                tileMap[index] = this.tileArray[id];
            }

            // Return the generated map.
            return tileMap;
        }

        // On failure, return null.
        return null;
    }

    private TileInterface[] createTileArray() {
        List<TileInterface> tileList = new ArrayList<TileInterface>();

        for (TileInterface tile : this.tileMap) {
            if (tileList.contains(tile)) {
                continue;
            }

            tileList.add(tile);
        }

        TileInterface[] tileArray = new TileInterface[tileList.size()];

        return tileList.toArray(tileArray);
    }

    private double[] createTileWeight() {
        double[] tileWeight = new double[this.tileArray.length];

        for (int index = 0; index < this.tileArray.length; index++) {
            tileWeight[index]++;
        }

        return tileWeight;
    }

    private int[][][] createWavePropagate() {
        int[][][] wavePropagate = new int[4][this.tileArray.length][this.tileArray.length];

        // Fill the array with -1, which means no id. The id can normally never be negative, as it should resemble the
        // index of a tile.
        for (int[][] wavePropagateTile : wavePropagate) {
            for (int[] wavePropagateTileAllow : wavePropagateTile) {
                Arrays.fill(wavePropagateTileAllow, -1);
            }
        }

        // Find the neighboring tile of the current tile and add that to the whitelist for each tile and neighbor.
        for (int neighbor = 0; neighbor < wavePropagate.length; neighbor++) {
            for (int mapX = 0; mapX < this.width; mapX++) {
                for (int mapY = 0; mapY < this.height; mapY++) {
                    int mapXNext = mapX + TileMapGenerator.DIRECTION_X[neighbor];
                    int mapYNext = mapY + TileMapGenerator.DIRECTION_Y[neighbor];

                    if (mapXNext < 0 || mapYNext < 0 || mapXNext >= this.width || mapYNext >= this.height) {
                        continue;
                    }

                    int tileId = this.getInternalTileIdAtPosition(mapX, mapY);
                    int tileIdNext = this.getInternalTileIdAtPosition(mapXNext, mapYNext);

                    wavePropagate[neighbor][tileId][tileIdNext] = tileIdNext;
                }
            }
        }

        // Remove every id that is negative.
        for (int neighbor = 0; neighbor < wavePropagate.length; neighbor++) {
            for (int tile = 0; tile < wavePropagate[neighbor].length; tile++) {
                wavePropagate[neighbor][tile] = Arrays.stream(wavePropagate[neighbor][tile])
                        .filter(new IntPredicate() {
                            @Override
                            public boolean test(int value) {
                                return value > -1;
                            }
                        })
                        .toArray();
            }
        }

        return wavePropagate;
    }

    private int getInternalTileIdAtPosition(int mapX, int mapY) {
        int index = (mapY * this.width) + mapX;

        if (index < 0 || index >= this.tileMap.length) {
            return -1;
        }

        TileInterface tile = this.tileMap[index];

        return this.getInternalTileId(tile.getId());
    }

    private int getInternalTileId(int id) {
        for (int index = 0; index < this.tileArray.length; index++) {
            if (this.tileArray[index].getId() != id) {
                continue;
            }

            return index;
        }

        return -1;
    }

    private int getExternalTileId(int id) {
        if (id < this.tileArray.length) {
            return this.tileArray[id].getId();
        }

        return -1;
    }
}
