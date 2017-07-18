package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DatasetDistancesComputer;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Collection;
import java.util.Map;


/**
 * This class compute distance between {@link Dataset}s using L1 distance between dataset's meta-features vector.
 */
public class L1DatasetDistancesComputer implements DatasetDistancesComputer {
    /**
     * @return Distance between given DataSets using only meta features with names form given list datasetMetaFeatureNames
     */
    public double calculate(Dataset dataSetFirst, Dataset dataSetSecond, Collection<String> datasetMetaFeatureNames, Map<String, Pair<Double, Double>> minMaxValues) {
        double distance = 0;
        for (String metaFeatureName : datasetMetaFeatureNames) {
            double metaFeatureScatter = minMaxValues.get(metaFeatureName).second - minMaxValues.get(metaFeatureName).first;

            if (metaFeatureScatter == 0) {
                continue;  // all values for this meta-feature are same across all dataset: skipping
            }

            distance += Math.abs(dataSetFirst.getMetaFeatures().get(metaFeatureName) - dataSetSecond.getMetaFeatures().get(metaFeatureName)) / metaFeatureScatter;
        }

        return distance;  // #todo: should we normalize this value by division by numberOfMetaFeatures?
    }
}
