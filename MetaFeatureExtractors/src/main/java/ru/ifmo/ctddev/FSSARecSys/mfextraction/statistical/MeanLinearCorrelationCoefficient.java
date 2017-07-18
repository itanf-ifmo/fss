package ru.ifmo.ctddev.FSSARecSys.mfextraction.statistical;

import ru.ifmo.ctddev.FSSARecSys.utils.StatisticalUtils;
import weka.core.Instances;


public class MeanLinearCorrelationCoefficient extends AbstractStatisticalExtractor {
    private static final String NAME = "Mean absolute linear correlation coefficient of all possible pairs of features";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (isNonClassNumericalAttribute(instances, i)) {
                double[] values1 = instances.attributeToDoubleArray(i);
                for (int j = i + 1; j < instances.numAttributes(); j++) {
                    if (isNonClassNumericalAttribute(instances, j)) {
                        double[] values2 = instances.attributeToDoubleArray(j);
                        double stat = StatisticalUtils.linearCorrelationCoefficient(values1, values2);
                        count++;
                        if (Double.isNaN(stat)) {  // todo: check this with some one
                            continue;
                        }

                        sum += stat;
                    }
                }
            }
        }

        double result = sum / count;
        return Double.isNaN(result) ? -1 : result;
    }
}
