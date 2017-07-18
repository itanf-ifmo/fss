package ru.ifmo.ctddev.FSSARecSys.interfaces;


import weka.classifiers.Classifier;

public interface MetaLearningAlgorithm extends CouchDBSavable<MetaLearningAlgorithm> {
    /**
     * Run meta-learner on full set of instances.
     */
    MetaLearningResult evaluate(Dataset dataset);

    /**
     * Run meta-learner using preselected instances in featureSelectionResult
     */
    MetaLearningResult evaluate(Dataset dataset, FeatureSelectionResult featureSelectionResult);
    MetaLearningResult evaluate(Dataset dataset, int[] selectedFeatures);

    String getName();
    void setName(String name);

    String getTaskType();
    void setTaskType(String taskType);

    String getOptions();
    void setOptions(String options);

    Classifier getClassifier();

    void setClassifier(Classifier classifier);
}
