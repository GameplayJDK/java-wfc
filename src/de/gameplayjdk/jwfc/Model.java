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

package de.gameplayjdk.jwfc;

import java.util.Random;

public class Model {

    // The mapping of index to opposite index for each side index.
    private static final int[] OPPOSITE = {
            2,
            3,
            0,
            1,
    };

    // The direction to move for what side index in x direction.
    private static final int[] DIRECTION_X = {
            -1,
            0,
            1,
            0,
    };

    // The direction to move for what side index in y direction.
    private static final int[] DIRECTION_Y = {
            0,
            1,
            0,
            -1,
    };

    // The output wave map size.
    private final int width;
    private final int height;

    // The tile count.
    private int count;

    // The wave map.
    private boolean[][] wave;

    // The wave map observed successfully.
    private int[] waveObserve;

    // The compatibility of each wave map field by tile to the neighboring wave map field.
    private int[][][] compatible;

    // The possible tile by direction and current tile. It provides the following data: What tiles appear next to what
    // tiles, on which sides. E.g.: int[4][this.count][N]; N = 0 < N < count; N = "set of possible tile".
    private int[][][] wavePropagate;

    // The weight of each tile.
    private double[] weight;
    private double[] weightLogarithm;

    private double sumWeightAll;
    private double sumWeightAllLogarithm;

    // The entropy start value.
    private double entropyStart;

    // The available tile options for each wave map field.
    private int[] sumOne;

    private double[] sumWeight;
    private double[] sumWeightLogarithm;

    // The entropy value of each wave map field.
    private double[] entropy;

    // The record of wave map field and banned tile.
    private int[][] stack;
    // The current size of the stack. This way the stack can keep its fixed size.
    private int stackSize;

    // The option of whether to produce periodic output or not.
    private boolean periodic;

    public Model(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setup(int count, double[] weight, int[][][] wavePropagate, boolean periodic) {
        this.count = count;

        this.weight = weight;

        this.wavePropagate = wavePropagate;

        this.periodic = periodic;
    }

    private void initialize() {
        //this.count = ?;

        this.wave = new boolean[this.width * this.height][this.count];

        this.waveObserve = null;

        this.compatible = new int[this.wave.length][this.count][4];

        //this.wavePropagate = new int[4][this.count][this.count];

        //this.weight = new double[this.count];
        this.weightLogarithm = new double[this.count];

        this.sumWeightAll = 0.0D;
        this.sumWeightAllLogarithm = 0.0D;

        for (int tile = 0; tile < this.count; tile++) {
            this.weightLogarithm[tile] = this.weight[tile] * Math.log(this.weight[tile]);

            this.sumWeightAll += this.weight[tile];
            this.sumWeightAllLogarithm += this.weightLogarithm[tile];
        }

        this.entropyStart = this.calculateEntropy(this.sumWeightAll, this.sumWeightAllLogarithm);

        this.sumOne = new int[this.wave.length];
        this.sumWeight = new double[this.wave.length];
        this.sumWeightLogarithm = new double[this.wave.length];
        this.entropy = new double[this.wave.length];

        this.stack = new int[this.wave.length * this.count][2];
        this.stackSize = 0;

        //this.periodic = false;
    }

    private boolean observe(Random random) {
        this.waveObserve = null;

        // The minimum entropy value (0.001).
        double minimum = 1E+3D;
        // The index of the wave map field with the lowest value below the minimum entropy.
        int minimumFieldIndex = -1;

        // Iterate the wave map fields.
        for (int field = 0; field < this.wave.length; field++) {
            // Skip, if no longer inside the map boundary. The arguments resemble the x and y value inside of the wave
            // map for the current field.
            if (this.isOutOfBoundary(field % this.width, (field - (field % this.width)) / this.width)) {
                continue;
            }

            // The amount of available tile options left for the current wave map field.
            int amount = this.sumOne[field];

            // If there is a wave map field without any possible options left, the generation has failed. Thus break
            // early by returning false.
            if (0 == amount) {
                return false;
            }

            // The entropy of the current wave map field.
            double entropy = this.entropy[field];

            // If the amount is negative () and the entropy of that field is below or equal the minimum threshold,
            if (1 < amount && entropy <= minimum) {
                double noise = random.nextDouble();

                // Add a little random noise upon it, and check if it is still below the minimum threshold,
                if ((entropy + noise) < minimum) {
                    // If that is the case, set the minimum threshold to the value,
                    minimum = entropy + noise;

                    // And also set the field index.
                    minimumFieldIndex = field;
                }
            }
        }

        // If there was no entropy value below the initial minimal threshold, the observation was successful. Thus save
        // the observed wave and return true.
        if (-1 == minimumFieldIndex) {
            // Initialize the observed wave map.
            this.waveObserve = new int[this.wave.length];

            // Copy the wave map to the observed wave map. Iterate the wave map fields
            for (int i = 0; i < this.wave.length; i++) {
                for (int tile = 0; tile < this.count; tile++) {
                    // Take the first possible tile from that wave map field,
                    if (this.wave[i][tile]) {
                        // Set the tile for the observed wave map,
                        this.waveObserve[i] = tile;

                        // And break after that.
                        break;
                    }
                }
            }

            return true;
        }

        // Else, create a new array with its size based on the tile count.
        double[] waveFieldWeight = new double[this.count];

        // Go though all available tiles for the element with the lowest entropy value below the minimum threshold and
        // copy their tile weight.
        for (int tile = 0; tile < this.count; tile++) {
            if (this.wave[minimumFieldIndex][tile]) {
                waveFieldWeight[tile] = this.weight[tile];
            } else {
                waveFieldWeight[tile] = 0.0D;
            }
        }

        // Get the index of the first element of the array that is greater than the supplied random value or the first
        // element if there is no one.
        int index = Helper.getNextIndexCloseToDouble(waveFieldWeight, random.nextDouble());

        // Get a reference the wave map field with the lowest entropy value below the minimum threshold, negotiated
        // before.
        boolean[] waveField = this.wave[minimumFieldIndex];

        // Go through all tiles for the selected wave map field,
        for (int tile = 0; tile < this.count; tile++) {
            // Check if
            //  either the tile has the previously selected index and is already banned
            //  or the tile has a different index than the previously selected one and is not yet banned
            if ((tile == index) != waveField[tile]) {
                // And ban it for that wave map field.
                // In other words, ban every tile but the one with greater weight than the random probability which is
                // not yet banned.
                this.ban(minimumFieldIndex, tile);
            }
        }

        // If true is returned, check in run for this.waveObserve being null to distinguish between the two return
        // points of it.
        return true;
    }

    private void ban(int fieldIndex, int tile) {
        // Disable the given tile for the given wave map field.
        this.wave[fieldIndex][tile] = false;

        // Grab a reference of the compatibility information to the neighboring wave map fields by tile.
        int[] compatible = this.compatible[fieldIndex][tile];

        // Go through the compatibility information about the 4 neighboring wave map fields by tile and set them to zero.
        for (int neighbor = 0; neighbor < 4; neighbor++) {
            compatible[neighbor] = 0;
        }

        // Set the ban information to the stack. The int pair represents the wave map field and the banned tile.
        this.stack[this.stackSize][0] = fieldIndex;
        this.stack[this.stackSize][1] = tile;
        this.stackSize++;

        // Subtract one from the amount of available tile options left for the current wave map field.
        this.sumOne[fieldIndex] -= 1;
        // Subtract the tile weight from the sum of all tile weights for that wave map field.
        this.sumWeight[fieldIndex] -= this.weight[tile];
        this.sumWeightLogarithm[fieldIndex] -= weightLogarithm[tile];

        // Calculate the new entropy value and set it for the current wave map field.
        //this.entropy[fieldIndex] = Math.log(sumWeight) - (this.sumWeightLogarithm[fieldIndex] / sumWeight);
        this.entropy[fieldIndex] = this.calculateEntropy(this.sumWeight[fieldIndex], this.sumWeightLogarithm[fieldIndex]);
    }

    public boolean run(long seed, int limit) {
        // If the wave map is null, this is the first run, so
        if (null == this.wave) {
            // Initialize all the variables,
            this.initialize();
        }

        // Then clear existing the data or fill it in initially.
        this.clear();

        // Create a new random generator using the provided seed.
        Random random = new Random(seed);

        for (int run = 0; (run < limit || 0 == limit); run++) {
            // Observe a new wave map.
            boolean result = this.observe(random);

            // The observed wave map is only set when the wave map was successfully observed.
            if (null != this.waveObserve) {
                return result;
            }

            // Go through the banned tiles and negotiate the compatibility.
            this.propagate();
        }

        return false;
    }

    private void propagate() {
        // Loop until the stack size is zero.
        while (0 < this.stackSize) {
            // Get the ban information from the stack. The int pair represents the wave map field and the banned tile.
            int field = this.stack[this.stackSize - 1][0];
            int tile = this.stack[this.stackSize - 1][1];
            this.stackSize--;

            // Calculate the x and y value inside of the wave map for the current field.
            int x = field % this.width;
            int y = (field - (field % this.width)) / this.width;

            // Go through the 4 direct neighbors of the current wave map field.
            for (int neighbor = 0; neighbor < 4; neighbor++) {
                // Find out how far to go into each direction for each neighboring field.
                int directionX = Model.DIRECTION_X[neighbor];
                int directionY = Model.DIRECTION_Y[neighbor];

                // Calculate the coordinates of the neighboring wave map field in that direction.
                int neighborX = x + directionX;
                int neighborY = y + directionY;

                // Skip, if no longer inside the map boundary. Note, that if the map is periodic, this will not skip.
                if (this.isOutOfBoundary(neighborX, neighborY)) {
                    continue;
                }

                // Thus, if it is periodic, this will move the position to the next wave map field on the other side.
                if (neighborX < 0) {
                    neighborX += this.width;
                } else if (neighborX >= this.width) {
                    neighborX -= this.width;
                }
                if (neighborY < 0) {
                    neighborY += this.height;
                } else if (neighborY >= this.height) {
                    neighborY -= this.height;
                }

                // Calculate the wave map field index of the neighbor.
                int fieldNeighbor = neighborX + (neighborY * this.width);

                // Compatibility in the direction (of the neighboring field) for the banned tile. The dimension now only
                // contains the tiles possible for the direction (of the neighboring field) of the banned tile.
                int[] propagate = this.wavePropagate[neighbor][tile];
                // Get the compatibility of the neighboring wave map field by tile to the neighboring wave map field.
                // The dimension now only contains the compatibility of tiles to the neighbors.
                int[][] compatible = this.compatible[fieldNeighbor];

                // Go through the possible tiles in the direction (of the neighboring field) for the banned tile.
                for (int tileNeighbor : propagate) {
                    // The tile neighbor is a possible tile in the direction (of the neighboring tile).

                    // The number of possible options left (for the neighboring field) for the tile.
                    int[] compatiblePossible = compatible[tileNeighbor];
                    compatiblePossible[neighbor]--;

                    // The above is equal to the following:
                    //compatible[propagate[index]][neighbor]--;
                    //this.compatible[fieldNeighbor][propagate[index]][neighbor]--;

                    // If there is no possible tile left (for the neighboring field), ban the tile for the neighbor.
                    if (0 == compatiblePossible[neighbor]) {
                        this.ban(fieldNeighbor, tileNeighbor);
                    }
                }
            }
        }
    }

    private void clear() {
        // Go through the wave map fields,
        for (int field = 0; field < this.wave.length; field++) {
            // Through the tile count,
            for (int tile = 0; tile < this.count; tile++) {
                // Reset all tiles for that wave map field to be possible,
                this.wave[field][tile] = true;

                // And finally go through the compatibility information for every wave map field by tile,
                for (int neighbor = 0; neighbor < 4; neighbor++) {
                    // To do... what? This creates the compatibility information from the wave propagate. It reverses
                    // the information from wave propagate.
                    this.compatible[field][tile][neighbor] = this.wavePropagate[Model.OPPOSITE[neighbor]][tile].length;
                }
            }

            // Set the amount of available tile options left for the current wave map field to the length of the weight.
            this.sumOne[field] = this.weight.length;
            // Set the weight for the current wave map field to the overall sum of weight.
            this.sumWeight[field] = this.sumWeightAll;
            this.sumWeightLogarithm[field] = this.sumWeightAllLogarithm;
            // Set the entropy for the current wave map field to the start entropy.
            this.entropy[field] = this.entropyStart;
        }
    }

    private double calculateEntropy(double sumWeight, double sumWeightLogarithm) {
        return Math.log(sumWeight) - (sumWeightLogarithm / sumWeight);
    }

    private boolean isOutOfBoundary(int x, int y) {
        return !this.periodic && (x < 0 || y < 0 || x >= this.width || y >= this.height);
    }

    public int[] getWaveObserve() {
        return this.waveObserve;
    }
}
