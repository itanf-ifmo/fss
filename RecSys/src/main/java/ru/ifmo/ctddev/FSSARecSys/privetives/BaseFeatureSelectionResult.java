package ru.ifmo.ctddev.FSSARecSys.privetives;

import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("WeakerAccess")
public class BaseFeatureSelectionResult implements FeatureSelectionResult {
    private static final CouchDB selectionResults = new CouchDB("https://couchdb.home.tanfilyev.ru", "selection-results");

    private int[] selectedFeatures;
    /**
     * selection time in seconds
     */
    private double selectionTime;
    private Dataset dataset;
    private FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm;

    private Map<String, MetaLearningResult> map = new TreeMap<>();
    private String id;
    private String mlaPreOrdered = null;

    // cache
    @SuppressWarnings("unused")
    private static Map<String, BaseFeatureSelectionResult> cache = null;
    private static final String syncCache = "";
    private String name;
    private double preOrderTime = -1;

    public BaseFeatureSelectionResult(int[] selectedFeatures, double selectionTime, Dataset dataset, FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm, String mlaPreOrdered) {
        this.selectedFeatures = selectedFeatures;
        this.selectionTime = selectionTime;
        this.dataset = dataset;
        this.featureSubsetSelectionAlgorithm = featureSubsetSelectionAlgorithm;
        this.mlaPreOrdered = mlaPreOrdered;
    }

    public void setMlaPreOrdered(String mlaPreOrdered) {
        this.mlaPreOrdered = mlaPreOrdered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BaseFeatureSelectionResult() {}
    public BaseFeatureSelectionResult(int[] selectedFeatures, double selectionTime, Dataset dataset, FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm) {
        this(selectedFeatures, selectionTime, dataset, featureSubsetSelectionAlgorithm, null);
    }

    @Override
    public int[] getSelectedFeatures() {
        return selectedFeatures;
    }

    @Override
    public void setSelectedFeatures(int[] selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    @Override
    public double getSelectionTime() {
        return selectionTime;
    }

    @Override
    public void setSelectionTime(double selectionTime) {
        this.selectionTime = selectionTime;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public FeatureSubsetSelectionAlgorithm getFeatureSubsetSelectionAlgorithm() {
        return featureSubsetSelectionAlgorithm;
    }

    @Override
    public void setFeatureSubsetSelectionAlgorithm(FeatureSubsetSelectionAlgorithm featureSubsetSelectionAlgorithm) {
        this.featureSubsetSelectionAlgorithm = featureSubsetSelectionAlgorithm;
    }

    @Override
    public void setResults(String metaLearningAlgorithmName, MetaLearningResult metaLearningResult) {
        map.put(metaLearningAlgorithmName, metaLearningResult);
    }

    public String getKey() {
        return "SR:" + (mlaPreOrdered == null ? ":" : mlaPreOrdered + ":");
    }

    @Override
    public synchronized ClassificationResult getResult(String metaLearningAlgorithmName) {
        ClassificationResult a = ClassificationResult.getAll().get("LR:" + getId().replace("::", ":") + ":" + metaLearningAlgorithmName);
        a.setFeatureSelectionResult(this);
        return a;
    }

    public String toString() {
        return String.format("FSS-Result<%s : %s>", dataset, featureSubsetSelectionAlgorithm);
    }

    @Override
    public DBObject getDBObject(DB db) {
        return db.object(getKey() + dataset.getName() + ":" + featureSubsetSelectionAlgorithm.getName())
                .put("selectedFeatures", selectedFeatures)
                .put("selectionTime", selectionTime)
                .put("dataset", dataset.getName())
                .put("mlaPreOrdered", mlaPreOrdered)
                .put("preOrderTime", preOrderTime)
                .put("fssa", featureSubsetSelectionAlgorithm.getName());
    }

    @Override
    public DBObject getDBObject() {
        return getDBObject(ImplementationManager.couchdb);
    }

    @Override
    public BaseFeatureSelectionResult loadFromCouchDB(String name) {
        return loadFromCouchDB(ImplementationManager.couchdb.object(getKey() + name).load());
    }


    @Override
    public BaseFeatureSelectionResult loadFromCouchDB(DBObject dbObject) {
        setId(dbObject.getId());
        //noinspection unchecked
        setSelectedFeatures(((ArrayList<Integer>) dbObject.getObject("selectedFeatures")).stream().mapToInt(i -> i).toArray());
        setSelectionTime(dbObject.getDouble("selectionTime"));
        setPreOrderTime(dbObject.getDouble("preOrderTime", -1));
        setMlaPreOrdered(dbObject.getString("mlaPreOrdered", null));
        setName(dbObject.getString("fssa"));
        return this;
    }

    public synchronized static Map<String, BaseFeatureSelectionResult> getAll() {
        return getAll(selectionResults);
    }

    public synchronized static Map<String, BaseFeatureSelectionResult> getAll(DB db) {
        synchronized (syncCache) {
            if (db == null) {
                db = selectionResults; // todo: hate this!!!
            }

            if (cache == null) {
                cache = db.getAll("SR:").stream().map(o -> new BaseFeatureSelectionResult().loadFromCouchDB(o)).collect(Collectors.toMap(BaseFeatureSelectionResult::getId, Function.identity()));
            }

            return cache;
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return featureSubsetSelectionAlgorithm.getName();  // todo??
    }

    public void setPreOrderTime(double preOrderTime) {
        this.preOrderTime = preOrderTime;
    }
}
