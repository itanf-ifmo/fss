package ru.ifmo.ctddev.FSSARecSys.recsys;


import ru.ifmo.ctddev.FSSARecSys.*;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseMetaFeaturesExtractor;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * The Label Ranking Approach
 * 3.1.4 of metalearning_qs_thesis.pdf
 */
public class ClassifierRecommender implements Recommender, AEARRComputers {
    private final String metaLearningAlgorithmName;
    private final double beta;
    private final List<String> FSSAs;
    private final List<Dataset> datasets;
    private final List<String> metaFeatures;
    private final String classifierClassName;
    private final String[] classifierOptions;
    private final double degree;

    @SuppressWarnings("WeakerAccess")
    public ClassifierRecommender(String metaLearningAlgorithmName, double beta, String classifierClassName, String classifierOptions, CouchDB db) {
        this(metaLearningAlgorithmName, beta, classifierClassName, classifierOptions, 1.0, db);
    }

    @SuppressWarnings("WeakerAccess")
    public ClassifierRecommender(String metaLearningAlgorithmName, double beta, String classifierClassName, String classifierOptions, double degree, CouchDB db) {
        this.metaLearningAlgorithmName = metaLearningAlgorithmName;
        this.degree = degree;
        this.beta = beta;
        this.metaFeatures = new ArrayList<>(BaseMetaFeaturesExtractor.getAll(db).keySet());
        Collections.reverse(this.metaFeatures);
        this.datasets = BaseDataset.getAll(db).stream().map(d -> (Dataset)d).collect(Collectors.toList());
        this.classifierClassName = classifierClassName;
        if (classifierOptions.isEmpty()) {
            this.classifierOptions = new String[0];
        } else {
            this.classifierOptions = classifierOptions.split(" ");
        }

        this.FSSAs = BaseFeatureSubsetSelectionAlgorithm.getAll(db).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());
        Collections.reverse(this.FSSAs);
    }

    @SuppressWarnings("unused")
    public ClassifierRecommender(String metaLearningAlgorithmName, double beta, String classifierClassName, CouchDB db) {
        this(metaLearningAlgorithmName, beta, classifierClassName, "", db);
    }

    @Override
    public List<Pair<Double, String>> recommend(Dataset dataset) {
        Pair<Instances, Instance> p = buildInstances(dataset);
        double[] w = classify(p.first, p.second);
        return IntStream.range(0, FSSAs.size()).mapToObj(i -> Pair.of(w[i], FSSAs.get(i))).sorted(Comparator.comparingDouble(a -> -a.first)).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return "CR[" +
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

    @SuppressWarnings("Duplicates")
    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> m = new TreeMap<>();

        Map<String, Object> optionsMap = new TreeMap<>();

        String flag = null;
        for (String option : classifierOptions) {
            if (option.startsWith("-")) {
                if (flag == null) {
                    flag = option;
                } else {
                    optionsMap.put(flag, "");
                }
            } else {
                try {
                    optionsMap.put(flag, Integer.parseInt(option));

                } catch (NumberFormatException e) {
                    try {
                        optionsMap.put(flag, Double.parseDouble(option));

                    } catch (NumberFormatException ex) {
                        optionsMap.put(flag, option);
                    }
                }
                flag = null;
            }
        }
        if (flag != null) {
            optionsMap.put(flag, "");
        }

        m.put("beta", beta);
        m.put("classifier", classifierClassName.substring(classifierClassName.lastIndexOf('.') + 1));
        m.put("MLAlg", metaLearningAlgorithmName);
        m.put("classifierOptions", optionsMap);
        m.put("degree", degree);

        return m;
    }

    private Pair<Instances, Instance> buildInstances(Dataset dataset) {
        ArrayList<Attribute> atts = new ArrayList<>();

        metaFeatures.stream().map(Attribute::new).forEach(atts::add);

        atts.add(new Attribute("@@class@@", new ArrayList<>(FSSAs)));

        Instances trainSet = new Instances("BPCANearestDatasetSearcher", atts, 0);
        trainSet.setClassIndex(trainSet.numAttributes() - 1);

        for (Dataset other : datasets) {
            if (other.equals(dataset)) {
                continue;
            }

            List<Pair<Double, Double>> ps = IntStream
                    .range(0, FSSAs.size())
                    .mapToObj(i -> Pair.of(
                            aearr(
                                    other,
                                    (ClassificationResult)other.getResult(FSSAs.get(i)).getResult(metaLearningAlgorithmName),
                                    beta
                            ),
                            (double) i
                    ))
                    .collect(Collectors.toList());

            buildInstance(other, metaFeatures, ps).forEach(trainSet::add);
        }

        Instances dataUnlabeled = new Instances("TestInstances", atts, 0);

        dataUnlabeled.add(buildInstance(dataset, metaFeatures, Stream.of(Pair.of((double)1, (double)-1)).collect(Collectors.toList())).get(0));
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        return Pair.of(trainSet, dataUnlabeled.firstInstance());
    }

    private double[] classify(Instances trainSet, Instance testInstance) {
        try {
            AbstractClassifier classifier = ClassUtils.load(AbstractClassifier.class, this.classifierClassName);
            classifier.setOptions(classifierOptions.clone());
            classifier.buildClassifier(trainSet);
            return classifier.distributionForInstance(testInstance);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * @param classFeatures first element of the pair is the weight of the feature,
     *                      second will be number of the FSS algorithm
     */
    private ArrayList<Instance> buildInstance(Dataset dataset, Collection<String> metaFeatures, Collection<Pair<Double, Double>> classFeatures) {
        Map<String, Double> mf = dataset.getMetaFeatures();
        double[] features = metaFeatures.stream().mapToDouble(mf::get).toArray();
        features = Arrays.copyOf(features, features.length + 1);

        ArrayList<Instance> instances = new ArrayList<>();

        for (Pair<Double, Double> classFeature : classFeatures) {
            features[features.length - 1] = classFeature.second;
            instances.add(new DenseInstance(Math.pow(classFeature.first, degree), features.clone()));
        }

        return instances;
    }
}
