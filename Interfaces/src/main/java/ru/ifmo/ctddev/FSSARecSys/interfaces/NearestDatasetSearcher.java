package ru.ifmo.ctddev.FSSARecSys.interfaces;

import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;

/**
 * This interface provide method for searching nearest {@link Dataset} to given
 */
public interface NearestDatasetSearcher {
    /**
     * @return ordered Pairs of distances and {@link Dataset}
     */
    List<Pair<Double, Dataset>> search(Dataset dataset);

    String getId();
}
