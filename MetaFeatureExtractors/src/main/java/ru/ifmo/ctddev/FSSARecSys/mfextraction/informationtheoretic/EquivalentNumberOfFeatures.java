package ru.ifmo.ctddev.FSSARecSys.mfextraction.informationtheoretic;

import ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils;
import weka.core.Instances;

import static ru.ifmo.ctddev.FSSARecSys.utils.InformationTheoreticUtils.entropy;


public class EquivalentNumberOfFeatures extends AbstractDiscretizeExtractor {
    private static final String NAME = "Equivalent number of features";

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
        int classIndex = instances.classIndex();
        if (classIndex < 0) {
            throw new IllegalArgumentException("dataset hasn't class attribute");
        }
        double[] values = instances.attributeToDoubleArray(classIndex);
        InformationTheoreticUtils.EntropyResult result = entropy(values, instances.classAttribute().numValues());
        double r = result.entropy / meanMutualInformation;
        return Double.isInfinite(r) ? -1 : r;
    }
}
