package ru.ifmo.ctddev.FSSARecSys.privetives;

import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


public class BaseDataset implements Dataset {
    private static List<BaseDataset> all;
    private String name;
    private Instances instances;
    private String taskType;
    private Map<String, Double> metaFeatures = new TreeMap<>();
    private Map<String, FeatureSelectionResult> map = new TreeMap<>();
    private boolean included;

    @SuppressWarnings("unused")
    public BaseDataset() {
    }

    public BaseDataset(String name, URL arrfFile, String taskType) {
        this.name = name;
        setInstances(arrfFile);
        this.taskType = taskType;
    }

    public BaseDataset(String name, Instances instances) {
        this.name = name;
        setInstances(instances);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Instances getInstances() {
        return instances;
    }

    /**
     * get instances with preselected features.
     */
    @Override
    public Instances getInstances(FeatureSelectionResult featureSelectionResult) {
        return getInstances(featureSelectionResult.getSelectedFeatures());
    }

    @Override
    public Instances getInstances(int[] selectedFeatures) {
        Remove removeFilter = new Remove();
        removeFilter.setInvertSelection(true);

        removeFilter.setAttributeIndicesArray(addClassAttributeIndexToArray(selectedFeatures));

        try {
            removeFilter.setInputFormat(instances);
            Instances selectedInstances = Filter.useFilter(instances, removeFilter);
            setClassAttribute(selectedInstances);
            return selectedInstances;
        } catch (Exception e) {
            throw new Error(e);
        }
    }


    private int[] addClassAttributeIndexToArray(int[] array) {
        int classIndex = instances.classIndex();
        for (int i : array) {
            if (i == classIndex) {
                return array;
            }
        }

        int[] result = new int[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = classIndex;
        return result;
    }

    @Override
    public void setInstances(URL url) {
        try {
            instances = new Instances(new FileReader(new File(url.getFile())));
            setClassAttribute(instances);
            removeInstancesWithMissingClasses();
            convertNumericClassToNominal();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * removing instances that has missing class value
     * Some of class selection algorithms couldn't missing class values.
     */
    private void removeInstancesWithMissingClasses() {
        Stack<Integer> toRemove = new Stack<>();

        for (int i = 0; i < instances.numInstances(); i++) {
            if (instances.instance(i).classIsMissing()) {
                toRemove.push(i);
            }
        }

        if (!toRemove.isEmpty()) {
            // logger?
            System.out.println(String.format("Removing %s instances with missing class value for %s", toRemove.size(), toString()));
        }

        while (!toRemove.isEmpty()) {
            instances.remove((int) toRemove.pop());
        }
    }

    private static Instances setClassAttribute(Instances instances) {
        if (instances.classIndex() == -1) {
            instances.setClassIndex(searchClassInstance(instances));
        }

        return instances;
    }

    private static int searchClassInstance(Instances instances) {
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (instances.attribute(i).name().equalsIgnoreCase("class")) {
                return i;
            }
        }

        // set last if no attribute with name 'class' found
        return instances.numAttributes() - 1;
    }

    @Override
    public void setInstances(Instances instances) {
        this.instances = instances;
    }

    @Override
    public Map<String, Double> getMetaFeatures() {
        return metaFeatures;
    }

    @Override
    public void setMetaFeatures(Map<String, Double> metaFeatures) {
        this.metaFeatures = metaFeatures;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    @Override
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public void setResults(String fssAlgorithmName, FeatureSelectionResult featureSelectionResult) {
        map.put(fssAlgorithmName, featureSelectionResult);
    }

    public void setIncluded(boolean flag) {
        this.included = flag;
    }

    public boolean getIncluded() {
        return this.included;
    }

    @Override
    public FeatureSelectionResult getResult(String fssAlgorithmName) {
        return getResult(fssAlgorithmName, "");
    }

    @Override
    public synchronized FeatureSelectionResult getResult(String fssAlgorithmName, String mlaName) {
        BaseFeatureSelectionResult a = BaseFeatureSelectionResult.getAll().get("SR:" + mlaName + ":" + name + ":" + fssAlgorithmName);
        if (a == null) {
            System.out.println("!!!!!!!!!!!!!!! NPE:" + getName());
        }
        a.setDataset(this);
        a.setFeatureSubsetSelectionAlgorithm(BaseFeatureSubsetSelectionAlgorithm.get(fssAlgorithmName));
        return a;
    }

    @Override
    public String toString() {
        return String.format("<Dataset[%s]> i:%d f:%d c:%d", name, instances.numInstances(), instances.numAttributes(), instances.numClasses());
    }

    @Override
    public DBObject getDBObject() {
        return ImplementationManager.couchdb.object("DS:" + name)
                        .put("name", name)
                        .put("included", included)
                        .put("instances", instances.toString())
                        .put("taskType", taskType)
                        .put("metaFeatures", getMetaFeatures());
    }

    @Override
    public BaseDataset loadFromCouchDB(String name) {
        return loadFromCouchDB(ImplementationManager.couchdb.object("DS:" + name).load());
    }

    @Override
    public BaseDataset loadFromCouchDB(DBObject dbObject) {
        setName(dbObject.getString("name"));
        setIncluded(dbObject.getBoolean("included", true));

        try {
            setInstances(new Instances(new StringReader(dbObject.getString("instances"))));
            setClassAttribute(getInstances());
            removeInstancesWithMissingClasses();
            convertNumericClassToNominal();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        setTaskType(dbObject.getString("taskType"));
        @SuppressWarnings("unchecked")
        Map<Object, Object> a = (Map<Object, Object>) dbObject.getObject("metaFeatures", new TreeMap<>());

        try {
            setMetaFeatures((a.entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(), e -> ((Number) e.getValue()).doubleValue()))));
        } catch (ClassCastException e) {
            throw new Error("Error in dataset: " + this.getName(), e);
        }
        return this;
    }

    public static List<BaseDataset> getAll() {
        return getAll(ImplementationManager.couchdb);
    }

    public synchronized static List<BaseDataset> getAll(CouchDB db) {
        if (all == null) {
            all = db.getAll("DS:").parallelStream().map(o -> new BaseDataset().loadFromCouchDB(o)).filter(BaseDataset::getIncluded).collect(Collectors.toList());
        }
        return all;
    }

    public synchronized static List<BaseDataset> getNotIncluded(CouchDB db) {
        return db.getAll("DS:").parallelStream().map(o -> new BaseDataset().loadFromCouchDB(o)).filter(d -> !d.getIncluded()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof BaseDataset && ((BaseDataset) other).getName().equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private void convertNumericClassToNominal() {
        if (instances.classAttribute().isNumeric()) {
            NumericToNominal convert = new NumericToNominal();

            try {
                convert.setOptions(new String[]{
                        "-R",
                        String.valueOf(instances.classIndex() + 1)
                });

                convert.setInputFormat(instances);
                instances = Filter.useFilter(instances, convert);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public double[] getMetaFeaturesVector(String[] metaFeaturesNames) {
        return Arrays.stream(metaFeaturesNames).mapToDouble(metaFeatures::get).toArray();
    }
}
