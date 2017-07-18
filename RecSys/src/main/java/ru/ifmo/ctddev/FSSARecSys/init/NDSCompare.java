package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.nds.ClassifierDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender2;
import ru.ifmo.ctddev.FSSARecSys.recsys.OracleRecommender;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class NDSCompare {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final List<BaseDataset> datasets;

    static {
        List<BaseDataset> datasets_ = BaseDataset.getAll(mainDB);
        Collections.shuffle(datasets_, new Random(20));
        datasets = datasets_;//.stream().limit(2).collect(Collectors.toList());
    }

    private static final String[] ids = new String[]{
            "OR",
            "Simple",
            "SVM",
            "J48",
            "PART",
            "IBk",
            "BayesNet",
            "NaiveBayes",
    };

    public static void main(String[] ignore) {
        run("IBk");
        run("J48");
        run("PART");
        run("BayesNet");
        run("NaiveBayes");
        run("SVM");
    }

    private static void run(String mlaName) {
        double beta = 0.1;

        HashMap<String, Double> map = new HashMap<>();

        ClassifierAlgorithm mla = ClassifierAlgorithm.getAll().get(mlaName);
        FeaturesRankAggregator aggregator = FeaturesRankAggregator.getNotRanked(mla, beta);

        List<List<Pair<String, Double>>> ress = datasets.parallelStream().map(dataset -> {
            Recommender[] recommenders = new Recommender[]{
                    new OracleRecommender(mla.getName(), beta, mainDB),
                    new DistRecommender(mla.getName(), beta, new SimpleNearestDatasetSearcher(mainDB, 3)),
                    new DistRecommender2(mla.getName(), beta, ClassifierDatasetSearcher.SVM2(3)),
                    new DistRecommender2(mla.getName(), beta, ClassifierDatasetSearcher.IBk(3)),
                    new DistRecommender2(mla.getName(), beta, ClassifierDatasetSearcher.J48(3)),
                    new DistRecommender2(mla.getName(), beta, ClassifierDatasetSearcher.NaiveBayes(3)),
                    new DistRecommender2(mla.getName(), beta, ClassifierDatasetSearcher.BayesNet(3)),
                    new DistRecommender2(mla.getName(), beta, ClassifierDatasetSearcher.PART(3)),
            };

            List<Pair<String, Double>> collect = Arrays.stream(recommenders).parallel().map(recommender ->
                Pair.of(recommender.getId(), aggregator.evaluateToAEARR(dataset, recommender.recommend(dataset, 3)).first)

            ).collect(Collectors.toList());


//            System.out.println(dataset);

            return collect;
        }).collect(Collectors.toList());

        for (List<Pair<String, Double>> pairs : ress) {
            for (Pair<String, Double> pair : pairs) {
                if (!map.containsKey(pair.first)) {
                    map.put(pair.first, 0.);
                }
                map.put(pair.first, map.get(pair.first) + pair.second);
            }
        }

        System.out.print(mlaName);

        for (String id : ids) {
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                if (entry.getKey().contains(id)) {
                    System.out.print(',');
                    System.out.print(String.format("%.3f", entry.getValue() / datasets.size()).replace(',', '.'));
                    break;
                }
            }
        }

        System.out.println();
    }
}
