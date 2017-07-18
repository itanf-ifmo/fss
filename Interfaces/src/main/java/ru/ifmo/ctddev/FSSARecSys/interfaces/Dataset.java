package ru.ifmo.ctddev.FSSARecSys.interfaces;

import weka.core.Instances;

import java.net.URL;
import java.util.Map;

public interface Dataset extends CouchDBSavable<Dataset> {
    String getName();
    void setName(String name);

    Instances getInstances();

    /**
     * get instances with preselected features.
     */
    Instances getInstances(FeatureSelectionResult featureSelectionResult);
    Instances getInstances(int[] selectedFeatures);
    void setInstances(URL url);
    void setInstances(Instances instances);

    Map<String, Double> getMetaFeatures();
    void setMetaFeatures(Map<String, Double> metaFeatures);

    String getTaskType();
    void setTaskType(String taskType);

    void setResults(String fssAlgorithmName, FeatureSelectionResult featureSelectionResult);

    void setIncluded(boolean flag);
    boolean getIncluded();

    FeatureSelectionResult getResult(String fssAlgorithmName);
    FeatureSelectionResult getResult(String fssAlgorithmName, String mla);

    default int getFeaturesNumber() {
        return getInstances().numAttributes();
    }
}
