package ru.ifmo.ctddev.FSSARecSys.filters;

import ru.ifmo.ctddev.FSSARecSys.interfaces.AlgFilter;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;


public class EmptyAlgFilter implements AlgFilter {
    /**
     * @param dataset                         what we need to process
     * @return list of names of feature subset selection algorithms that should be used for rank aggregation
     */
    @Override
    public String[] filter(Dataset dataset) {
        return BaseFeatureSubsetSelectionAlgorithm.getAll().stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).toArray(String[]::new).clone();
    }

    /**
     * @return filter name and it's parameters.
     */
    @Override
    public String getId() {
        return "PassThroughFilter";
    }
}
