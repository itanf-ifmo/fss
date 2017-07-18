package ru.ifmo.ctddev.FSSARecSys.recsys;

import ru.ifmo.ctddev.FSSARecSys.AEARRComputers;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class DistRecommender2 implements Recommender, AEARRComputers {
    private final double beta;
    private final String classifierName;
    private final NearestDatasetSearcher nds;

    private static final List<String> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll().stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());

    public DistRecommender2(String classifierName, double beta, NearestDatasetSearcher nds) {
        this.beta = beta;
        this.classifierName = classifierName;
        this.nds = nds;
    }

    static public List<Pair<Double, Dataset>> distToWeights(List<Pair<Double, Dataset>> distances) {
        double sum = distances.stream().mapToDouble(p -> p.first > 0 ? 1 / p.first : 0).sum();

        if (sum == 0) {
            sum = 1;
        }

        double sum2 = sum;

        return distances.stream().map(p ->
                Pair.of(p.first > 0 ? 1 / p.first / sum2 : 0, p.second)
        ).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return "CR[" +
                classifierName +
                ":b" +
                beta +
                ":d" +
                nds.getId() +
                "]";
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> m = new TreeMap<>();

        m.put("classifierName", classifierName);
        m.put("beta", beta);

        return m;
    }


    @Override
    public List<Pair<Double, String>> recommend(Dataset dataset) {
        List<Pair<Double, Dataset>> distances = distToWeights(nds.search(dataset));

        Map<String, Double> map = new HashMap<>();

        for (Pair<Double, Dataset> wp : distances) {
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
