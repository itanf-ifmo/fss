package ru.ifmo.ctddev.FSSARecSys.utils;

import weka.core.ContingencyTables;

import static ru.ifmo.ctddev.FSSARecSys.utils.StatisticalUtils.isCorrectValue;


@SuppressWarnings("unused")
public class InformationTheoreticUtils {
    private InformationTheoreticUtils() {
    }

    public static EntropyResult entropy(double[] values, int numValues) {
        double[] distribution = new double[numValues];
        int count = 0;
        for (double v : values) {
            if (isCorrectValue(v)) {
                distribution[(int) v]++;
                count++;
            }
        }
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] /= count;
        }
        return new EntropyResult(ContingencyTables.entropy(distribution), count);
    }

    public static class EntropyResult {

        private static final double LOG_2 = Math.log(2);

        public final double entropy;
        public final double normalizedEntropy;

        public EntropyResult(double entropy, int count) {
            this.entropy = entropy;
            this.normalizedEntropy = entropy / (Math.log(count) / LOG_2);
        }

        public EntropyResult(double entropy, double normalizedEntropy) {
            this.entropy = entropy;
            this.normalizedEntropy = normalizedEntropy;
        }
    }

}
