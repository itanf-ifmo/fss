package ru.ifmo.ctddev.FSSARecSys.nds;


import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.ArrayList;
import java.util.List;


/**
 * Class for searching nearest {@link Dataset} to given one
 */
public class ThisNearestDatasetSearcher implements NearestDatasetSearcher {
    /**
     * This method counting distances between other and given {@link Dataset}.
     * Then adds Pairs of distances and DataSets to PriorityQueue
     * Finally returning top <code>n</code> of Pairs
     *
     * Note that this algorithm already exclude given dataset from collections of others.
     *
     * @return ordered Pairs of distances and {@link Dataset} truncated or padded with <code>nulls</code>
     * to obtain the specified length
     */
    public List<Pair<Double, Dataset>> search(Dataset dataset) {
        ArrayList<Pair<Double, Dataset>> list = new ArrayList<>(1);
        list.add(Pair.of(1., dataset));
        assert list.size() == 1;
        return list;
    }

    @Override
    public String getId() {
        return "T-NDS";
    }
}
