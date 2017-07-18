package ru.ifmo.ctddev.FSSARecSys.db;

import ru.ifmo.ctddev.FSSARecSys.db.tables.*;
import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.interfaces.*;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HibernateDatabase implements Database {
    @Override
    public Dataset getDataset(Class<? extends Dataset> clazz, String name) {
        return DatasetRecord.getByName(clazz, name);
    }

    @Override
    public List<Dataset> getAllDatasets(Class<? extends Dataset> clazz) {
        return DatasetRecord.getAll(clazz);
    }

    @Override
    public void save(Dataset dataset) {
        System.out.println("Saving " + dataset);
        DatasetRecord.save(dataset);
    }

    @Override
    public FeatureSubsetSelectionAlgorithm getFeatureSubsetSelectionAlgorithm(Class<? extends FeatureSubsetSelectionAlgorithm> clazz, String name) {
        return FeatureSubsetSelectionAlgorithmRecord.getByName(clazz, name);
    }

    @Override
    public List<FeatureSubsetSelectionAlgorithm> getAllFeatureSubsetSelectionAlgorithms(Class<? extends FeatureSubsetSelectionAlgorithm> clazz) {
        return FeatureSubsetSelectionAlgorithmRecord.getAll(clazz);
    }

    @Override
    public void save(FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm) {
        FeatureSubsetSelectionAlgorithmRecord.save(featureSubsetSelectionAlgorithm);
    }

    @Override
    public MetaFeatureExtractor getMetaFeatureExtractor(String name) {
        return MetaFeatureExtractorRecord.getByName(name);
    }

    @Override
    public List<String> getAllDatasetMetaFeatureNames() {
        return MetaFeatureExtractorRecord.getAll().stream().map(MetaFeatureExtractor::getName).collect(Collectors.toList());
    }

    @Override
    public List<MetaFeatureExtractor> getAllMetaFeatureExtractors() {
        return MetaFeatureExtractorRecord.getAll();
    }

    @Override
    public void save(MetaFeatureExtractor metaFeatureExtractor) {
        MetaFeatureExtractorRecord.save(metaFeatureExtractor);
    }

    @Override
    public FeatureSelectionResult getFeatureSelectionResult(Class<? extends FeatureSelectionResult> clazz, Dataset dataset, FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm) {
        return SelectedFeaturesRecord.get(clazz, dataset, featureSubsetSelectionAlgorithm);
    }

    @Override
    public void save(FeatureSelectionResult featureSelectionResult) {
        System.out.println("Saving " + featureSelectionResult.toString());
        SelectedFeaturesRecord.save(featureSelectionResult);
    }

    @Override
    public MetaLearningAlgorithm getMetaLearningAlgorithm(Class<? extends MetaLearningAlgorithm> clazz, String name) {
        return MetaLearningAlgorithmRecord.getByName(clazz, name);
    }

    @Override
    public List<MetaLearningAlgorithm> getAllMetaLearningAlgorithms(Class<? extends MetaLearningAlgorithm> clazz) {
        return MetaLearningAlgorithmRecord.getAll(clazz);
    }

    @Override
    public void save(MetaLearningAlgorithm metaLearningAlgorithm) {
        System.out.println("Saving " + metaLearningAlgorithm.toString());
        MetaLearningAlgorithmRecord.save(metaLearningAlgorithm);
    }

    @Override
    public MetaLearningResult getMetaLearningResult(Class<? extends MetaLearningResult> clazz, FeatureSelectionResult featureSelectionResult, MetaLearningAlgorithm mla) {
        return MetaLearningAlgorithmEvaluationResultRecord.get(clazz, featureSelectionResult, mla);
    }

    @Override
    public void save(MetaLearningResult metaLearningResult) {
        System.out.println("Saving " + metaLearningResult.toString());
        MetaLearningAlgorithmEvaluationResultRecord.save(metaLearningResult);
    }

    @Override
    public Map<String, Pair<Double, Double>> getMinMaxValuesOfDatasetMetaFeatures() {
        return DatasetMetaFeatureRecord.getMinMaxValuesOfDatasetMetaFeatures();
    }

    @Override
    public List<Dataset> getAllDatasets(Class<? extends Dataset> clazz, Class<? extends FeatureSelectionResult> FSSRClazz, Class<? extends MetaLearningResult> MLResultClazz) {
        return DatasetRecord.getAllDatasets(clazz, FSSRClazz, MLResultClazz);
    }

    public void close() {
        HibernateUtil.close();
    }
}
