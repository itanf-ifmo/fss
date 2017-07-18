package ru.ifmo.ctddev.FSSARecSys.interfaces;

import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;
import java.util.Map;


public interface Database {
    // datasets
    Dataset getDataset(Class<? extends Dataset> clazz, String name);
    List<Dataset> getAllDatasets(Class<? extends Dataset> clazz);
    void save(Dataset dataset);


    // FeatureSubsetSelectionAlgorithms
    FeatureSubsetSelectionAlgorithm getFeatureSubsetSelectionAlgorithm(Class<? extends FeatureSubsetSelectionAlgorithm> clazz, String name);
    List<FeatureSubsetSelectionAlgorithm> getAllFeatureSubsetSelectionAlgorithms(Class<? extends FeatureSubsetSelectionAlgorithm> clazz);
    void save(FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm);


    // MetaFeaturesExtractors
    MetaFeatureExtractor getMetaFeatureExtractor(String name);
    List<String> getAllDatasetMetaFeatureNames();
    List<MetaFeatureExtractor> getAllMetaFeatureExtractors();
    void save(MetaFeatureExtractor dataset);

    // Feature Subset Selection Result
    FeatureSelectionResult getFeatureSelectionResult(Class<? extends FeatureSelectionResult> clazz, Dataset dataset, FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm);
    void save(FeatureSelectionResult featureSelectionResult);

    // Evaluation
    MetaLearningAlgorithm getMetaLearningAlgorithm(Class<? extends MetaLearningAlgorithm> clazz, String name);
    List<MetaLearningAlgorithm> getAllMetaLearningAlgorithms(Class<? extends MetaLearningAlgorithm> clazz);
    void save(MetaLearningAlgorithm metaLearningAlgorithm);

    MetaLearningResult getMetaLearningResult(Class<? extends MetaLearningResult> clazz, FeatureSelectionResult featureSelectionResult, MetaLearningAlgorithm mla);
    void save(MetaLearningResult metaLearningResult);

    // Statistic
    Map<String, Pair<Double, Double>> getMinMaxValuesOfDatasetMetaFeatures();

    List<Dataset> getAllDatasets(Class<? extends Dataset> clazz, Class<? extends FeatureSelectionResult> FSSRClazz, Class<? extends MetaLearningResult> MLResultClazz);

    void close();
}
