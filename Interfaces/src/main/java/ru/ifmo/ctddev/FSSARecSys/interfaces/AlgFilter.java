package ru.ifmo.ctddev.FSSARecSys.interfaces;

public interface AlgFilter {
    /**
     * @param namesOfRecommendedFSSAlgorithms list of recommended feature subset selection algorithms
     * @param dataset what we need to process
     * @return list of names of feature subset selection algorithms that should be used for rank aggregation
     */
    String[] filter(Dataset dataset);

    /**
     * @return filter name and it's parameters.
     */
    String getId();
}
