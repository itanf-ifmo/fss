package ru.ifmo.ctddev.FSSARecSys.mfextraction.statistical;

import ru.ifmo.ctddev.FSSARecSys.utils.StatisticalUtils;
import weka.core.Instances;


public class MeanSkewness extends AbstractStatisticalExtractor {
    private static final String NAME = "Mean skewness";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        int count = 0;
        double sum = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (isNonClassNumericalAttribute(instances, i)) {
                count++;
                double[] values = instances.attributeToDoubleArray(i);
                double mean = StatisticalUtils.mean(values);
                double variance = StatisticalUtils.variance(values, mean);
                double centralMoment = StatisticalUtils.centralMoment(values, 3, mean);
                if (centralMoment == 0 && variance == 0) {  // todo: check this with some one
                    continue;
                }
                sum += centralMoment / Math.pow(variance, 1.5);
            }
        }
        return sum / count;
    }
}
