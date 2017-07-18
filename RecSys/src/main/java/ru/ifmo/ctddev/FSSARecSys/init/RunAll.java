package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.FeaturesRankAggregator;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender;
import ru.ifmo.ctddev.FSSARecSys.recsys.OracleRecommender;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RunAll extends TestRun {
    private static final List<BaseDataset> datasets  = BaseDataset.getAll(mainDB);

    static {
        datasets.addAll(BaseDataset.getNotIncluded(mainDB));
    }

    public static void main(String[] ignore) {
        double beta = 0.1;
        int n = 3;

        ClassifierAlgorithm mla = ClassifierAlgorithm.getAll(mainDB).get("PART");
        run(mla, n, beta);


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

    @SuppressWarnings("Duplicates")
    protected static Map<String, Double> run(ClassifierAlgorithm mla, int n, double beta) {
        Recommender recommender = new DistRecommender(mla.getName(), beta, new SimpleNearestDatasetSearcher(mainDB, 3));
        Recommender recommenderO = new OracleRecommender(mla.getName(), beta, mainDB);

        List<DBObject> collect = datasets.parallelStream().map(dataset -> {
            try {
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
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());

        Map<String, Double> map = new TreeMap<>();

        for (DBObject dbObject : collect) {
            if (dbObject == null) {
                continue;
            }

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
}
