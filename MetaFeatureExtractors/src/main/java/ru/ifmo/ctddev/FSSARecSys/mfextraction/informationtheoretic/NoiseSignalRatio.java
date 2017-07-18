package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils;
import weka.core.Instances;

import static ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils.entropy;


public class NoiseSignalRatio extends AbstractDiscretizeExtractor {
    private static final String NAME = "Noise-signal ratio";

    private double meanMutualInformation;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extract(Instances instances) {
        meanMutualInformation = new MeanMutualInformation().extract(instances);
        return super.extract(instances);
    }

    @Override
    protected double extractInternal(Instances instances) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (isNonClassNominalAttribute(instances, i)) {
                count++;
                double[] values = instances.attributeToDoubleArray(i);
                InformationTheoreticUtils.EntropyResult result = entropy(values, instances.attribute(i).numValues());
                if (Double.isNaN(result.entropy)) {  // todo: check this with some one
                    continue;
                }

                sum += result.entropy;
            }
        }
        double meanEntropy = sum / count;
        double result = (meanEntropy - meanMutualInformation) / meanMutualInformation;
        return Double.isNaN(result) ? -1 : result;
    }
}
