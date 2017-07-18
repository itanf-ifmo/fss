package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.nds.SimpleNearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Recommender;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.recsys.*;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunInnerClassifierTypes {
    private static final String classifierName = "NaiveBayes";
    private static final double beta = 0.1;
    private static final CouchDB mainDB = new CouchDB("https://couchdb.home.tanfilyev.ru", "main");
    private static final CouchDB resultsDB = new CouchDB("https://couchdb.home.tanfilyev.ru", "classifiers-v3");

    private static final List<Dataset> datasets = BaseDataset.getAll(mainDB).stream().map(d -> (Dataset)d).collect(Collectors.toList());

    private static final Recommender bpcaRecommenderIBk = new BPCARecommender(classifierName, beta, "weka.classifiers.lazy.IBk", mainDB);
    private static final Recommender bpcaRecommenderJ48 = new BPCARecommender(classifierName, beta, "weka.classifiers.trees.J48", mainDB);
    private static final Recommender bpcaRecommenderSVM = new BPCARecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-K 3", mainDB);
    private static final Recommender kNNRecommender = new DistRecommender(classifierName, beta, new SimpleNearestDatasetSearcher(mainDB, 3));
    private static final Recommender oracleRecommender = new OracleRecommender(classifierName, beta, mainDB);
//    private static final Recommender classifierRecommenderSVM = new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B", mainDB);  // 0.6892
//private static final Recommender classifierRecommenderSVM = new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B -G 0.001 -C 1000.0 -M 100.0", mainDB);  // 0.7114
//private static final Recommender classifierRecommenderSVM = new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B -K 1", mainDB);
    private static final Recommender classifierRecommenderSVM = new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-K 3", mainDB);
    private static final Recommender classifierRecommenderJ48 = new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", mainDB);
    private static final Recommender classifierRecommenderIBk = new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", mainDB);

    private static final Recommender bpcaRecommenderIBkW = new BPCARecommenderWeighted(classifierName, beta, "weka.classifiers.lazy.IBk", mainDB);
    private static final Recommender bpcaRecommenderJ48W = new BPCARecommenderWeighted(classifierName, beta, "weka.classifiers.trees.J48", mainDB);
    private static final Recommender bpcaRecommenderSVMW = new BPCARecommenderWeighted(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B -K 3", mainDB);



    public static void main(String[] ignore) {
//        testOne(datasets.get(0));
//        handleDegrees(datasets.get(0));
//        handleDegrees(datasets.get(100));
//        datasets.parallelStream().forEach(RunInnerClassifierTypes::handle);
        datasets.parallelStream().forEach(RunInnerClassifierTypes::handleDegrees);
    }

    private static void handle(Dataset dataset) {
        List<Pair<Double, String>> oracleRecommendation = oracleRecommender.recommend(dataset);

        double min = oracleRecommendation.get(oracleRecommendation.size() - 1).first;
        double max = oracleRecommendation.get(0).first - min;

        Map oracleScore = oracleRecommendation
                .stream()
                .map(p -> Pair.of((p.first - min) / max, p.second))
                .collect(Collectors.toMap(p -> p.second, p -> p.first));

        System.out.println(dataset.getName());


        call(dataset, oracleScore, oracleRecommender, "oracle");
        call(dataset, oracleScore, kNNRecommender, "kNN");
        call(dataset, oracleScore, bpcaRecommenderIBk, "BPCA-IBk");
        call(dataset, oracleScore, bpcaRecommenderSVM, "BPCA-SVM");
        call(dataset, oracleScore, bpcaRecommenderJ48, "BPCA-J48");

        call(dataset, oracleScore, bpcaRecommenderIBkW, "BPCA-IBk-w2");
        call(dataset, oracleScore, bpcaRecommenderJ48W, "BPCA-J48-w2");
        call(dataset, oracleScore, bpcaRecommenderSVMW, "BPCA-SVM-w2");

        call(dataset, oracleScore, classifierRecommenderSVM, "CR-SVM");
        call(dataset, oracleScore, classifierRecommenderJ48, "CR-J48");
        call(dataset, oracleScore, classifierRecommenderIBk, "CR-IBk");
    }

    private static void call(Dataset dataset, Map oracleScore, Recommender r, String prefix) {
        resultsDB.object("cc:" + prefix + ":" + dataset.getName())
                .put("dataset", dataset.getName())
                .put("method", prefix)
                .put("data", r.recommend(dataset).stream().map(p -> oracleScore.get(p.second)).toArray())
                .save();
    }

    private static void call(Dataset dataset, Map oracleScore, Recommender r) {
        resultsDB.object("cc:" + r.getId() + ":" + dataset.getName())
                .put("dataset", dataset.getName())
                .put("options", r.getOptions())
                .put("data", r.recommend(dataset).stream().map(p -> oracleScore.get(p.second)).toArray())
                .save();
    }

    private static void handleDegrees(Dataset dataset) {
        List<Pair<Double, String>> oracleRecommendation = oracleRecommender.recommend(dataset);
        double max = oracleRecommendation.get(0).first;

        Map oracleScore = oracleRecommendation
                .stream()
                .map(p -> Pair.of(p.first / max, p.second))
                .collect(Collectors.toMap(p -> p.second, p -> p.first));

        System.out.println(dataset.getName());

        for (double degree = -10; degree <= 10; degree += 1) {
            for (Recommender recommender : new Recommender[]{
                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", "", degree, mainDB),
                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", "", degree, mainDB),
                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.NaiveBayes", "", degree, mainDB),
                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.BayesNet", "", degree, mainDB),
                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.RandomForest", "", degree, mainDB),
                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.rules.PART", "", degree, mainDB),
            }) {
                if (!resultsDB.object("cc:" + recommender.getId() + ":" + dataset.getName()).exists()) {
                    call(dataset, oracleScore, recommender);
                }
            }
        }


        double degree = -100;

        for (Recommender recommender : new Recommender[]{
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.NaiveBayes", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.BayesNet", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.RandomForest", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.rules.PART", "", degree, mainDB),
        }) {
            if (!resultsDB.object("cc:" + recommender.getId() + ":" + dataset.getName()).exists()) {
                call(dataset, oracleScore, recommender);
            }
        }

        degree = 0;

        for (Recommender recommender : new Recommender[]{
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.NaiveBayes", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.BayesNet", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.RandomForest", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.rules.PART", "", degree, mainDB),
        }) {
            if (!resultsDB.object("cc:" + recommender.getId() + ":" + dataset.getName()).exists()) {
                call(dataset, oracleScore, recommender);
            }
        }

        degree = 100;

        for (Recommender recommender : new Recommender[]{
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.NaiveBayes", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.BayesNet", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.RandomForest", "", degree, mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.rules.PART", "", degree, mainDB),
        }) {
            if (!resultsDB.object("cc:" + recommender.getId() + ":" + dataset.getName()).exists()) {
                call(dataset, oracleScore, recommender);
            }
        }
    }

    private static void testOne(Dataset dataset) {

        List<Pair<Double, String>> oracleRecommendation = oracleRecommender.recommend(dataset);

        double max = oracleRecommendation.get(0).first;

        Map oracleScore = oracleRecommendation
                .stream()
                .map(p -> Pair.of(p.first / max, p.second))
                .collect(Collectors.toMap(p -> p.second, p -> p.first));
//
//        System.out.println(dataset.getName());


        for (Recommender recommender : new Recommender[]{
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B -K 3", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-K 3", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", "", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", "", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.NaiveBayes", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.bayes.BayesNet", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.RandomForest", mainDB),
                new ClassifierRecommender(classifierName, beta, "weka.classifiers.rules.PART", mainDB),
        }) {
            System.out.println(recommender.recommend(dataset).stream().map(p -> oracleScore.get(p.second)).limit(5).mapToDouble(p->(double)p).sum() / 5);
        }
//
//        for (double degree = -10; degree <= 10; degree += 1) {
//            for (Recommender recommender : new Recommender[]{
//                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B -K 3", degree, mainDB),
//                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-B", degree, mainDB),
//                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "-K 3", degree, mainDB),
//                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.functions.LibSVM", "", degree, mainDB),
//                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.trees.J48", "", degree, mainDB),
//                    new ClassifierRecommender(classifierName, beta, "weka.classifiers.lazy.IBk", "", degree, mainDB)
//            }) {
//                if (!resultsDB.object("cc:" + recommender.getId() + ":" + dataset.getName()).exists()) {
//                    call(dataset, oracleScore, recommender);
//                }
//            }
//        }
    }
}
