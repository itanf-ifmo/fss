package ru.ifmo.ctddev.FSSARecSys.mfextraction;

import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaFeatureExtractor;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.Arrays;


public abstract class BaseMetaFeatureExtractor implements MetaFeatureExtractor {
    public abstract double extract(Instances instances);

    public double extract(Dataset dataset) {
        return extract(dataset.getInstances());
    }

    protected boolean isNonClassAttributeWithType(Instances instances, int attributeIndex, int... types) {
        Attribute attribute = instances.attribute(attributeIndex);
        int type = attribute.type();
        return attributeIndex != instances.classIndex() && Arrays.stream(types).anyMatch(t -> t == type);
    }

    @Override
    public String toString() {
        return String.format("<MFExtractor[%s]>", getName());
    }
}
