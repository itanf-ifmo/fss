package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import weka.attributeSelection.InfoGainAttributeEval;
import ru.ifmo.ctddev.FSSARecSys.mfextraction.BaseMetaFeatureExtractor;
import weka.core.Instances;


public class MaxMutualInformation extends BaseMetaFeatureExtractor {
    private static final String NAME = "Maximum mutual information";

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
            return -Double.MAX_VALUE;
        }
        double maxMutualInformation = -Double.MAX_VALUE;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (i != instances.classIndex()) {
                try {
                    maxMutualInformation = Math.max(maxMutualInformation, infoGain.evaluateAttribute(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return maxMutualInformation;
    }
}
