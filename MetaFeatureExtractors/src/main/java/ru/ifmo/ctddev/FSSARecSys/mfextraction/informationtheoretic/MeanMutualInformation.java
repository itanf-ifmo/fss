package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Instances;
import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;


public class MeanMutualInformation extends BaseMetaFeatureExtractor {
    private static final String NAME = "Mean mutual information of class and attribute";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
        try {
            infoGain.buildEvaluator(instances);
        } catch (Exception e) {
            throw new Error(e);
        }
        double meanMutualInformation = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (i != instances.classIndex()) {
                try {
                    meanMutualInformation += infoGain.evaluateAttribute(i);
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }
        return meanMutualInformation;
    }
}
