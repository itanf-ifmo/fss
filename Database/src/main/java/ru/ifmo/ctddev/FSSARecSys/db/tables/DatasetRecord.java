package ru.ifmo.ctddev.FSSARecSys.db.tables;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaLearningResult;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import weka.core.Instances;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil.getSessionFactory;


@Entity
@Table(name="Dataset")
public class DatasetRecord extends Record {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @Column(name="name", unique=true, nullable=false)
    private String name;

    @Column(name="file", nullable=false)
    private byte[] file;

    @Column(name="taskType", nullable=false)
    private String taskType;

    public Map<String, Double> getMetaFeatures() {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            session.beginTransaction();
            Map<String, Double> map = new TreeMap<>();

            //noinspection JpaQlInspection
            for (Object o : session.createQuery("FROM DatasetMetaFeatureRecord where dataset = :datasetId").setParameter("datasetId", id).list()) {
                map.put(((DatasetMetaFeatureRecord) o).getMetaFeatureExtractorRecord().getName(), ((DatasetMetaFeatureRecord) o).getValue());
            }

            return map;

        } finally {
            if (session != null) {
                session.getTransaction().commit();
                session.close();
            }

//            close();
        }
    }

    public void setMetaFeatures(Map<String, Double> metaFeatures) {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            session.beginTransaction();
            for (String metaFeatureExecutorName : metaFeatures.keySet()) {
                DatasetMetaFeatureRecord metaFeatureRecord = new DatasetMetaFeatureRecord();
                metaFeatureRecord.setDatasetRecord(this);
                metaFeatureRecord.setMetaFeatureExtractorRecord((MetaFeatureExtractorRecord)session
                        .createCriteria(MetaFeatureExtractorRecord.class)
                        .add(Restrictions.eq("name", metaFeatureExecutorName))
                        .uniqueResult()
                );
                metaFeatureRecord.setValue(metaFeatures.get(metaFeatureExecutorName));
                session.saveOrUpdate(metaFeatureRecord);
            }

        } finally {
            if (session != null) {
                session.getTransaction().commit();
                session.close();
            }

//            close();
        }
    }

    // interface

    public Dataset getInstance(Session session, Class<? extends Dataset> clazz, Class<? extends FeatureSelectionResult> FSSRClazz, Class<? extends MetaLearningResult> MLResultClazz) {
        Dataset dataset = ClassUtils.load(Dataset.class, clazz.getCanonicalName());
        dataset.setName(name);
        dataset.setTaskType(taskType);
        dataset.setInstances(ClassUtils.deserialize(Instances.class, file));
        dataset.setMetaFeatures(getMetaFeatures());
//
//        List<SelectedFeaturesRecord> fssResults = SelectedFeaturesRecord.getRecords(this);
//
//        for (SelectedFeaturesRecord sfr : fssResults) {
//            dataset.setResults(sfr.getFeatureSubsetSelectionAlgorithmRecord().getName(), sfr.getInstance(session, FSSRClazz, MLResultClazz));
//        }

        return dataset;
    }

    public static List<Dataset> getAllDatasets(Class<? extends Dataset> clazz, Class<? extends FeatureSelectionResult> FSSRClazz, Class<? extends MetaLearningResult> MLResultClazz) {
        //noinspection unchecked,JpaQlInspection
        return HibernateUtil.<List>runQuery(session -> ((List<DatasetRecord>)session.createQuery("FROM DatasetRecord").list())
                .stream()
                .map(r -> r.getInstance(session, clazz, FSSRClazz, MLResultClazz))
                .collect(Collectors.toList()));
    }

    public Dataset getInstance(Class<? extends Dataset> clazz) {
        Dataset dataset = ClassUtils.load(Dataset.class, clazz.getCanonicalName());
        dataset.setName(name);
        dataset.setTaskType(taskType);
        dataset.setInstances(ClassUtils.deserialize(Instances.class, file));
        dataset.setMetaFeatures(getMetaFeatures());
        return dataset;
    }

    public static void save(Dataset dataset) {
        DatasetRecord datasetRecord;

        datasetRecord = load(dataset.getName());
        if (datasetRecord == null) {
            datasetRecord = new DatasetRecord();
            datasetRecord.setName(dataset.getName());
        }

        datasetRecord.setTaskType(dataset.getTaskType());
        datasetRecord.setFile(ClassUtils.serialize(dataset.getInstances()));
        datasetRecord.saveOrUpdate();
        datasetRecord.setMetaFeatures(dataset.getMetaFeatures());
    }

    public static Dataset getByName(Class<? extends Dataset> clazz, String name) {
        return load(name).getInstance(clazz);
    }

    public static DatasetRecord load(String name) {
        return HibernateUtil.runQuery(session -> (DatasetRecord)session
                .createCriteria(DatasetRecord.class)
                .add(Restrictions.eq("name", name))
                .uniqueResult()
        );
    }

    public static DatasetRecord load(Dataset dataset) {
        return load(dataset.getName());
    }

    public static List<Dataset> getAll(Class<? extends Dataset> clazz) {
        //noinspection unchecked,JpaQlInspection
        return ((Stream<Dataset>)HibernateUtil.<List>runQuery(session -> session.createQuery("FROM DatasetRecord").list())
                .stream()
                .map(r -> ((DatasetRecord)r).getInstance(clazz)))
                .collect(Collectors.toList());
    }

    // setters and getters

    @SuppressWarnings("unused")
    public int getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(int id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public String getTaskType() {
        return taskType;
    }

    @SuppressWarnings("unused")
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    @SuppressWarnings("unused")
    public byte[] getFile() {
        return file;
    }

    @SuppressWarnings("unused")
    public void setFile(byte[] file) {
        this.file = file;
    }
}
