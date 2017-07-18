package ru.ifmo.ctddev.FSSARecSys.db.tables;

import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;
import ru.ifmo.ctddev.FSSARecSys.interfaces.*;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;

import org.hibernate.Session;
import javax.persistence.*;
import java.util.List;
import java.util.TreeMap;


@Entity
@Table(name="MLEvaluationResult")
public class MetaLearningAlgorithmEvaluationResultRecord extends Record {
    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="ml_algorithm")
    private MetaLearningAlgorithmRecord metaLearningAlgorithmRecord;

    @Id
    @ManyToOne(optional=false)
    @JoinColumns({
            @JoinColumn(name="dataset", referencedColumnName="dataset"),
            @JoinColumn(name="fss_algorithm", referencedColumnName="fss_algorithm")
    })
    private SelectedFeaturesRecord selectedFeaturesRecord;
//
//    @JoinColumn(name="dataset")
//    private DatasetRecord datasetRecord;
//
//    @JoinColumn(name="fss_algorithm")
//    private FeatureSubsetSelectionAlgorithmRecord featureSubsetSelectionAlgorithmRecord;

    @Column(name="result", nullable=false)
    private byte[] result;

    // interface

    public MetaLearningResult getInstance(Class<? extends MetaLearningResult> clazz, FeatureSelectionResult featureSelectionResult, MetaLearningAlgorithm mla) {
        MetaLearningResult output = ClassUtils.load(MetaLearningResult.class, clazz.getCanonicalName());
        output.setFeatureSelectionResult(featureSelectionResult);
        output.setMetaLearningAlgorithm(mla);
        //noinspection unchecked
        output.setProperties(ClassUtils.deserialize((new TreeMap<String, Double>()).getClass(), result));

        return output;
    }

    public static MetaLearningResult get(Class<? extends MetaLearningResult> clazz, FeatureSelectionResult featureSelectionResult, MetaLearningAlgorithm mla) {
        return load(featureSelectionResult, mla).getInstance(clazz, featureSelectionResult, mla);
    }

    public static MetaLearningAlgorithmEvaluationResultRecord load(FeatureSelectionResult featureSelectionResult, MetaLearningAlgorithm mla) {
        int dsId = DatasetRecord.load(featureSelectionResult.getDataset()).getId();
        int fssId = FeatureSubsetSelectionAlgorithmRecord.load(featureSelectionResult.getFeatureSubsetSelectionAlgorithm()).getId();
        int mlId = MetaLearningAlgorithmRecord.load(mla).getId();

        //noinspection JpaQlInspection
        return HibernateUtil.runQuery(session ->
                (MetaLearningAlgorithmEvaluationResultRecord)session
                        .createQuery("FROM MetaLearningAlgorithmEvaluationResultRecord WHERE dataset = :dsId AND fss_algorithm = :fssId AND ml_algorithm = :mlId")
                        .setParameter("dsId", dsId)
                        .setParameter("fssId", fssId)
                        .setParameter("mlId", mlId)
                        .uniqueResult()
        );
    }

    public static List<MetaLearningAlgorithmEvaluationResultRecord> getRecords(Session session, SelectedFeaturesRecord selectedFeaturesRecord) {
        //noinspection JpaQlInspection,unchecked
        return (List<MetaLearningAlgorithmEvaluationResultRecord>)session
                        .createQuery("FROM MetaLearningAlgorithmEvaluationResultRecord WHERE dataset = :dsId AND fss_algorithm = :fssId")
                        .setParameter("dsId", selectedFeaturesRecord.getDatasetRecord().getId())
                        .setParameter("fssId", selectedFeaturesRecord.getFeatureSubsetSelectionAlgorithmRecord().getId())
                        .list();
    }

    public MetaLearningResult getInstance(Class<? extends MetaLearningResult> clazz) {
        MetaLearningResult output = ClassUtils.load(MetaLearningResult.class, clazz.getCanonicalName());
        //noinspection unchecked
        output.setProperties(ClassUtils.deserialize((new TreeMap<String, Double>()).getClass(), result));
        return output;
    }

    public static void save(MetaLearningResult output) {
        MetaLearningAlgorithmEvaluationResultRecord record = new MetaLearningAlgorithmEvaluationResultRecord();
        record.setMetaLearningAlgorithmRecord(MetaLearningAlgorithmRecord.load(output.getMetaLearningAlgorithm()));
        record.setSelectedFeaturesRecord(SelectedFeaturesRecord.load(output.getFeatureSelectionResult()));
        record.setResult(ClassUtils.serialize(output.getProperties()));
        record.saveOrUpdate();
    }

    // setters and getters

    @SuppressWarnings("unused")
    public MetaLearningAlgorithmRecord getMetaLearningAlgorithmRecord() {
        return metaLearningAlgorithmRecord;
    }

    @SuppressWarnings("unused")
    public void setMetaLearningAlgorithmRecord(MetaLearningAlgorithmRecord metaLearningAlgorithmRecord) {
        this.metaLearningAlgorithmRecord = metaLearningAlgorithmRecord;
    }
//
//    @SuppressWarnings("unused")
//    public FeatureSubsetSelectionAlgorithmRecord getFeatureSubsetSelectionAlgorithmRecord() {
//        return featureSubsetSelectionAlgorithmRecord;
//    }
//
//    @SuppressWarnings("unused")
//    public void setFeatureSubsetSelectionAlgorithmRecord(FeatureSubsetSelectionAlgorithmRecord featureSubsetSelectionAlgorithmRecord) {
//        this.featureSubsetSelectionAlgorithmRecord = featureSubsetSelectionAlgorithmRecord;
//    }

    @SuppressWarnings("unused")
    public SelectedFeaturesRecord getSelectedFeaturesRecord() {
        return selectedFeaturesRecord;
    }

    @SuppressWarnings("unused")
    public void setSelectedFeaturesRecord(SelectedFeaturesRecord selectedFeaturesRecord) {
        this.selectedFeaturesRecord = selectedFeaturesRecord;
    }

    @SuppressWarnings("unused")
    public byte[] getResult() {
        return result;
    }

    @SuppressWarnings("unused")
    public void setResult(byte[] result) {
        this.result = result;
    }

    // other

    @Override
    public boolean equals(Object other) {
        return other != null && (other instanceof MetaLearningAlgorithmEvaluationResultRecord) && other.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return metaLearningAlgorithmRecord.hashCode() + selectedFeaturesRecord.hashCode();
    }
}
