package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.filters.ClustererFilter;
import ru.ifmo.ctddev.FSSARecSys.filters.util.FssasDistancesCache;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender;

import java.util.*;
import java.util.stream.Collectors;

public class ClusteringTest {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final CouchDB resultsDB = new CouchDB(DB_URL, "results-tmp-f-clusters");
    private static final Map<String, ClassifierAlgorithm> mlas = ClassifierAlgorithm.getAll(mainDB);
    private static final List<BaseDataset> datasets;
    private static final String[] allFssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).toArray(String[]::new);

    static {
        List<BaseDataset> datasets_ = BaseDataset.getAll(mainDB);
        Collections.shuffle(datasets_, new Random(20));
        datasets = datasets_.stream().limit(30).collect(Collectors.toList());
    }

    private static final ClassifierAlgorithm[] ids = new ClassifierAlgorithm[]{
            ClassifierAlgorithm.getAll(mainDB).get("SVM"),
            ClassifierAlgorithm.getAll(mainDB).get("J48"),
            ClassifierAlgorithm.getAll(mainDB).get("PART"),
            ClassifierAlgorithm.getAll(mainDB).get("IBk"),
            ClassifierAlgorithm.getAll(mainDB).get("BayesNet"),
            ClassifierAlgorithm.getAll(mainDB).get("NaiveBayes"),
    };

    public static void main(String[] ignore) {
        double beta = 0.1;
        int n = 3;
        System.out.println("Starting");

        System.out.println("Classifier,Baseline,SH,SC,HH,HC");
        for (ClassifierAlgorithm mla : ids) {
            NearestDatasetSearcher nds = new SimpleNearestDatasetSearcher(mainDB, 3);
            FeaturesRankAggregator aggregator = FeaturesRankAggregator.getNotRanked(mla, beta);
            System.out.print(mla.getName().replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));

            Map<String, Double> map = saveResults(mla, n, beta, aggregator, nds);


            for (String key : new String[]{
                    "orig",
                    "sh",
                    "sc",
                    "hh",
                    "hc",
            }) {
                System.out.print(',');
                System.out.print(String.format("%.3f", map.get(key)).replace(',', '.'));
            }
            System.out.println();
        }
    }

    private static Map<String, Double> saveResults(ClassifierAlgorithm mla, int toAggregate, double beta, FeaturesRankAggregator aggregator, NearestDatasetSearcher nds) {
        List<DBObject> collect = datasets.parallelStream().map(dataset -> {
            String id = "FR-cluster:" + mla.getName() + ":l" + String.valueOf(toAggregate) + ":" + dataset.getName();

            if (resultsDB.object(id).exists()) {
                return resultsDB.object(id).load();
            }

            double sh = aggregator.evaluateToAEARR(dataset, ClustererFilter.simpleKMeansClusterFilter(
                    toAggregate,
                    nds,
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta
            ).filter(dataset)).first;
            double sc = aggregator.evaluateToAEARR(dataset, ClustererFilter.simpleKMeansClusterFilter(
                    toAggregate,
                    nds,
                    FssasDistancesCache.cosine(),
                    allFssas,
                    mla.getName(),
                    beta
            ).filter(dataset)).first;
            double hh = aggregator.evaluateToAEARR(dataset, ClustererFilter.hierarchicalClusterFilter(
                    toAggregate,
                    nds,
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta
            ).filter(dataset)).first;
            double hc = aggregator.evaluateToAEARR(dataset, ClustererFilter.hierarchicalClusterFilter(
                    toAggregate,
                    nds,
                    FssasDistancesCache.cosine(),
                    allFssas,
                    mla.getName(),
                    beta
            ).filter(dataset)).first;

            double orig = aggregator.evaluateToAEARR(dataset, new DistRecommender(
                            mla.getName(),
                            beta,
                            nds
                    ).recommend(dataset, toAggregate)).first;

            return resultsDB.object(id)
                    .put("sh", sh)
                    .put("sc", sc)
                    .put("hh", hh)
                    .put("hc", hc)
                    .put("orig", orig)
                    .put("dataset", dataset.getName())
                    .put("beta", beta)
                    .put("n", toAggregate)
                    .save();
        }).collect(Collectors.toList());

        Map<String, Double> map = new TreeMap<>();

        for (DBObject dbObject : collect) {
            for (String key : new String[]{
                    "sh",
                    "sc",
                    "hh",
                    "hc",
                    "orig",
            }) {
                map.put(key, map.getOrDefault(key, 0.) + dbObject.getDouble(key));
            }
        }

        for (String key : new String[]{
                "sh",
                "sc",
                "hh",
                "hc",
                "orig",
        }) {
            map.put(key, map.getOrDefault(key, 0.) / datasets.size());
        }

        return map;
    }
}
