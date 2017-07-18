package ru.ifmo.ctddev.FSSARecSys.interfaces;

public interface FeatureSelectionResult extends CouchDBSavable<FeatureSelectionResult> {
    int[] getSelectedFeatures();
    void setSelectedFeatures(int[] selectedFeatures);

    default int getNumberOfSelectedFeatures() {
        return getSelectedFeatures().length;
    }

    /**
     * selection time in seconds
     */
    double getSelectionTime();
    void setSelectionTime(double selectionTime);

    Dataset getDataset();
    void setDataset(Dataset dataset);

    FeatureSubsetSelectionAlgorithm getFeatureSubsetSelectionAlgorithm();
    void setFeatureSubsetSelectionAlgorithm(FeatureSubsetSelectionAlgorithm fssAlgorithm);

    void setResults(String metaLearningAlgorithmName, MetaLearningResult metaLearningResult);
    MetaLearningResult getResult(String metaLearningAlgorithmName);

    String getId();

    void setName(String name);

    String getName();
}
