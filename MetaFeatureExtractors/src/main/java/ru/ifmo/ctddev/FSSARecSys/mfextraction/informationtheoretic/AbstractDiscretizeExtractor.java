package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;


public abstract class AbstractDiscretizeExtractor extends BaseMetaFeatureExtractor {
    @Override
    public double extract(Instances instances) {
        Discretize discretize = new Discretize();
        discretize.setUseBetterEncoding(true);
        try {
            discretize.setInputFormat(instances);
            instances = Filter.useFilter(instances, discretize);
            return extractInternal(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected boolean isNonClassNominalAttribute(Instances instances, int attributeIndex) {
        return isNonClassAttributeWithType(instances, attributeIndex, Attribute.NOMINAL);
    }

    protected abstract double extractInternal(Instances instances);
}
