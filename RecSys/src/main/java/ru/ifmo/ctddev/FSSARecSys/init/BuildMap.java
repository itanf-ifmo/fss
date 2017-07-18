package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.filters.util.FssasDistancesCache;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BuildMap {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final CouchDB resultsDB = new CouchDB(DB_URL, "results-all");
    private static final List<ClassifierAlgorithm> mlas = new ArrayList<>(ClassifierAlgorithm.getAll(mainDB).values());
    private static final List<BaseFeatureSubsetSelectionAlgorithm> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB);
    private static final List<BaseDataset> datasets;
    private static final ClassifierAlgorithm mla = ClassifierAlgorithm.getAll(mainDB).get("SVM");
    private static final double beta = 0.1;
    private static final BaseDataset dataset;

    static {
        List<BaseDataset> datasets_ = BaseDataset.getAll(mainDB);
        Collections.shuffle(datasets_, new Random(311));
        datasets = datasets_.stream().limit(4).collect(Collectors.toList());
        dataset = datasets.get(3);
        System.out.println(dataset.getName());
    }

    public static void main(String[] ignore) {

        for (int i = 0; i < fssas.size(); i++) {
            System.out.print("d,");
            System.out.print(fssas.get(i).getName());

            if (i < fssas.size() - 1) {
                System.out.print(',');
            }
        }

        System.out.println();


        for (BaseFeatureSubsetSelectionAlgorithm fssaA : fssas) {
            System.out.print(fssaA.getName());

            List<Pair<Double, Double>> list = fssas.parallelStream().map(fssaB -> Pair.of(dist(fssaA, fssaB, dataset), score(fssaA, fssaB, dataset))).collect(Collectors.toList());

            for (Pair<Double, Double> pair : list) {
                System.out.print(",");
                System.out.print(pair.first);
                System.out.print(",");
                System.out.print(pair.second);
            }

            System.out.println();
        }
    }

    private static double dist(BaseFeatureSubsetSelectionAlgorithm fssaA, BaseFeatureSubsetSelectionAlgorithm fssaB, Dataset dataset) {
        return FssasDistancesCache.cosine().getDistances(dataset, fssaA.getName(), fssaB.getName());
    }

    private static double score(BaseFeatureSubsetSelectionAlgorithm fssaA, BaseFeatureSubsetSelectionAlgorithm fssaB, Dataset dataset) {
        FeaturesRankAggregator aggregator = FeaturesRankAggregator.getNotRanked(mla, beta);
        return aggregator.evaluateToAEARR(dataset, new String[]{
                fssaA.getName(),
                fssaB.getName()
        }).first;
    }
}
