package ru.ifmo.ctddev.FSSARecSys.interfaces;

import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;
import java.util.Map;

public interface DB {
    List<DBObject> getAll(String keyPrefix);

    DBObject object(String id);

    Map<String, Pair<Double, Double>> getMinMax();
}
