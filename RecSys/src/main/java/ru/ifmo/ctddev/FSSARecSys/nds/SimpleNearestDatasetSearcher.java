package ru.ifmo.ctddev.FSSARecSys.nds;


import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseMetaFeaturesExtractor;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Class for searching nearest {@link Dataset} to given one
 */
public class SimpleNearestDatasetSearcher implements NearestDatasetSearcher {
    private final Collection<Dataset> others;
    private final Collection<String> metaFeatures;
    private final int n;
    private final Map<String, Pair<Double, Double>> minMaxValues;


    public SimpleNearestDatasetSearcher(CouchDB db, int n) {
        this.n = n;
        this.others = BaseDataset.getAll().stream().map(d -> (Dataset)d).collect(Collectors.toList());
        this.metaFeatures = BaseMetaFeaturesExtractor.getAll().keySet();;
        this.minMaxValues = db.getMinMax();
    }

    /**
     * This method counting distances between other and given {@link Dataset}.
     * Then adds Pairs of distances and DataSets to PriorityQueue
     * Finally returning top <code>n</code> of Pairs
     *
     * Note that this algorithm already exclude given dataset from collections of others.
     *
     * @return ordered Pairs of distances and {@link Dataset} truncated or padded with <code>nulls</code>
     * to obtain the specified length
     */
    public List<Pair<Double, Dataset>> search(Dataset dataset) {
        return others
                .stream()
                .filter(otherDataSet -> !otherDataSet.equals(dataset))
                .map(otherDataset -> Pair.of(
                        calculate(dataset, otherDataset),
                        otherDataset
                ))
                .sorted(Comparator.comparingDouble(a -> a.first))
                .limit(n)
                .collect(Collectors.toList());
    }

    private double calculate(Dataset dataSetFirst, Dataset dataSetSecond) {
        double distance = 0;
        for (String metaFeatureName : metaFeatures) {
            double metaFeatureScatter = minMaxValues.get(metaFeatureName).second - minMaxValues.get(metaFeatureName).first;

            if (metaFeatureScatter == 0) {
                continue;  // all values for this meta-feature are same across all dataset: skipping
            }

            distance += Math.abs(dataSetFirst.getMetaFeatures().get(metaFeatureName) - dataSetSecond.getMetaFeatures().get(metaFeatureName)) / metaFeatureScatter;
        }

        return distance;
    }

    @Override
    public String getId() {
        return String.format("S-NDS-%d", n);
    }
}
