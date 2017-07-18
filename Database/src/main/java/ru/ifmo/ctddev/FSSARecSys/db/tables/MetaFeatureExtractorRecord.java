package ru.ifmo.ctddev.FSSARecSys.db.tables;

import org.hibernate.criterion.Restrictions;
import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import ru.ifmo.ctddev.FSSARecSys.db.utils.HibernateUtil;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaFeatureExtractor;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Entity
@Table(name="MetaFeatureExtractor")
public class MetaFeatureExtractorRecord extends Record {
    // columns

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @Column(name="name", unique=true, nullable=false)
    private String name;

    @Column(name="classPath", nullable=false)
    private String classPath;

    // interface

    public MetaFeatureExtractor getInstance() {
        return ClassUtils.load(MetaFeatureExtractor.class, classPath);
    }

    public static void save(MetaFeatureExtractor extractor) {
        MetaFeatureExtractorRecord dbExtractor = new MetaFeatureExtractorRecord();
        dbExtractor.setName(extractor.getName());
        dbExtractor.setClassPath(extractor.getClass().getCanonicalName());
        dbExtractor.saveOrUpdate();
    }

    public static MetaFeatureExtractor getByName(String name) {
        return load(name).getInstance();
    }

    public static MetaFeatureExtractorRecord load(String name) {
        return HibernateUtil.runQuery((session) -> ((MetaFeatureExtractorRecord)session.createCriteria(MetaFeatureExtractorRecord.class).add(Restrictions.eq("name", name)).uniqueResult()));
    }

    public static List<MetaFeatureExtractor> getAll() {
        //noinspection unchecked,JpaQlInspection
        return ((Stream<MetaFeatureExtractor>)HibernateUtil.<List>runQuery(session -> session.createQuery("FROM MetaFeatureExtractorRecord").list())
                .stream()
                .map(r -> ((MetaFeatureExtractorRecord)r).getInstance()))
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
    public String getClassPath() {
        return classPath;
    }

    @SuppressWarnings("unused")
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }
}
