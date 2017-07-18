package ru.ifmo.ctddev.FSSARecSys.interfaces;

import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Collection;
import java.util.Map;


/**
 * This interface provide {@link Dataset} metric computer
 */
public interface DatasetDistancesComputer {
    /**
     * @return Distance between given DataSets using only meta features with names form given list datasetMetaFeatureNames
     */
    double calculate(Dataset dataSetFirst, Dataset dataSetSecond, Collection<String> datasetMetaFeatureNames, Map<String, Pair<Double, Double>> minMaxValues);
}
