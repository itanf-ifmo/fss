package ru.ifmo.ctddev.FSSARecSys.recsys;


import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * The Binary Pairwise Classification Approach
 * 3.1.2 of metalearning_qs_thesis.pdf
 */
public class BPCARecommenderWeighted extends BPCARecommender {
    private final double degree;

    @SuppressWarnings({"unused", "WeakerAccess"})
    public BPCARecommenderWeighted(String metaLearningAlgorithmName, double beta, String classifierClassName, String classifierOptions, double degree, CouchDB db) {
        super(metaLearningAlgorithmName, beta, classifierClassName, classifierOptions, db);
        this.degree = degree;
    }

    @SuppressWarnings("unused")
    public BPCARecommenderWeighted(String metaLearningAlgorithmName, double beta, String classifierClassName, String classifierOptions, CouchDB db) {
        this(metaLearningAlgorithmName, beta, classifierClassName, classifierOptions, 1.0, db);
    }

    public BPCARecommenderWeighted(String metaLearningAlgorithmName, double beta, String classifierClassName, CouchDB db) {
        this(metaLearningAlgorithmName, beta, classifierClassName, "", db);
    }

    @Override
    public String getId() {
        return "BPCA-W[" +
                classifierClassName.substring(classifierClassName.lastIndexOf('.') + 1) +
                ":m" +
                metaLearningAlgorithmName +
                ":b" +
                beta +
                ":d" +
                degree +
                ":o" +
                String.join("_", classifierOptions) +
                "]";
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> m = super.getOptions();
        m.put("degree", degree);
        return m;
    }

    @Override
    public List<Pair<Double, String>> recommend(Dataset dataset) {
        List<String> FSSAs = BaseFeatureSubsetSelectionAlgorithm.getAll(db).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());

        double[] counter = new double[FSSAs.size()];

        for (int i = 0; i < FSSAs.size() - 1; i++) {
            String fssAlgorithmNameA = FSSAs.get(i);
            for (int j = i + 1; j < FSSAs.size(); j++) {
                String fssAlgorithmNameB = FSSAs.get(j);
                Pair<Instances, Instance> pair = buildInstances(dataset, fssAlgorithmNameA, fssAlgorithmNameB);
                double[] results = classifyToMany(pair.first, pair.second);
                counter[i] += Math.pow(results[0], degree);
                counter[j] += Math.pow(results[1], degree);
            }
        }

        return IntStream.range(0, FSSAs.size()).mapToObj(i -> Pair.of(counter[i], FSSAs.get(i))).sorted(Comparator.comparingDouble(a -> -a.first)).collect(Collectors.toList());
    }

    private double[] classifyToMany(Instances trainSet, Instance testInstance) {
        try {
            AbstractClassifier classifier = ClassUtils.load(AbstractClassifier.class, classifierClassName);
            classifier.setOptions(classifierOptions.clone());
            classifier.buildClassifier(trainSet);
            return classifier.distributionForInstance(testInstance);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
