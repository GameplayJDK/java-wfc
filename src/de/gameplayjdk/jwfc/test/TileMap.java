package de.gameplayjdk.jwfc.test;

import de.gameplayjdk.jwfc.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntPredicate;

public class TileMap {

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

    private final TileInterface[] tileMap;

    private TileInterface[] tileArray;
    private double[] tileWeight;

    private int[][][] wavePropagate;

    public TileMap(int width, int height, TileInterface[] tileMap) {
        this.width = width;
        this.height = height;

        this.tileMap = tileMap;
    }

    public void analyze() {
        this.createTileArray();
        this.createTileWeight();

        this.createWavePropagate();
    }

    public TileInterface[] generate(int width, int height) {
        return this.generate(width, height, false);
    }

    public TileInterface[] generate(int width, int height, boolean periodic) {
        Random random = new Random();
        long seed = random.nextLong();

        Model model = new Model(width, height);
        model.setup(this.tileArray.length, this.tileWeight, this.wavePropagate, periodic);

        if (model.run(seed, 0)) {
            int[] result = model.getWaveObserve();

            TileInterface[] tileMap = new TileInterface[result.length];

            for (int index = 0; index < result.length; index++) {
                int id = result[index];
                id = this.getExternalTileId(id);

                tileMap[index] = this.tileArray[id];
            }

            return tileMap;
        }

        return null;
    }

    private void createTileArray() {
        List<TileInterface> tileList = new ArrayList<TileInterface>();

        for (TileInterface tile : this.tileMap) {
            if (tileList.contains(tile)) {
                continue;
            }

            tileList.add(tile);
        }

        TileInterface[] tileArray = new TileInterface[tileList.size()];

        this.tileArray = tileList.toArray(tileArray);
    }

    private void createTileWeight() {
        double[] tileWeight = new double[this.tileArray.length];

        for (int index = 0; index < this.tileArray.length; index++) {
            tileWeight[index]++;
        }

        this.tileWeight = tileWeight;
    }

    private void createWavePropagate() {
        int[][][] wavePropagate = new int[4][this.tileArray.length][this.tileArray.length];

        // Fill with -1, which means no id. The id can normally never be negative, as it should resemble the index of a tile.
        for (int[][] wavePropagateTile : wavePropagate) {
            for (int[] wavePropagateTileAllow : wavePropagateTile) {
                Arrays.fill(wavePropagateTileAllow, -1);
            }
        }

        // Find the neighboring tile of the current tile and add that to the whitelist.
        for (int neighbor = 0; neighbor < wavePropagate.length; neighbor++) {
            for (int mapX = 0; mapX < this.width; mapX++) {
                for (int mapY = 0; mapY < this.height; mapY++) {
                    int mapXNext = mapX + TileMap.DIRECTION_X[neighbor];
                    int mapYNext = mapY + TileMap.DIRECTION_Y[neighbor];

                    if (mapXNext < 0 || mapYNext < 0 || mapXNext >= this.width || mapYNext >= this.height) {
                        continue;
                    }

                    int tileId = this.getInternalTileIdAtPosition(mapX, mapY);
                    int tileIdNext = this.getInternalTileIdAtPosition(mapXNext, mapYNext);

                    wavePropagate[neighbor][tileId][tileIdNext] = tileIdNext;
                }
            }
        }

        // Remove every id which that is negative.
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

        this.wavePropagate = wavePropagate;
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
