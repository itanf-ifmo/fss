package ru.ifmo.ctddev.FSSARecSys.privetives;


import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.*;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.core.Instances;
import weka.core.OptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.ifmo.ctddev.FSSARecSys.MLUtils.preRankInstances;


public class BaseFeatureSubsetSelectionAlgorithm implements FeatureSubsetSelectionAlgorithm {
    private static List<BaseFeatureSubsetSelectionAlgorithm> all;
    private String name;
    private ASSearch searcher;
    private String searchOptions;
    private ASEvaluation evaluator;
    private String evaluatorOptions;

    public AttributeSelection getAttributeSelection() {
        AttributeSelection attributeSelection = new AttributeSelection();
        if (searcher instanceof OptionHandler) {
            try {
                ((OptionHandler) searcher).setOptions(searchOptions.split(" "));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        if (evaluator instanceof OptionHandler) {
            try {
                ((OptionHandler) evaluator).setOptions(evaluatorOptions.split(" "));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        attributeSelection.setSearch(ClassUtils.load(ASSearch.class, searcher.getClass().getCanonicalName()));
        attributeSelection.setEvaluator(ClassUtils.load(ASEvaluation.class, evaluator.getClass().getCanonicalName()));

        // return runked result if possible
        attributeSelection.setRanking(true);

        // use cross validation with fixed seed.
        attributeSelection.setSeed(1);
        attributeSelection.setFolds(10);
        attributeSelection.setXval(true);

        return attributeSelection;
    }

    @Override
    public FeatureSelectionResult selectAttributes(Dataset dataset) {
        long startTime = System.currentTimeMillis();
        AttributeSelection attributeSelection = getAttributeSelection();

        try {
            attributeSelection.SelectAttributes(dataset.getInstances());
            double selectionTime = ((double) System.currentTimeMillis() - startTime) / 1000;
            return new BaseFeatureSelectionResult(attributeSelection.selectedAttributes(), selectionTime, dataset, this);

        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public FeatureSelectionResult preOrderAndSelectAttributes(MetaLearningAlgorithm mla, Dataset dataset) {
        if (!(mla instanceof ClassifierAlgorithm)) {
            System.err.println(dataset.toString() + '/' + this.getName());
            System.err.println("cust error");
            System.exit(1);
        }

        long startTimePre = System.currentTimeMillis();
        Pair<Instances,Integer[]> instancesMap = preRankInstances((ClassifierAlgorithm) mla, dataset);
        double preTime = ((double) System.currentTimeMillis() - startTimePre) / 1000;

        long startTime = System.currentTimeMillis();
        AttributeSelection attributeSelection = getAttributeSelection();

        try {
            attributeSelection.SelectAttributes(instancesMap.first);
            double selectionTime = ((double) System.currentTimeMillis() - startTime) / 1000;
            int[] attributes = Arrays.stream(attributeSelection.selectedAttributes()).map(i -> instancesMap.second[i]).toArray();
            BaseFeatureSelectionResult result = new BaseFeatureSelectionResult(attributes, selectionTime, dataset, this, mla.getName());
            result.setPreOrderTime(preTime);
            return result;

        } catch (Exception e) {
            throw new Error(dataset.toString() + '/' + this.getName(), e);
        }
    }

    // setters and getters

    /**
     * @return name of Feature Subset Selection Algorithm
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ASSearch getSearcher() {
        return searcher;
    }

    @Override
    public void setSearcher(ASSearch searcher) {
        this.searcher = searcher;
    }

    @Override
    public String getSearchOptions() {
        return searchOptions;
    }

    @Override
    public void setSearchOptions(String searchOptions) {
        this.searchOptions = searchOptions;
    }

    @Override
    public ASEvaluation getEvaluator() {
        return evaluator;
    }

    @Override
    public void setEvaluator(ASEvaluation evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String getEvaluatorOptions() {
        return evaluatorOptions;
    }

    @Override
    public void setEvaluatorOptions(String evaluatorOptions) {
        this.evaluatorOptions = evaluatorOptions;
    }

    @Override
    public String toString() {
        return getName();
//        return "<FSSAlgorithm[" + getName() + "] s:"
//                + getSearcher().getClass().getCanonicalName() + "(" + getSearchOptions() + ") e:"
//                + getEvaluator().getClass().getCanonicalName() + "(" + getEvaluatorOptions() + ")>";
    }

    @Override
    public boolean equals(Object other) {
        return hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public DBObject getDBObject() {
        return ImplementationManager.couchdb.object("FSS:" + name)
                .put("name", getName())
                .put("searcher", getSearcher().getClass().getCanonicalName())
                .put("evaluator", getEvaluator().getClass().getCanonicalName())
                .put("searchOptions", getSearchOptions())
                .put("evaluatorOptions", getEvaluatorOptions());
    }

    @Override
    public BaseFeatureSubsetSelectionAlgorithm loadFromCouchDB(String name) {
        return loadFromCouchDB(ImplementationManager.couchdb.object("FSS:" + name).load());
    }

    @Override
    public BaseFeatureSubsetSelectionAlgorithm loadFromCouchDB(DBObject dbObject) {
        setName(dbObject.getString("name"));
        setSearcher(dbObject.getClazz(ASSearch.class, "searcher"));
        setSearchOptions(dbObject.getString("searchOptions"));
        setEvaluator(dbObject.getClazz(ASEvaluation.class, "evaluator"));
        setEvaluatorOptions(dbObject.getString("evaluatorOptions"));
        return this;
    }

    public synchronized static List<BaseFeatureSubsetSelectionAlgorithm> getAll() {
        return getAll(ImplementationManager.couchdb);
    }

    public synchronized static List<BaseFeatureSubsetSelectionAlgorithm> getAll(CouchDB db) {
        if (all == null) {
            all = db.getAll("FSS:").parallelStream().map(o -> new BaseFeatureSubsetSelectionAlgorithm().loadFromCouchDB(o)).collect(Collectors.toList());
        }

        return all;
    }

    public static FeatureSubsetSelectionAlgorithm get(String fssAlgorithmName) {
        return all.stream().filter(p -> p.getName().equals(fssAlgorithmName)).findFirst().get();  // todo not optimal =(
    }
}
