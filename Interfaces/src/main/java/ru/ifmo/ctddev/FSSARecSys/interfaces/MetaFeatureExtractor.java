package ru.ifmo.ctddev.FSSARecSys.interfaces;

public interface MetaFeatureExtractor {
    /**
     * @return name of meta-feature
     */
    String getName();

    /**
     * @return meta-feature value extracted from given dataset
     */
    double extract(Dataset dataset);
}

