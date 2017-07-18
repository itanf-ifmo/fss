package ru.ifmo.ctddev.FSSARecSys.db.tables;

import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaLearningResult;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;

import org.hibernate.Session;
import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="SelectedFeatures")
public class SelectedFeaturesRecord extends Record {
    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="fss_algorithm")
    private FeatureSubsetSelectionAlgorithmRecord featureSubsetSelectionAlgorithmRecord;

    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="dataset")
    private DatasetRecord datasetRecord;

    @Column(name="selected_features", nullable=false)
    private byte[] selectedFeatures;


    @Column(name="selectionTime", nullable=false)
    private double selectionTime;

    // interface

    public FeatureSelectionResult getInstance(Class<? extends FeatureSelectionResult> clazz, Class<? extends Dataset> datasetClazz, Class<? extends FeatureSubsetSelectionAlgorithm> fssAlgorithmClazz) {
        FeatureSelectionResult result = ClassUtils.load(FeatureSelectionResult.class, clazz.getCanonicalName());
        result.setDataset(datasetRecord.getInstance(datasetClazz));
        result.setFeatureSubsetSelectionAlgorithm(featureSubsetSelectionAlgorithmRecord.getInstance(fssAlgorithmClazz));
        result.setSelectedFeatures(ClassUtils.deserialize((new int[0]).getClass(), this.selectedFeatures));
        result.setSelectionTime(selectionTime);
        return result;
    }

    public FeatureSelectionResult getInstance(Session session, Class<? extends FeatureSelectionResult> clazz, Class<? extends MetaLearningResult> mlResultClass) {
        FeatureSelectionResult result = ClassUtils.load(FeatureSelectionResult.class, clazz.getCanonicalName());
        result.setSelectedFeatures(ClassUtils.deserialize((new int[0]).getClass(), this.selectedFeatures));
        result.setSelectionTime(selectionTime);

        List<MetaLearningAlgorithmEvaluationResultRecord> mlResults = MetaLearningAlgorithmEvaluationResultRecord.getRecords(session, this);

        for (MetaLearningAlgorithmEvaluationResultRecord mlr : mlResults) {
            result.setResults(mlr.getMetaLearningAlgorithmRecord().getName(), mlr.getInstance(mlResultClass));
        }

        return result;
    }

    public static FeatureSelectionResult get(Class<? extends FeatureSelectionResult> clazz, Dataset dataset, FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm) {
        return load(
                DatasetRecord.load(dataset.getName()),
                FeatureSubsetSelectionAlgorithmRecord.load(featureSubsetSelectionAlgorithm.getName())
        ).getInstance(
                clazz,
                dataset.getClass(),
                featureSubsetSelectionAlgorithm.getClass());
    }

    public static SelectedFeaturesRecord load(FeatureSelectionResult result) {
        return load(
                DatasetRecord.load(result.getDataset().getName()),
                FeatureSubsetSelectionAlgorithmRecord.load(result.getFeatureSubsetSelectionAlgorithm().getName())
        );
    }

    public static SelectedFeaturesRecord load(DatasetRecord datasetRecord, FeatureSubsetSelectionAlgorithmRecord featureSubsetSelectionAlgorithmRecord) {
        int datasetId = datasetRecord.getId();
        int featureSubsetSelectionAlgorithmRecordId = featureSubsetSelectionAlgorithmRecord.getId();

        //noinspection JpaQlInspection
        return HibernateUtil.runQuery(session ->
                (SelectedFeaturesRecord)session
                .createQuery("FROM SelectedFeaturesRecord WHERE dataset = :dsId AND fss_algorithm = :fssId")
                .setParameter("dsId", datasetId)
                .setParameter("fssId", featureSubsetSelectionAlgorithmRecordId)
                .uniqueResult()
        );
    }

    public static void save(FeatureSelectionResult metaLearningAlgorithmOutput) {
        SelectedFeaturesRecord record = new SelectedFeaturesRecord();
        record.setDatasetRecord(DatasetRecord.load(metaLearningAlgorithmOutput.getDataset().getName()));
        record.setFeatureSubsetSelectionAlgorithmRecord(FeatureSubsetSelectionAlgorithmRecord.load(metaLearningAlgorithmOutput.getFeatureSubsetSelectionAlgorithm().getName()));
        record.setSelectedFeatures(ClassUtils.serialize(metaLearningAlgorithmOutput.getSelectedFeatures()));
        record.setSelectionTime(metaLearningAlgorithmOutput.getSelectionTime());
        record.saveOrUpdate();
    }

    public static List<SelectedFeaturesRecord> getRecords(DatasetRecord datasetRecord) {
        //noinspection JpaQlInspection
        return HibernateUtil.<List<SelectedFeaturesRecord>>runQuery(session ->
                session
                        .createQuery("FROM SelectedFeaturesRecord WHERE dataset = :dsId")
                        .setParameter("dsId", datasetRecord.getId())
                        .list()
        );
    }

    // setters and getters

    @SuppressWarnings("unused")
    public FeatureSubsetSelectionAlgorithmRecord getFeatureSubsetSelectionAlgorithmRecord() {
        return featureSubsetSelectionAlgorithmRecord;
    }

    @SuppressWarnings("unused")
    public void setFeatureSubsetSelectionAlgorithmRecord(FeatureSubsetSelectionAlgorithmRecord featureSubsetSelectionAlgorithmRecord) {
        this.featureSubsetSelectionAlgorithmRecord = featureSubsetSelectionAlgorithmRecord;
    }

    @SuppressWarnings("unused")
    public DatasetRecord getDatasetRecord() {
        return datasetRecord;
    }

    @SuppressWarnings("unused")
    public void setDatasetRecord(DatasetRecord datasetRecord) {
        this.datasetRecord = datasetRecord;
    }

    @SuppressWarnings("unused")
    public byte[] getSelectedFeatures() {
        return selectedFeatures;
    }

    @SuppressWarnings("unused")
    public void setSelectedFeatures(byte[] selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    @SuppressWarnings("unused")
    public double getSelectionTime() {
        return selectionTime;
    }

    @SuppressWarnings("unused")
    public void setSelectionTime(double selectionTime) {
        this.selectionTime = selectionTime;
    }

    // other

    @Override
    public boolean equals(Object other) {
        return other != null && (other instanceof MetaLearningAlgorithmEvaluationResultRecord) && other.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return datasetRecord.hashCode() + featureSubsetSelectionAlgorithmRecord.hashCode();
    }
}
