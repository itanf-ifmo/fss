package ru.ifmo.ctddev.FSSARecSys.mfextraction.statistical;

import weka.core.Attribute;
import weka.core.Instances;
import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;


public abstract class AbstractStatisticalExtractor extends BaseMetaFeatureExtractor {
    protected boolean isNonClassNumericalAttribute(Instances instances, int attributeIndex) {
        return isNonClassAttributeWithType(instances, attributeIndex, Attribute.NOMINAL, Attribute.NUMERIC);
    }
}
