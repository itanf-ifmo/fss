package ru.ifmo.ctddev.FSSARecSys.db.tables;

import org.hibernate.criterion.Restrictions;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSubsetSelectionAlgorithm;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Entity
@Table(name="FSSAlgorithm")
public class FeatureSubsetSelectionAlgorithmRecord extends Record {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @Column(name="name", unique=true, nullable=false)
    private String name;

    @Column(name="searchClass", nullable=false)
    private String searchClass;

    @Column(name="evalClass", nullable=false)
    private String evalClass;

    @Column(name="searchOptions", nullable=false)
    private String searchOptions;

    @Column(name="evalOptions", nullable=false)
    private String evalOptions;

    // interface

    public FeatureSubsetSelectionAlgorithm getInstance(Class<? extends FeatureSubsetSelectionAlgorithm> clazz) {
        FeatureSubsetSelectionAlgorithm fssAlgorithm = ClassUtils.load(FeatureSubsetSelectionAlgorithm.class, clazz.getCanonicalName());
        fssAlgorithm.setName(name);
        fssAlgorithm.setSearcher(ClassUtils.load(ASSearch.class, searchClass));
        fssAlgorithm.setSearchOptions(searchOptions);
        fssAlgorithm.setEvaluator(ClassUtils.load(ASEvaluation.class, evalClass));
        fssAlgorithm.setEvaluatorOptions(evalOptions);

        return fssAlgorithm;
    }

    public static void save(FeatureSubsetSelectionAlgorithm fssAlgorithm) {
        FeatureSubsetSelectionAlgorithmRecord fssAlgorithmRecord = new FeatureSubsetSelectionAlgorithmRecord();
        fssAlgorithmRecord.setName(fssAlgorithm.getName());
        fssAlgorithmRecord.setSearchClass(fssAlgorithm.getSearcher().getClass().getCanonicalName());
        fssAlgorithmRecord.setSearchOptions(fssAlgorithm.getSearchOptions());
        fssAlgorithmRecord.setEvalClass(fssAlgorithm.getEvaluator().getClass().getCanonicalName());
        fssAlgorithmRecord.setEvalOptions(fssAlgorithm.getEvaluatorOptions());
        fssAlgorithmRecord.saveOrUpdate();
    }

    public static FeatureSubsetSelectionAlgorithm getByName(Class<? extends FeatureSubsetSelectionAlgorithm> clazz, String name) {
        return load(name).getInstance(clazz);
    }

    public static FeatureSubsetSelectionAlgorithmRecord load(String name) {
        return HibernateUtil.runQuery(session -> (FeatureSubsetSelectionAlgorithmRecord)session
                .createCriteria(FeatureSubsetSelectionAlgorithmRecord.class)
                .add(Restrictions.eq("name", name))
                .uniqueResult()
        );
    }

    public static FeatureSubsetSelectionAlgorithmRecord load(FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm) {
        return load(featureSubsetSelectionAlgorithm.getName());
    }

    public static List<FeatureSubsetSelectionAlgorithm> getAll(Class<? extends FeatureSubsetSelectionAlgorithm> clazz) {
        //noinspection unchecked,JpaQlInspection
        return ((Stream<FeatureSubsetSelectionAlgorithm>)HibernateUtil.<List>runQuery(session -> session.createQuery("FROM FeatureSubsetSelectionAlgorithmRecord").list())
                .stream()
                .map(r -> ((FeatureSubsetSelectionAlgorithmRecord)r).getInstance(clazz)))
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
    public String getSearchClass() {
        return searchClass;
    }

    @SuppressWarnings("unused")
    public void setSearchClass(String searchClass) {
        this.searchClass = searchClass;
    }

    @SuppressWarnings("unused")
    public String getEvalClass() {
        return evalClass;
    }

    @SuppressWarnings("unused")
    public void setEvalClass(String evalClass) {
        this.evalClass = evalClass;
    }

    @SuppressWarnings("unused")
    public String getSearchOptions() {
        return searchOptions;
    }

    @SuppressWarnings("unused")
    public void setSearchOptions(String searchOptions) {
        this.searchOptions = searchOptions;
    }

    @SuppressWarnings("unused")
    public String getEvalOptions() {
        return evalOptions;
    }

    @SuppressWarnings("unused")
    public void setEvalOptions(String evalOptions) {
        this.evalOptions = evalOptions;
    }
}
