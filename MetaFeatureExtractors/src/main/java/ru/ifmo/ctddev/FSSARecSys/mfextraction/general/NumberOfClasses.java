package ru.ifmo.ctddev.FSSARecSys.mfextraction.general;

import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;
import weka.core.Instances;


public class NumberOfClasses extends BaseMetaFeatureExtractor {
    private static final String NAME = "Number of classes";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        if (instances.classIndex() >= 0) {
            return instances.numClasses();
        }
        return 0;
    }
}
