package ru.ifmo.ctddev.FSSARecSys.interfaces;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;

public interface FeatureSubsetSelectionAlgorithm extends CouchDBSavable<FeatureSubsetSelectionAlgorithm> {
    /**
     * @return meta-feature value extracted from given dataset
     */
    FeatureSelectionResult selectAttributes(Dataset dataset);

    // getters & setters

    FeatureSelectionResult preOrderAndSelectAttributes(MetaLearningAlgorithm mla, Dataset dataset);

    /**
     * @return name of meta-feature
     */
    String getName();
    void setName(String name);

    ASSearch getSearcher();
    void setSearcher(ASSearch searcher);

    String getSearchOptions();
    void setSearchOptions(String searchOptions);

    ASEvaluation getEvaluator();
    void setEvaluator(ASEvaluation evaluator);

    String getEvaluatorOptions();
    void setEvaluatorOptions(String evaluatorOptions);


    AttributeSelection getAttributeSelection();
}
