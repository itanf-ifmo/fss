package ru.ifmo.ctddev.FSSARecSys.privetives;

import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ClassificationResult extends BaseMetaLearningResult {
    protected static Map<String, ClassificationResult> all;
    private static final String toSyncCache = "";

    public enum PROPERTIES {
        F1_MEASURE,
        ACCURACY,
        CLASSIFICATION_TIME,
        FEATURES_NUMBER,
    }

    public double getF1Measure() {
        return getProperties().get(PROPERTIES.F1_MEASURE.toString());
    }

    public long getFeaturesNumber() {
        return Math.round(getProperties().get(PROPERTIES.FEATURES_NUMBER.toString()));
    }

    public double getAccuracy() {
        return getProperties().get(PROPERTIES.ACCURACY.toString());
    }

    public double getClassificationTime() {
        return getProperties().get(PROPERTIES.CLASSIFICATION_TIME.toString());
    }

    @Override
    public String toString() {
        return String.format("ML-Result<f1:%f acc:%f t:%f, n:%f>", getF1Measure(), getAccuracy(), getClassificationTime(), getProperties().get(PROPERTIES.FEATURES_NUMBER.toString()));
    }

    public synchronized static Map<String, ClassificationResult> getAll() {
        synchronized (toSyncCache) {
            if (all == null) {
                Stream<ClassificationResult> a = ImplementationManager.couchdb.getAll("LR:").parallelStream().map(o -> (ClassificationResult) new ClassificationResult().loadFromCouchDB(o));
                all = a.collect(Collectors.toMap(ClassificationResult::getId, Function.identity()));
            }

            return all;
        }
    }
}
