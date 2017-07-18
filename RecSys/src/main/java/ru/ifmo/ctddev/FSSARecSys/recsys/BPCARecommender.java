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


/**
 * The Binary Pairwise Classification Approach
 * 3.1.2 of metalearning_qs_thesis.pdf
 */
public class BPCARecommender implements Recommender, AEARRComputers {
    protected final String metaLearningAlgorithmName;
    protected final double beta;
    protected final CouchDB db;
    protected final List<Dataset> datasets;
    private final List<String> metaFeatures;
    @SuppressWarnings("WeakerAccess")
    protected final String classifierClassName;
    @SuppressWarnings("WeakerAccess")
    protected final String[] classifierOptions;

    @Override
    public String getId() {
        return "BPCA[" +
                classifierClassName.substring(classifierClassName.lastIndexOf('.') + 1) +
                ":m" +
                metaLearningAlgorithmName +
                ":b" +
                beta +
                ":o" +
                String.join("_", classifierOptions) +
                "]";
    }

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
        m.put("classifier", classifierClassName);
        m.put("MLAlg", metaLearningAlgorithmName);
        m.put("classifierOptions", optionsMap);

        return m;
    }

    @SuppressWarnings("WeakerAccess")
    public BPCARecommender(String metaLearningAlgorithmName, double beta, String classifierClassName, String classifierOptions, CouchDB db) {
        this.metaLearningAlgorithmName = metaLearningAlgorithmName;
        this.beta = beta;
        this.metaFeatures = new ArrayList<>(BaseMetaFeaturesExtractor.getAll(db).keySet());
        Collections.reverse(this.metaFeatures);
        this.datasets = BaseDataset.getAll(db).stream().map(d -> (Dataset)d).collect(Collectors.toList());
        this.db = db;
        this.classifierClassName = classifierClassName;
        this.classifierOptions = classifierOptions.split(" ");
    }

    public BPCARecommender(String metaLearningAlgorithmName, double beta, String classifierClassName, CouchDB db) {
        this(metaLearningAlgorithmName, beta, classifierClassName, "", db);
    }

    @Override
    public List<Pair<Double, String>> recommend(Dataset dataset) {
        List<String> FSSAs = BaseFeatureSubsetSelectionAlgorithm.getAll(db).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());

        double[] counter = new double[FSSAs.size()];

        for (int i = 0; i < FSSAs.size() - 1; i++) {
            String fssAlgorithmNameA = FSSAs.get(i);
            for (int j = i + 1; j < FSSAs.size(); j++) {
                String fssAlgorithmNameB = FSSAs.get(j);

                Pair<Instances, Instance> pair = buildInstances(dataset,fssAlgorithmNameA, fssAlgorithmNameB);
                String selected = classify(pair.first, pair.second);

                if (selected.equals(fssAlgorithmNameA)) {
                    counter[i]++;
                } else {
                    counter[j]++;
                }
            }
        }

        return IntStream.range(0, FSSAs.size()).mapToObj(i -> Pair.of(counter[i], FSSAs.get(i))).sorted(Comparator.comparingDouble(a -> -a.first)).collect(Collectors.toList());
    }

    @SuppressWarnings("WeakerAccess")
    protected Pair<Instances, Instance> buildInstances(Dataset dataset, String fssAlgorithmNameA, String fssAlgorithmNameB) {
        ArrayList<Attribute> atts = new ArrayList<>();
        metaFeatures.stream().map(Attribute::new).forEach(atts::add);

        ArrayList<String> classVal = new ArrayList<>();
        classVal.add(fssAlgorithmNameA);
        classVal.add(fssAlgorithmNameB);
        atts.add(new Attribute("@@class@@", classVal));

        Instances trainSet = new Instances("BPCANearestDatasetSearcher", atts, 0);
        trainSet.setClassIndex(trainSet.numAttributes() - 1);

        for (Dataset other : datasets) {
            if (other.equals(dataset)) {
                continue;
            }
            double scoreA = aearr(other, (ClassificationResult)other.getResult(fssAlgorithmNameA).getResult(metaLearningAlgorithmName), beta);
            double scoreB = aearr(other, (ClassificationResult)other.getResult(fssAlgorithmNameB).getResult(metaLearningAlgorithmName), beta);

            trainSet.add(buildInstance(other, metaFeatures, (scoreA > scoreB ? 0 : 1)));
        }

        Instances dataUnlabeled = new Instances("TestInstances", atts, 0);
        dataUnlabeled.add(buildInstance(dataset, metaFeatures, -1.0));
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        return Pair.of(trainSet, dataUnlabeled.firstInstance());
    }

    private String classify(Instances trainSet, Instance testInstance) {
        try {
            AbstractClassifier classifier = ClassUtils.load(AbstractClassifier.class, this.classifierClassName);
            classifier.setOptions(classifierOptions.clone());
            classifier.buildClassifier(trainSet);

            int c = (int) classifier.classifyInstance(testInstance);
            return testInstance.classAttribute().value(c);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private Instance buildInstance(Dataset dataset, Collection<String> metaFeatures, double classFeature) {
        Map<String, Double> mf = dataset.getMetaFeatures();
        double[] features = metaFeatures.stream().mapToDouble(mf::get).toArray();
        features = Arrays.copyOf(features, features.length + 1);
        features[features.length - 1] = classFeature;
        return new DenseInstance(1.0, features);
    }
}
