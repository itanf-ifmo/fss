package ru.ifmo.ctddev.FSSARecSys.nds;

import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseMetaFeaturesExtractor;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassifierDatasetSearcher implements NearestDatasetSearcher {
    private final int n;
    private final String classifierClassName;
    private final String[] classifierOptions;
    private final String[] metaFeaturesNames;
    private final List<BaseDataset> others;

    public static ClassifierDatasetSearcher SVM(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.functions.LibSVM", "-B -K 3", n);
    }

    public static ClassifierDatasetSearcher SVM2(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.functions.LibSVM", "-B", n);
    }

    public static ClassifierDatasetSearcher IBk(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.lazy.IBk", "", n);
    }

    public static ClassifierDatasetSearcher J48(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.trees.J48", "-A", n);
    }

    public static ClassifierDatasetSearcher NaiveBayes(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.bayes.NaiveBayes", "", n);
    }

    public static ClassifierDatasetSearcher BayesNet(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.bayes.BayesNet", "", n);
    }

    public static ClassifierDatasetSearcher PART(int n) {
        return new ClassifierDatasetSearcher("weka.classifiers.rules.PART", "", n);
    }

    public ClassifierDatasetSearcher(String classifierClassName, String classifierOptions, int n) {
        this.classifierClassName = classifierClassName;
        if (classifierOptions.isEmpty()) {
            this.classifierOptions = new String[0];
        } else {
            this.classifierOptions = classifierOptions.split(" ");
        }
        this.n = n;
        this.metaFeaturesNames = BaseMetaFeaturesExtractor.getAll().keySet().toArray(new String[0]);
        this.others = BaseDataset.getAll();
    }

    /**
     * @param newDataset to search near
     * @return ordered Pairs of distances and {@link Dataset}
     */
    @Override
    public List<Pair<Double, Dataset>> search(Dataset newDataset) {
        assert newDataset instanceof BaseDataset;

        double[] w = classify(buildInstances((BaseDataset) newDataset));
        return IntStream.range(0, others.size()).mapToObj(i -> Pair.of(w[i] == 0 ? Double.MAX_VALUE : 1 / w[i], (Dataset) others.get(i))).sorted(Comparator.comparingDouble(a -> a.first)).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return String.format("C-NDS<%s,%s,n:%d>", classifierClassName, String.join(",", classifierOptions), n);
    }

    private Pair<Instances, Instance> buildInstances(BaseDataset newDataset) {
        ArrayList<Attribute> atts = new ArrayList<>();

        Arrays.stream(metaFeaturesNames).map(Attribute::new).forEach(atts::add);
        atts.add(new Attribute("@@class@@", others.stream().map(Dataset::getName).collect(Collectors.toList())));

        Instances trainSet = new Instances("ClassifierNearestDatasetSearcher", atts, 0);
        trainSet.setClassIndex(trainSet.numAttributes() - 1);

        int datasetId = 0;
        for (BaseDataset other : others) {
            if (other.equals(newDataset)) {
                continue;
            }
            trainSet.add(buildInstance(other, datasetId++));
        }

        Instances dataUnlabeled = new Instances("TestInstances", atts, 0);

        dataUnlabeled.add(buildInstance(newDataset, -1));
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        return Pair.of(trainSet, dataUnlabeled.firstInstance());
    }

    private double[] classify(Pair<Instances, Instance> trainAndTest) {
        try {
            AbstractClassifier classifier = ClassUtils.load(AbstractClassifier.class, this.classifierClassName);
            classifier.setOptions(classifierOptions.clone());
            classifier.buildClassifier(trainAndTest.first);
            return classifier.distributionForInstance(trainAndTest.second);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private Instance buildInstance(BaseDataset dataset, int datasetId) {
        double[] features = dataset.getMetaFeaturesVector(metaFeaturesNames);

        // add one element for class
        features = Arrays.copyOf(features, features.length + 1);
        features[features.length - 1] = datasetId;

        return new DenseInstance(1.0, features);
    }
}
