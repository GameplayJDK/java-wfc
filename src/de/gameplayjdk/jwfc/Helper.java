package de.gameplayjdk.jwfc;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public final class Helper {

    /**
     * Find the index of the element which is the first one greater than the given value after accumulation of the
     * previous values.
     *
     * @param array
     * @param random
     * @return
     */
    public static int getNextIndexCloseToDouble(double[] array, final double random) {
        final double sum = Arrays.stream(array)
                .sum();

        final double[] arrayFinal = Arrays.stream(array)
                .map(new DoubleUnaryOperator() {
                    @Override
                    public double applyAsDouble(double operand) {
                        return operand / sum;
                    }
                })
                .toArray();

        return IntStream.range(0, array.length)
                .filter(new IntPredicate() {
                    private double accumulator = 0.0D;

                    @Override
                    public boolean test(int value) {
                        this.accumulator += arrayFinal[value];

                        return this.accumulator >= random;
                    }
                })
                .findFirst()
                .orElse(0);
    }
}
