package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.filters.FuncFilter;
import ru.ifmo.ctddev.FSSARecSys.filters.util.FssasDistancesCache;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class OracleFiltering {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final CouchDB resultsDB = new CouchDB(DB_URL, "variate-gamma");
    private static final Map<String, ClassifierAlgorithm> mlas = ClassifierAlgorithm.getAll(mainDB);
    private static final List<BaseDataset> datasets;
    private static final String[] allFssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).toArray(String[]::new);

    static {
        List<BaseDataset> datasets_ = BaseDataset.getAll(mainDB);
        Collections.shuffle(datasets_, new Random(20));
        datasets = datasets_.stream().limit(30).collect(Collectors.toList());
    }

    public static void main(String[] ignore) {
        System.out.println("Datasets: " + String.valueOf(datasets.size()));
        double beta = 0.1;
        int n = 3;
        ClassifierAlgorithm mla = mlas.get("PART");
        NearestDatasetSearcher nds = new SimpleNearestDatasetSearcher(mainDB, 3);

        System.out.println("Starting");
        FeaturesRankAggregator aggregator = FeaturesRankAggregator.getNotRanked(mla, beta);

        for (int delta = 100; delta >= 0; delta -= 5) {
            final double d = delta / 100.0;

            datasets.parallelStream().forEach(dataset -> {
                String id = "FR:" + mla.getName() + ":l" + String.valueOf(n) + ":g" + String.valueOf(d) + ":" + dataset.getName();

                if (resultsDB.object(id).exists()) {
                    return;
                }

                double Fh = aggregator.evaluateToAEARR(dataset, new FuncFilter(
                        n,
                        nds,
                        FssasDistancesCache.cosine(),
                        allFssas,
                        mla.getName(),
                        beta,
                        d
                ).filter(dataset)).first;

                double Fc = aggregator.evaluateToAEARR(dataset, new FuncFilter(
                        n,
                        nds,
                        FssasDistancesCache.hamming(),
                        allFssas,
                        mla.getName(),
                        beta,
                        d
                ).filter(dataset)).first;

                double orig = aggregator.evaluateToAEARR(dataset, new DistRecommender(
                        mla.getName(),
                        beta,
                        nds
                ).recommend(dataset, n)).first;


                resultsDB.object(id)
                        .put("fc", Fc)
                        .put("fh", Fh)
                        .put("orig", orig)
                        .put("dataset", dataset.getName())
                        .put("beta", beta)
                        .put("mal", mla.getName())
                        .put("gamma", d)
                        .put("n", n)
                        .save();
            });

            System.out.println(d);
        }
    }
}
