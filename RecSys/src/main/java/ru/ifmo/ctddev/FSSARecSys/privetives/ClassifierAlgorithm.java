package ru.ifmo.ctddev.FSSARecSys.privetives;

import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaLearningAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ClassifierAlgorithm implements MetaLearningAlgorithm {
    private static Map<String, ClassifierAlgorithm> all;
    private Classifier classifier;
    private String name;
    private String taskType;
    private String options;


    @Override
    public ClassificationResult evaluate(Dataset dataset) {
        return evaluate(dataset.getInstances(), null);
    }

    @Override
    public ClassificationResult evaluate(Dataset dataset, FeatureSelectionResult featureSelectionResult) {
        return evaluate(dataset.getInstances(featureSelectionResult.getSelectedFeatures()), featureSelectionResult);
    }

    @Override
    public ClassificationResult evaluate(Dataset dataset, int[] selectedFeatures) {
        return evaluate(dataset.getInstances(selectedFeatures), null);
    }

    /**
     * Yes, really. Second argument can be null.
     */
    private ClassificationResult evaluate(Instances instances, FeatureSelectionResult featureSelectionResult) {
        Evaluation eval;
        double classificationTime;

        try {
            long startTime = System.currentTimeMillis();
            eval = new Evaluation(instances);
            eval.crossValidateModel(ClassUtils.load(Classifier.class, classifier.getClass().getCanonicalName()), instances, 10, new Random(1));
            classificationTime = ((double) System.currentTimeMillis() - startTime) / 1000;
        } catch (Exception e) {
            throw new Error(e);
        }

        ClassificationResult output = new ClassificationResult();
        output.setMetaLearningAlgorithm(this);
        output.setFeatureSelectionResult(featureSelectionResult);
        TreeMap<String, Double> map = new TreeMap<>();

        map.put(ClassificationResult.PROPERTIES.ACCURACY.toString(), eval.pctCorrect() / 100);
        map.put(ClassificationResult.PROPERTIES.F1_MEASURE.toString(), eval.weightedFMeasure());
        map.put(ClassificationResult.PROPERTIES.CLASSIFICATION_TIME.toString(), classificationTime);
        map.put(ClassificationResult.PROPERTIES.FEATURES_NUMBER.toString(), (double) instances.numAttributes());

        output.setProperties(map);

        return output;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    @Override
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public String getOptions() {
        return options;
    }

    @Override
    public void setOptions(String options) {
        this.options = options;
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }

    @Override
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public String toString() {
        return String.format("ML-%s[%s]<%s(%s)>", getTaskType(), getName(), getClassifier().getClass().getCanonicalName(), getOptions());
    }

    @Override
    public DBObject getDBObject() {
        return ImplementationManager.couchdb.object("ML:" + name)
                .put("name", name)
                .put("classifier", classifier.getClass().getCanonicalName())
                .put("taskType", taskType)
                .put("options", options);
    }

    @Override
    public ClassifierAlgorithm loadFromCouchDB(String name) {
        return loadFromCouchDB(ImplementationManager.couchdb.object("ML:" + name).load());
    }

    @Override
    public ClassifierAlgorithm loadFromCouchDB(DBObject dbObject) {
        setName(dbObject.getString("name"));
        setClassifier(dbObject.getClazz(Classifier.class, "classifier"));
        setOptions(dbObject.getString("options"));
        setTaskType(dbObject.getString("taskType"));
        return this;
    }

    public synchronized static Map<String, ClassifierAlgorithm> getAll(CouchDB db) {
        if (all == null) {
            all = db.getAll("ML:").parallelStream().map(o -> new ClassifierAlgorithm().loadFromCouchDB(o)).collect(Collectors.toMap(ClassifierAlgorithm::getName, Function.identity()));
        }

        return all;
    }

    public synchronized static Map<String, ClassifierAlgorithm> getAll() {
        if (all == null) {
            return getAll(ImplementationManager.couchdb);
        }

        return all;
    }
}
