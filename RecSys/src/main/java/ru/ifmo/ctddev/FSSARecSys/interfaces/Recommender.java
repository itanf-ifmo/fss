package ru.ifmo.ctddev.FSSARecSys.interfaces;

import ru.ifmo.ctddev.FSSARecSys.AEARRComputers;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;
import java.util.Map;


public interface Recommender extends AEARRComputers {
    List<Pair<Double, String>> recommend(Dataset dataset);
    Map<String, Object> getOptions();
    String getId();

    /**
     * @param n - how many elements need to be recommended.
     */
    default String[] recommend(Dataset dataset, int n) {
        return recommend(dataset).stream().map(p -> p.second).limit(n).toArray(String[]::new);
    }
}
