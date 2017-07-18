package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils;
import weka.core.Instances;

import static ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils.entropy;


public class MeanNormalizedFeatureEntropy extends AbstractDiscretizeExtractor {
    private static final String NAME = "Mean normalized feature entropy";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected double extractInternal(Instances instances) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (isNonClassNominalAttribute(instances, i)) {
                count++;
                double[] values = instances.attributeToDoubleArray(i);
                InformationTheoreticUtils.EntropyResult result = entropy(values, instances.attribute(i).numValues());
                if (Double.isNaN(result.normalizedEntropy)) {  // todo: check this with some one
                    continue;
                }
                sum += result.normalizedEntropy;
            }
        }
        return sum / count;
    }
}
