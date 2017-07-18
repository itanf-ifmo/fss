package ru.ifmo.ctddev.FSSARecSys.recsys;

import ru.ifmo.ctddev.FSSARecSys.*;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class DistRecommender implements Recommender, AEARRComputers {
    private final double beta;
    private final String classifierName;
    private final NearestDatasetSearcher nds;

    private static final List<String> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll().stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());

    public DistRecommender(String classifierName, double beta, NearestDatasetSearcher nds) {
        this.beta = beta;
        this.classifierName = classifierName;
        this.nds = nds;
    }

    @Override
    public String getId() {
        return "CR[" +
                classifierName +
                ":b" +
                beta +
                ":Simple]";
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> m = new TreeMap<>();

        m.put("classifierName", classifierName);
        m.put("beta", beta);

        return m;
    }

    private List<Pair<Double, Dataset>> getWeights(List<Pair<Double, Dataset>> distances) {
        double sum = distances.stream().mapToDouble(p -> 1 / p.first).sum();
        return distances.stream().map(p -> Pair.of(Double.isInfinite(sum) ? 1 : (1 / p.first / sum), p.second)).collect(Collectors.toList());
    }

    @Override
    public List<Pair<Double, String>> recommend(Dataset dataset) {
        List<Pair<Double, Dataset>> distances = nds.search(dataset);

        Map<String, Double> map = new HashMap<>();

        for (Pair<Double, Dataset> wp : getWeights(distances)) {
            for (Pair<Double, ClassificationResult> fp : aearrAndMLResults(fssas, wp.second, classifierName, beta)) {
                map.put(fp.second.getSelector(), map.getOrDefault(fp.second.getSelector(), 0.) + wp.first * fp.first);
            }
        }

        return map
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getValue(), e.getKey()))
                .sorted(Comparator.comparingDouble(a -> -a.first))
                .collect(Collectors.toList());
    }
}
