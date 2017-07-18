package ru.ifmo.ctddev.FSSARecSys.privetives;

import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaLearningAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaLearningResult;

import java.util.Map;
import java.util.TreeMap;


public class BaseMetaLearningResult implements MetaLearningResult {
    protected MetaLearningAlgorithm metaLearningAlgorithm;
    protected TreeMap<String, Double> properties;
    protected FeatureSelectionResult featureSelectionResult;
    protected String id;
    private String selector;

    @Override
    public MetaLearningAlgorithm getMetaLearningAlgorithm() {
        return metaLearningAlgorithm;
    }

    @Override
    public void setMetaLearningAlgorithm(MetaLearningAlgorithm metaLearningAlgorithm) {
        this.metaLearningAlgorithm = metaLearningAlgorithm;
    }

    @Override
    public FeatureSelectionResult getFeatureSelectionResult() {
        return featureSelectionResult;
    }

    @Override
    public void setFeatureSelectionResult(FeatureSelectionResult featureSelectionResult) {
        this.featureSelectionResult = featureSelectionResult;
    }

    public TreeMap<String, Double> getProperties() {
        return properties;
    }

    public void setProperties(TreeMap<String, Double> properties) {
        this.properties = properties;
    }

    @Override
    public DBObject getDBObject() {
        return ImplementationManager.couchdb.object(String.format("LR:%s:%s", featureSelectionResult == null ? null : featureSelectionResult.getId().replace("::", ":"), metaLearningAlgorithm == null ? null : metaLearningAlgorithm.getName()))
                .put("featureSelectionResult", featureSelectionResult == null ? null : featureSelectionResult.getId())
                .put("metaLearningAlgorithm", metaLearningAlgorithm == null ? null : metaLearningAlgorithm.getName())
                .put("properties", properties);
    }

    @Override
    public BaseMetaLearningResult loadFromCouchDB(String name) {
        return loadFromCouchDB(ImplementationManager.couchdb.object("LR:" + name).load());
    }

    @Override
    public BaseMetaLearningResult loadFromCouchDB(DBObject dbObject) {
        setId(dbObject.getId());

        properties = new TreeMap<>();
        //noinspection unchecked
        ((Map<String, Object>) dbObject.getObject("properties")).entrySet().forEach(e -> properties.put(e.getKey(), ((Number)e.getValue()).doubleValue()));
        String s = dbObject.getString("featureSelectionResult", "SR::");
        setSelector(s.substring(s.lastIndexOf(':') + 1));
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }
}
