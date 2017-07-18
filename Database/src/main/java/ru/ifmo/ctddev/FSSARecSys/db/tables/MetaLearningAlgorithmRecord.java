package ru.ifmo.ctddev.FSSARecSys.db.tables;

import org.hibernate.criterion.Restrictions;
import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaLearningAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import weka.classifiers.Classifier;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Entity
@Table(name="MLAlgorithm")
public class MetaLearningAlgorithmRecord extends Record {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @Column(name="name", unique=true, nullable=false)
    private String name;

    @Column(name="classPath", nullable=false)
    private String classPath;

    @Column(name="options", nullable=false)
    private String options;

    @Column(name="taskType", nullable=false)
    private String taskType;

    public MetaLearningAlgorithm getInstance(Class<? extends MetaLearningAlgorithm> clazz) {
        MetaLearningAlgorithm metaLearningAlgorithm = ClassUtils.load(MetaLearningAlgorithm.class, clazz.getCanonicalName());
        metaLearningAlgorithm.setName(name);
        metaLearningAlgorithm.setClassifier(ClassUtils.load(Classifier.class, classPath)); // todo classifier?
        metaLearningAlgorithm.setOptions(options);
        metaLearningAlgorithm.setTaskType(taskType);

        return metaLearningAlgorithm;
    }

    public static void save(MetaLearningAlgorithm metaLearningAlgorithm) {
        MetaLearningAlgorithmRecord metaLearningAlgorithmRecord = new MetaLearningAlgorithmRecord();
        metaLearningAlgorithmRecord.setName(metaLearningAlgorithm.getName());
        metaLearningAlgorithmRecord.setClassPath(metaLearningAlgorithm.getClassifier().getClass().getCanonicalName()); // todo classifier?
        metaLearningAlgorithmRecord.setOptions(metaLearningAlgorithm.getOptions());
        metaLearningAlgorithmRecord.setTaskType(metaLearningAlgorithm.getTaskType());
        metaLearningAlgorithmRecord.saveOrUpdate();
    }

    public static List<MetaLearningAlgorithm> getAll(Class<? extends MetaLearningAlgorithm> clazz) {
        //noinspection unchecked,JpaQlInspection
        return ((Stream<MetaLearningAlgorithm>)HibernateUtil.<List>runQuery(session -> session.createQuery("FROM MetaLearningAlgorithmRecord").list())
                .stream()
                .map(r -> ((MetaLearningAlgorithmRecord)r).getInstance(clazz)))
                .collect(Collectors.toList());
    }

    public static MetaLearningAlgorithm getByName(Class<? extends MetaLearningAlgorithm> clazz, String name) {
        return load(name).getInstance(clazz);
    }

    public static MetaLearningAlgorithmRecord load(MetaLearningAlgorithm metaLearningAlgorithm) {
        return load(metaLearningAlgorithm.getName());
    }

    public static MetaLearningAlgorithmRecord load(String name) {
        return HibernateUtil.runQuery((session) -> ((MetaLearningAlgorithmRecord)session.createCriteria(MetaLearningAlgorithmRecord.class).add(Restrictions.eq("name", name)).uniqueResult()));
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
    public String getClassPath() {
        return classPath;
    }

    @SuppressWarnings("unused")
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    @SuppressWarnings("unused")
    public String getOptions() {
        return options;
    }

    @SuppressWarnings("unused")
    public void setOptions(String options) {
        this.options = options;
    }

    @SuppressWarnings("unused")
    public String getTaskType() {
        return taskType;
    }

    @SuppressWarnings("unused")
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
