package ru.ifmo.ctddev.FSSARecSys.mfextraction.general;

import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;
import weka.core.Instances;


public class DataSetDimensionality extends BaseMetaFeatureExtractor {
    private static final String NAME = "Data set dimensionality";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        int instanceNumber = instances.numInstances();
        int attributeNumber = instances.classIndex() >= 0 ? instances.numAttributes() - 1 : instances.numAttributes();
        return (double) instanceNumber / attributeNumber;
    }
}
