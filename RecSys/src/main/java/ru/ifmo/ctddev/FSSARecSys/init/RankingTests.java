package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender;
import ru.ifmo.ctddev.FSSARecSys.recsys.OracleRecommender;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RankingTests {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final double beta = 0.1;
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final CouchDB resultsDB = new CouchDB(DB_URL, "results-tmp-ranking");
    private static final List<BaseDataset> datasets;
    private static final List<ClassifierAlgorithm> mlas = new ArrayList<>(ClassifierAlgorithm.getAll(mainDB).values());

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

        List<Pair<String, List<Pair<String, Map<String, Double>>>>> list = Arrays.stream(ids).parallel().map(mla -> Pair.of(mla.getName(),
                Arrays.stream(ids).parallel().map(mla2 -> Pair.of(mla2.getName(),
                        saveResults(mla, mla2, 3, 3)
                )).collect(Collectors.toList())
        )).collect(Collectors.toList());


        System.out.println("pre");
        System.out.print("Classifier," + "NotRanked");
        for (ClassifierAlgorithm id : ids) {
            System.out.print("," + id.getName().replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));
        }
        System.out.println();
        for (Pair<String, List<Pair<String, Map<String, Double>>>> stringListPair : list) {
            System.out.print(stringListPair.first.replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));

            System.out.print(',');
            System.out.print(String.format("%.3f", stringListPair.second.get(0).second.get("not")).replace(',', '.'));

            for (Pair<String, Map<String, Double>> pair : stringListPair.second) {
                Double result = pair.second.get("pre");
                System.out.print(',');
                System.out.print(String.format("%.3f", result).replace(',', '.'));
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("pos");

        System.out.print("Classifier," + "NotRanked");
        for (ClassifierAlgorithm id : ids) {
            System.out.print("," + id.getName().replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));
        }
        System.out.println();
        for (Pair<String, List<Pair<String, Map<String, Double>>>> stringListPair : list) {
            System.out.print(stringListPair.first.replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));

            System.out.print(',');
            System.out.print(String.format("%.3f", stringListPair.second.get(0).second.get("not")).replace(',', '.'));

            for (Pair<String, Map<String, Double>> pair : stringListPair.second) {
                Double result = pair.second.get("pos");
                System.out.print(',');
                System.out.print(String.format("%.3f", result).replace(',', '.'));
            }
            System.out.println();
        }


        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("all");

        System.out.println("Classifier,NotRanked,Best,Pre,Post");
        for (Pair<String, List<Pair<String, Map<String, Double>>>> stringListPair : list) {
            System.out.print(stringListPair.first.replaceAll("BayesNet", "BN").replaceAll("NaiveBayes", "NB"));

            System.out.print(',');
            System.out.print(String.format("%.3f", stringListPair.second.get(0).second.get("not")).replace(',', '.'));

            System.out.print(',');
            System.out.print(String.format("%.3f", stringListPair.second.get(0).second.get("r1")).replace(',', '.'));

            double maxPos = 0;
            double maxPre = 0;

            for (Pair<String, Map<String, Double>> pair : stringListPair.second) {
                if (pair.second.get("pos") > maxPos) {
                    maxPos = pair.second.get("pos");
                }
                if (pair.second.get("pre") > maxPre) {
                    maxPre = pair.second.get("pre");
                }
            }
            System.out.print(',');
            System.out.print(String.format("%.3f", maxPre).replace(',', '.'));
            System.out.print(',');
            System.out.print(String.format("%.3f", maxPos).replace(',', '.'));
            System.out.println();
        }
//        ClassifierAlgorithm mla = ClassifierAlgorithm.getAll(mainDB).get("J48");
//        Recommender recommender = new DistRecommender(mla.getName(), 5, beta, new SimpleNearestDatasetSearcher(mainDB));
//        Dataset dataset = datasets.get(10);
//        double scoreNot = FeaturesRankAggregator.getNotRanked(mla, beta).evaluateToAEARR(dataset, recommender.recommend(dataset, 5)).first;
//        System.out.println(scoreNot);
//
//
//        System.exit(1);

//        mlas.parallelStream().forEach(mla -> saveResults(mla, 3, 3));

//        ClassifierAlgorithm mla = ClassifierAlgorithm.getAll(mainDB).get("J48");
//
        IntStream.range(1, 11).parallel().forEach(i -> saveResults(ClassifierAlgorithm.getAll(mainDB).get("J48"), ClassifierAlgorithm.getAll(mainDB).get("J48"), 3 , i));
    }

    private static Map<String, Double> saveResults(ClassifierAlgorithm mla, ClassifierAlgorithm mla2, int nearests, int toAggregate) {
        Recommender recommenderO = new OracleRecommender(mla.getName(), beta, mainDB);
        Recommender recommender = new DistRecommender(mla.getName(), beta, new SimpleNearestDatasetSearcher(mainDB, nearests));

        List<DBObject> collect = datasets.parallelStream().map(dataset -> {
            String id = "RT:" + mla.getName() + ":o[" + mla2.getName() + "]:n" + String.valueOf(nearests) + ":l" + String.valueOf(toAggregate) + ":" + dataset.getName();

            if (resultsDB.object(id).exists()) {
                return resultsDB.object(id).load();
            }

            double scoreNot = FeaturesRankAggregator.getNotRanked(mla, beta).evaluateToAEARR(dataset, recommender.recommend(dataset, toAggregate)).first;
            double scorePre = FeaturesRankAggregator.getPreRanked(mla, beta, mla2).evaluateToAEARR(dataset, recommender.recommend(dataset, toAggregate)).first;
            double scorePos = FeaturesRankAggregator.getPostRanked(mla, beta, mla2).evaluateToAEARR(dataset, recommender.recommend(dataset, toAggregate)).first;

            double scoreR1 = FeaturesRankAggregator.getNotRanked(mla, beta).evaluateToAEARR(dataset, recommender.recommend(dataset, 1)).first;
            double scoreBestR1 = FeaturesRankAggregator.getNotRanked(mla, beta).evaluateToAEARR(dataset, recommenderO.recommend(dataset, 1)).first;

            System.out.println("Extracted: " + id);

            return resultsDB.object(id)
                    .put("not", scoreNot)
                    .put("pre", scorePre)
                    .put("pos", scorePos)
                    .put("r1", scoreR1)
                    .put("best", scoreBestR1)
                    .put("dataset", dataset.getName())
                    .put("beta", beta)
                    .put("mla-main", mla.getName())
                    .put("mla-order", mla2.getName())
                    .save();
        }).collect(Collectors.toList());

        Map<String, Double> map = new TreeMap<>();

        for (DBObject dbObject : collect) {
            for (String key : new String[]{
                    "not",
                    "pre",
                    "pos",
                    "r1",
                    "best",
            }) {
                map.put(key, map.getOrDefault(key, 0.) + dbObject.getDouble(key));
            }
        }

        for (String key : new String[]{
                "not",
                "pre",
                "pos",
                "r1",
                "best",
        }) {
            map.put(key, map.getOrDefault(key, 0.) / datasets.size());
        }

        return map;
    }
}
