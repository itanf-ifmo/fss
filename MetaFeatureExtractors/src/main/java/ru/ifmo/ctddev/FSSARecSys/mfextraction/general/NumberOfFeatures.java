package ru.ifmo.ctddev.FSSARecSys.mfextraction.general;

import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;
import weka.core.Instances;


public class NumberOfFeatures extends BaseMetaFeatureExtractor {
    private static final String NAME = "Number of features";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        return instances.classIndex() >= 0 ? instances.numAttributes() - 1 : instances.numAttributes();
    }
}
