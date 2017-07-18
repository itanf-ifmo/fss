package ru.ifmo.ctddev.FSSARecSys.recsys;

import ru.ifmo.ctddev.FSSARecSys.AEARRComputers;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class OracleRecommender implements Recommender, AEARRComputers {
    private final String metaLearningAlgorithmName;
    private final double beta;
    private final List<String> FSSAs;

    public OracleRecommender(String metaLearningAlgorithmName, double beta, CouchDB db) {
        this.metaLearningAlgorithmName = metaLearningAlgorithmName;
        this.beta = beta;
        this.FSSAs = BaseFeatureSubsetSelectionAlgorithm.getAll(db).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());
    }

    public String getId() {
        return "OR[" +
                metaLearningAlgorithmName +
                ":b" +
                beta +
                "]";
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> m = new TreeMap<>();

        m.put("MLAlg", metaLearningAlgorithmName);
        m.put("beta", beta);

        return m;
    }
    @Override
    public List<Pair<Double, String>> recommend(Dataset dataset) {
        return earr(dataset)
                .stream()
                .map(p -> Pair.of(
                        p.first,
                        p.second.getFeatureSelectionResult().getName())
                )
                .sorted(Comparator.comparingDouble(v -> -v.first))
                .collect(Collectors.toList());
    }

    private Collection<Pair<Double, ClassificationResult>> earr(Dataset dataset) {
        return earr(FSSAs.stream().map(r -> (ClassificationResult) dataset.getResult(r).getResult(metaLearningAlgorithmName)).collect(Collectors.toList()));
    }

    private List<Pair<Double, ClassificationResult>> earr(List<ClassificationResult> results) {
        long normalizationFactor = results.size() - 1;
        return results.stream().map(a -> Pair.of((results.stream().mapToDouble(b -> aearr(a, b, beta)).sum() - 1) / normalizationFactor, a)).collect(Collectors.toList());
    }
}
