package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils;
import weka.core.Instances;

import static ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils.entropy;


public class NormalizedClassEntropy extends AbstractDiscretizeExtractor {
    private static final String NAME = "Normalized class entropy";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected double extractInternal(Instances instances) {
        int classIndex = instances.classIndex();
        if (classIndex < 0) {
            throw new IllegalArgumentException("dataset hasn't class attribute");
        }
        double[] values = instances.attributeToDoubleArray(classIndex);
        InformationTheoreticUtils.EntropyResult result = entropy(values, instances.classAttribute().numValues());
        return result.normalizedEntropy;
    }
}
