package ru.ifmo.ctddev.FSSARecSys.privetives;

import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.CouchDBSavable;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaFeatureExtractor;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseMetaFeaturesExtractor implements MetaFeatureExtractor, CouchDBSavable<BaseMetaFeaturesExtractor> {
    private MetaFeatureExtractor extractor;

    // cache
    private static Map<String, BaseMetaFeaturesExtractor> all;
    private String name;
    private String id;

    public BaseMetaFeaturesExtractor(DBObject o) {
        loadFromCouchDB(o);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public BaseMetaFeaturesExtractor(MetaFeatureExtractor mfe) {
        extractor = mfe;
        name = mfe.getName();
    }

    @Override
    public double extract(Dataset dataset) {
        return extractor.extract(dataset);
    }

    @Override
    public DBObject getDBObject() {
        return ImplementationManager.couchdb.object("MFE:" + getName())
                .put("extractor", extractor.getClass().getCanonicalName())
                .put("name", getName());
    }

    @Override
    public BaseMetaFeaturesExtractor loadFromCouchDB(String name) {
        return loadFromCouchDB(ImplementationManager.couchdb.object("MFE:" + getName()));
    }

    @Override
    public BaseMetaFeaturesExtractor loadFromCouchDB(DBObject dbObject) {
        setId(dbObject.getId());
        extractor = ClassUtils.load(MetaFeatureExtractor.class, dbObject.getString("extractor"));
        setName(dbObject.getString("name"));
        return this;
    }

    public synchronized static Map<String, BaseMetaFeaturesExtractor> getAll() {
        return getAll(ImplementationManager.couchdb);
    }

    public synchronized static Map<String, BaseMetaFeaturesExtractor> getAll(CouchDB db) {
        if (all == null) {
            all = db.getAll("MFE:").parallelStream().map(BaseMetaFeaturesExtractor::new).collect(Collectors.toMap(BaseMetaFeaturesExtractor::getName, Function.identity()));
        }
        return all;
    }
}
