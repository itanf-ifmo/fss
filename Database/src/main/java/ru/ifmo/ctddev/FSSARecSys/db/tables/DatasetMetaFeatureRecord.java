package ru.ifmo.ctddev.FSSARecSys.db.tables;

import org.hibernate.Session;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;

import javax.persistence.*;
import java.util.Map;
import java.util.TreeMap;


@Entity
@Table(name="DatasetMetaFeature")
public class DatasetMetaFeatureRecord extends Record {
    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="meta_feature_extractor")
    private MetaFeatureExtractorRecord metaFeatureExtractorRecord;

    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="dataset")
    private DatasetRecord datasetRecord;

    @Column(name="value", nullable=false)
    private double value;

    public static Map<String, Pair<Double, Double>> getMinMaxValuesOfDatasetMetaFeatures() {
        return HibernateUtil.<Map<String, Pair<Double, Double>>>runQuery(DatasetMetaFeatureRecord::getMinMaxValuesOfDatasetMetaFeatures);
    }

    private static Map<String, Pair<Double, Double>> getMinMaxValuesOfDatasetMetaFeatures(Session session) {
        Map<String, Pair<Double, Double>> map = new TreeMap<>();
        //noinspection JpaQlInspection
        for (Object e : session.createQuery("FROM MetaFeatureExtractorRecord").list()) { // todo rewrite this query
            MetaFeatureExtractorRecord extractor = (MetaFeatureExtractorRecord)e;

            //noinspection JpaQlInspection
            Double minV = (double) session
                    .createQuery("SELECT MIN(value) FROM DatasetMetaFeatureRecord where meta_feature_extractor = :mfId")
                    .setParameter("mfId", extractor.getId())
                    .uniqueResult();

            //noinspection JpaQlInspection
            Double maxV = (double) session
                    .createQuery("SELECT MAX(value) FROM DatasetMetaFeatureRecord where meta_feature_extractor = :mfId")
                    .setParameter("mfId", extractor.getId())
                    .uniqueResult();

            map.put(extractor.getName(), Pair.of(minV, maxV));
        }

        return map;
    }

    // setters and getters

    @SuppressWarnings("unused")
    public DatasetRecord getDatasetRecord() {
        return datasetRecord;
    }

    @SuppressWarnings("unused")
    public void setDatasetRecord(DatasetRecord datasetRecord) {
        this.datasetRecord = datasetRecord;
    }

    @SuppressWarnings("unused")
    public double getValue() {
        return value;
    }

    @SuppressWarnings("unused")
    public void setValue(double value) {
        this.value = value;
    }

    @SuppressWarnings("unused")
    public MetaFeatureExtractorRecord getMetaFeatureExtractorRecord() {
        return metaFeatureExtractorRecord;
    }

    @SuppressWarnings("unused")
    public void setMetaFeatureExtractorRecord(MetaFeatureExtractorRecord metaFeatureExtractorRecord) {
        this.metaFeatureExtractorRecord = metaFeatureExtractorRecord;
    }

    // other

    @Override
    public boolean equals(Object other) {
        return other != null && (other instanceof DatasetMetaFeatureRecord) && other.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return metaFeatureExtractorRecord.hashCode() + datasetRecord.hashCode();
    }
}
