package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.filters.FuncFilter;
import ru.ifmo.ctddev.FSSARecSys.filters.util.FssasDistancesCache;
import ru.ifmo.ctddev.FSSARecSys.interfaces.AlgFilter;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.nds.ClassifierDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender;
import ru.ifmo.ctddev.FSSARecSys.recsys.OracleRecommender;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestRun {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    protected static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    protected static final CouchDB resultsDB = new CouchDB(DB_URL, "results-tmp");
    protected static final Map<String, ClassifierAlgorithm> mlas = ClassifierAlgorithm.getAll(mainDB);
    protected static final List<BaseDataset> datasets;
    protected static final String[] allFssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB).stream().map(BaseFeatureSubsetSelectionAlgorithm::getName).toArray(String[]::new);

    static {
        List<BaseDataset> datasets_ = BaseDataset.getAll(mainDB);
        Collections.shuffle(datasets_, new Random(20));
        datasets = datasets_.stream().limit(20).collect(Collectors.toList());
    }

    protected static final ClassifierAlgorithm[] ids = new ClassifierAlgorithm[]{
            ClassifierAlgorithm.getAll(mainDB).get("SVM"),
            ClassifierAlgorithm.getAll(mainDB).get("J48"),
            ClassifierAlgorithm.getAll(mainDB).get("PART"),
            ClassifierAlgorithm.getAll(mainDB).get("IBk"),
            ClassifierAlgorithm.getAll(mainDB).get("BayesNet"),
            ClassifierAlgorithm.getAll(mainDB).get("NaiveBayes"),
    };

    protected static FeaturesRankAggregator getAggregator(ClassifierAlgorithm mla, double beta) {
        if (mla.getName().equals("SVM")) {
            return FeaturesRankAggregator.getPostRanked(mla, beta, mlas.get("SVM"));
        }
        if (mla.getName().equals("J48")) {
            return FeaturesRankAggregator.getPostRanked(mla, beta, mlas.get("J48"));
        }
        if (mla.getName().equals("PART")) {
            return FeaturesRankAggregator.getPostRanked(mla, beta, mlas.get("J48"));
        }
        if (mla.getName().equals("IBk")) {
            return FeaturesRankAggregator.getPostRanked(mla, beta, mlas.get("BayesNet"));
        }
        if (mla.getName().equals("BayesNet")) {
            return FeaturesRankAggregator.getPostRanked(mla, beta, mlas.get("BayesNet"));
        }
        if (mla.getName().equals("NaiveBayes")) {
            return FeaturesRankAggregator.getPostRanked(mla, beta, mlas.get("IBk"));
        }
        throw new AssertionError("Should be not reached");
    }

    protected static AlgFilter getFilter(ClassifierAlgorithm mla, int n, double beta) {
        if (mla.getName().equals("SVM")) {
            return new FuncFilter(
                    n,
                    ClassifierDatasetSearcher.SVM(3),
                    FssasDistancesCache.cosine(),
                    allFssas,
                    mla.getName(),
                    beta,
                    0.6
            );
        }
        if (mla.getName().equals("J48")) {
            return new FuncFilter(
                    n,
                    ClassifierDatasetSearcher.SVM(3),
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta,
                    0.6
            );
        }
        if (mla.getName().equals("PART")) {
            return new FuncFilter(
                    n,
                    ClassifierDatasetSearcher.SVM(3),
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta,
                    0.6
            );
        }
        if (mla.getName().equals("IBk")) {
            return new FuncFilter(
                    n,
                    ClassifierDatasetSearcher.SVM(3),
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta,
                    0.6
            );
        }
        if (mla.getName().equals("BayesNet")) {
            return new FuncFilter(
                    n,
                    ClassifierDatasetSearcher.SVM(3),
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta,
                    0.6
            );
        }
        if (mla.getName().equals("NaiveBayes")) {
            return new FuncFilter(
                    n,
                    ClassifierDatasetSearcher.SVM(3),
                    FssasDistancesCache.hamming(),
                    allFssas,
                    mla.getName(),
                    beta,
                    0.6
            );
        }
        throw new AssertionError("Should be not reached");
    }

    @SuppressWarnings("Duplicates")
    protected static Map<String, Double> run(ClassifierAlgorithm mla, int n, double beta) {
        Recommender recommender = new DistRecommender(mla.getName(), beta, new SimpleNearestDatasetSearcher(mainDB, 3));
        Recommender recommenderO = new OracleRecommender(mla.getName(), beta, mainDB);

        List<DBObject> collect = datasets.parallelStream().map(dataset -> {
            String id = "Re:" + mla.getName() + ":l" + String.valueOf(n) + ":b" + String.valueOf(beta) + ":" + dataset.getName();

            if (resultsDB.object(id).exists()) {
                return resultsDB.object(id).load();
            }

            double score = getAggregator(mla, beta).evaluateToAEARR(dataset, getFilter(mla, n, beta).filter(dataset)).first;

            double r1 = FeaturesRankAggregator.getNotRanked(mla, beta).evaluateToAEARR(dataset, recommenderO.recommend(dataset, 1)).first;
            double orig = FeaturesRankAggregator.getNotRanked(mla, beta).evaluateToAEARR(dataset, recommender.recommend(dataset, n)).first;

            System.out.println("Extracted: " + id);

            return resultsDB.object(id)
                    .put("score", score)
                    .put("r1", r1)
                    .put("orig", orig)
                    .put("dataset", dataset.getName())
                    .put("n", n)
                    .put("beta", beta)
                    .put("mla", mla.getName())
                    .save();
        }).collect(Collectors.toList());

        Map<String, Double> map = new TreeMap<>();

        for (DBObject dbObject : collect) {
            for (String key : new String[]{
                    "score",
                    "r1",
                    "orig",
            }) {
                map.put(key, map.getOrDefault(key, 0.) + dbObject.getDouble(key));
            }
        }

        for (String key : new String[]{
                    "score",
                    "r1",
                    "orig",
        }) {
            map.put(key, map.getOrDefault(key, 0.) / datasets.size());
        }

        return map;
    }

    public static void main(String[] ignore) {
        double beta = 0.1;
        int n = 3;

        ClassifierAlgorithm mla = ClassifierAlgorithm.getAll(mainDB).get("PART");
        IntStream.range(0, 26).parallel().forEach(b -> run(mla, n, ((double) b) / 100));

//        List<Pair<ClassifierAlgorithm, Map<String, Double>>> col = Arrays.stream(ids).parallel().map(mla -> Pair.of(mla, run(mla, n, beta))).collect(Collectors.toList());
//
//        System.out.println("# common-all.csv");
//        System.out.println();
//        System.out.println("Classifier,Baseline,Best,New");
//        for (Pair<ClassifierAlgorithm, Map<String, Double>> pair : col) {
//            System.out.print(pair.first.getName().replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));
//            System.out.print(',');
//            System.out.print(String.format("%.3f", pair.second.get("orig")).replace(',', '.'));
//            System.out.print(',');
//            System.out.print(String.format("%.3f", pair.second.get("r1")).replace(',', '.'));
//            System.out.print(',');
//            System.out.print(String.format("%.3f", pair.second.get("score")).replace(',', '.'));
//            System.out.println();
//        }
    }
}
