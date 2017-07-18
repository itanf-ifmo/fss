package ru.ifmo.ctddev.FSSARecSys.filters.util;

import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class FssasDistancesCache {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final CouchDB db = new CouchDB(DB_URL, "fssas-distances");
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final List<String> FSSAs = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());

    private final String distance;
    private final Map<String, Map<String, Map<String, Double>>> cache = new HashMap<>();

    private FssasDistancesCache(String distance) {
        this.distance = distance;
    }

    public static FssasDistancesCache cosine() {
        return new FssasDistancesCache("cosine");
    }

    public static FssasDistancesCache hamming() {
        return new FssasDistancesCache("hamming");
    }

    private static final BiFunction<double[], double[], Double> cosineSimilarity = (a, b) -> {
        assert a.length == b.length;

        double s = 0;
        double sa = 0;
        double sb = 0;

        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
            sa += a[i] * a[i];
            sb += b[i] * b[i];
        }

        return 1 - (s / (Math.sqrt(sa * sb)));
    };

    private static final BiFunction<double[], double[], Double> hammingDistance = (a, b) -> {
        assert a.length == b.length;

        double s = 0;

        for (int i = 0; i < a.length; i++) {
            s += a[i] == b[i] ? 0 : 1;
        }

        return s / a.length;
    };

    @SuppressWarnings({"Duplicates", "WeakerAccess"})
    public Double getDistances(Dataset dataset, String fssNameA, String fssNameB) {
        String id = "x:" + dataset.getName() + ":" + distance;

        if (!cache.containsKey(id)) {
            synchronized (cache) {
                if (!cache.containsKey(id)) {
                    if (!db.object(id).exists()) {
                        buildReport(dataset);
                    }

                    //noinspection unchecked
                    cache.put(id, (Map<String, Map<String, Double>>) db.object(id).load().getObject("data"));
                }
            }
        }

        return cache.get(id).get(fssNameA).get(fssNameB);
    }

    private void buildReport(Dataset dataset) {
        DBObject o;
        o = db.object("x:" + dataset.getName() + ":cosine");
        if (!o.exists()) {
            o.put("data", build(dataset, cosineSimilarity));
            o.save();
        }

        o = db.object("x:" + dataset.getName() + ":hamming");
        if (!o.exists()) {
            o.put("data", build(dataset, hammingDistance));
            o.save();
        }

        System.out.println("Distances for " + dataset + " counted");
    }

    private static TreeMap<String, TreeMap<String, Double>> build(Dataset dataset, BiFunction<double[], double[], Double> fun) {
        TreeMap<String, TreeMap<String, Double>> map = new TreeMap<>();

        for (String nameA : FSSAs) {
            map.put(nameA, new TreeMap<>());
            for (String nameB : FSSAs) {
                map.get(nameA).put(nameB, fun.apply(getFeatures(dataset, nameA), getFeatures(dataset, nameB)));
            }
        }

        return map;
    }

    @SuppressWarnings("Duplicates")  // todo
    private static double[] getFeatures(Dataset ds, String fssaName) {
        FeatureSelectionResult sr = ds.getResult(fssaName);
        double[] features = new double[ds.getFeaturesNumber()];
        int[] selectedFeatures = sr.getSelectedFeatures();

        Arrays.fill(features, 0);
        for (int selectedFeature : selectedFeatures) {
            features[selectedFeature] = 1;
        }

        return features;
    }

    public String getId() {
        return String.format("Dist<%s>", distance);
    }
}
