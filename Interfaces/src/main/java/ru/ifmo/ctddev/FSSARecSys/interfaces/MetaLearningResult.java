package ru.ifmo.ctddev.FSSARecSys.interfaces;


import java.util.TreeMap;

public interface MetaLearningResult extends CouchDBSavable<MetaLearningResult> {
    MetaLearningAlgorithm getMetaLearningAlgorithm();
    void setMetaLearningAlgorithm(MetaLearningAlgorithm metaLearningAlgorithm);

    FeatureSelectionResult getFeatureSelectionResult();
    void setFeatureSelectionResult(FeatureSelectionResult metaFeatureSelectionResult);

    TreeMap<String, Double> getProperties();
    void setProperties(TreeMap<String, Double> properties);
}
