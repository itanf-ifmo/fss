package ru.ifmo.ctddev.FSSARecSys.mfextraction.general;

import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;
import weka.core.Instances;


public class NumberOfInstances extends BaseMetaFeatureExtractor {
    private static final String NAME = "Number of instances";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        return instances.numInstances();
    }
}
