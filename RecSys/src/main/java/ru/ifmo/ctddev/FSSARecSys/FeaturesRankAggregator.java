package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import ru.ifmo.ctddev.ral.BordasAggregators;
import ru.ifmo.ctddev.ral.ListOfRanks;
import ru.ifmo.ctddev.ral.MarkovChainAggregators;
import ru.ifmo.ctddev.ral.RankAggregator;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ru.ifmo.ctddev.FSSARecSys.MLUtils.rankFeatures;


public class FeaturesRankAggregator implements AEARRComputers {
    private enum Type {
        PRE,
        POS,
        NON
    }
    private final ClassifierAlgorithm mla;
    private final double beta;
    private final Type type;
    private ClassifierAlgorithm mla2 = null;

    private FeaturesRankAggregator(ClassifierAlgorithm mla, double beta, Type type) {
        this.mla = mla;
        this.beta = beta;
        this.type = type;
    }

    @SuppressWarnings("unused")
    public static FeaturesRankAggregator getNotRanked(ClassifierAlgorithm mla, double beta) {
        return new FeaturesRankAggregator(mla, beta, Type.NON);
    }

    @SuppressWarnings("unused")
    public static FeaturesRankAggregator getPreRanked(ClassifierAlgorithm mla, double beta, ClassifierAlgorithm mla2) {
        FeaturesRankAggregator fra = new FeaturesRankAggregator(mla, beta, Type.PRE);
        fra.mla2 = mla2;
        return fra;
    }

    @SuppressWarnings("unused")
    public static FeaturesRankAggregator getPostRanked(ClassifierAlgorithm mla, double beta, ClassifierAlgorithm mla2) {
        FeaturesRankAggregator fra = new FeaturesRankAggregator(mla, beta, Type.POS);
        fra.mla2 = mla2;
        return fra;
    }

    /**
     * @return Pair of aearr and selection time
     */
    @SuppressWarnings("unused")
    public Pair<Double, Double> evaluateToAEARR(Dataset dataset, String[] fssAlgorithms) {
//        System.out.println(Arrays.toString(fssAlgorithms));
        return evaluateToAEARR(dataset, fssAlgorithms, null);
    }

    @SuppressWarnings("WeakerAccess")
    public Pair<Double, Double> evaluateToAEARR(Dataset dataset, String[] fssAlgorithms, @SuppressWarnings("SameParameterValue") double[] weights) {
        switch (type) {
            case NON:
                return evaluateNotRankedToAEARR(dataset, fssAlgorithms, weights);
            case POS:
                return evaluatePostRankedToAEARR(dataset, fssAlgorithms, weights);
            case PRE:
                return evaluatePreRankedToAEARR(dataset, fssAlgorithms, weights);
            default:
                throw new AssertionError();
        }
    }

    /**
     * @return Pair of aearr and selection time
     */
    private Pair<Double, Double> evaluatePostRankedToAEARR(Dataset dataset, String[] fssAlgorithms, double[] weights) {
        Integer[][] selectedFeatures = new Integer[fssAlgorithms.length][];
        double time = 0;

        for (int i = 0; i < selectedFeatures.length; i++) {
            FeatureSelectionResult result = dataset.getResult(fssAlgorithms[i]);
            time += result.getSelectionTime();
            int[] features = IntStream.of(result.getSelectedFeatures()).toArray();

            long startTimePre = System.currentTimeMillis();
            features = rankFeatures(mla2, dataset, features);
            time += ((double) System.currentTimeMillis() - startTimePre) / 1000;

            selectedFeatures[i] = IntStream.of(features).boxed().toArray(Integer[]::new);
        }

        long startRankingTime = System.currentTimeMillis();
        double score = aearr(dataset, evaluate(dataset, selectedFeatures, weights), beta);
        time += ((double) System.currentTimeMillis() - startRankingTime) / 1000;

        return Pair.of(score, time);
    }

    /**
     * @return Pair of aearr and selection time
     */
    private Pair<Double, Double> evaluatePreRankedToAEARR(Dataset dataset, String[] fssAlgorithms, double[] weights) {
        Integer[][] selectedFeatures = new Integer[fssAlgorithms.length][];
        double time = 0;

        for (int i = 0; i < selectedFeatures.length; i++) {
            FeatureSelectionResult result = dataset.getResult(fssAlgorithms[i], mla2.getName());
            time += result.getSelectionTime();
            selectedFeatures[i] = IntStream.of(result.getSelectedFeatures()).boxed().toArray(Integer[]::new);
        }

        long startRankingTime = System.currentTimeMillis();
        double score = aearr(dataset, evaluate(dataset, selectedFeatures, weights), beta);
        time += ((double) System.currentTimeMillis() - startRankingTime) / 1000;

        return Pair.of(score, time);
    }

    /**
     * @return Pair of aearr and selection time
     */
    private Pair<Double, Double> evaluateNotRankedToAEARR(Dataset dataset, String[] fssAlgorithms, double[] weights) {
        Integer[][] selectedFeatures = new Integer[fssAlgorithms.length][];
        double time = 0;

        for (int i = 0; i < selectedFeatures.length; i++) {
            FeatureSelectionResult result = dataset.getResult(fssAlgorithms[i]);
            time += result.getSelectionTime();
            selectedFeatures[i] = IntStream.of(result.getSelectedFeatures()).boxed().toArray(Integer[]::new);
        }

        long startRankingTime = System.currentTimeMillis();
        double score = aearr(dataset, evaluate(dataset, selectedFeatures, weights), beta);
        time += ((double) System.currentTimeMillis() - startRankingTime) / 1000;

        return Pair.of(score, time);
    }

    private ClassificationResult evaluate(Dataset dataset, Integer[][] selectedFeatures, double[] weights) {
        List<ClassificationResult> crl;

        if (weights == null) {
            crl = evalRanking(dataset, new ListOfRanks<>(selectedFeatures));
        } else {
            crl = evalRanking(dataset, new ListOfRanks<>(selectedFeatures, weights));
        }

        for (Integer[] fl : selectedFeatures) {
            crl.add(mla.evaluate(dataset, Stream.of(fl).mapToInt(i->i).toArray()));
        }

        List<Pair<Double, ClassificationResult>> a = earr(crl);
        return Collections.max(a).second;
    }

    @SuppressWarnings("Duplicates")
    private List<ClassificationResult> evalRanking(Dataset dataset, ListOfRanks<Integer> listOfRanks) {
        List<ClassificationResult> crl = new LinkedList<>();

        crl.add(variateListOFRanks(dataset, BordasAggregators.arithmeticAverageRanker(), listOfRanks));
        crl.add(variateListOFRanks(dataset, BordasAggregators.medianRanker(), listOfRanks));
        crl.add(variateListOFRanks(dataset, BordasAggregators.geometricMeanRanker(), listOfRanks));
        crl.add(variateListOFRanks(dataset, BordasAggregators.L2NormRanker(), listOfRanks));
        crl.add(variateListOFRanks(dataset, MarkovChainAggregators.MC1(), listOfRanks));
        crl.add(variateListOFRanks(dataset, MarkovChainAggregators.MC2(), listOfRanks));
        crl.add(variateListOFRanks(dataset, MarkovChainAggregators.MC3(), listOfRanks));

        return crl;
    }

    /**
     * create classification results by given rank aggregation
     */
    private ClassificationResult variateListOFRanks(Dataset dataset, RankAggregator<Integer> ra, ListOfRanks<Integer> lists) {
        List<Integer> aggregation = ra.aggregate(lists);
        List<Integer> stat = lists.stream().map(ListOfRanks.Rank::size).collect(Collectors.toList());

        long minN = Collections.min(stat);
        long medN = stat.stream().mapToInt(i -> i).sum() / stat.size();
        long maxN = Collections.max(stat);

        List<Pair<Double, ClassificationResult>> a = earr(Arrays.asList(
                mla.evaluate(dataset, aggregation.stream().limit(minN).mapToInt(i -> i).toArray()),
                mla.evaluate(dataset, aggregation.stream().limit(medN).mapToInt(i -> i).toArray()),
                mla.evaluate(dataset, aggregation.stream().limit(maxN).mapToInt(i -> i).toArray())
        ));

        return Collections.max(a).second;
    }

    private List<Pair<Double, ClassificationResult>> earr(List<ClassificationResult> results) {
        return results.stream().map(cr -> Pair.of((results.stream().mapToDouble(b -> aearr(cr, b, beta)).sum() - 1), cr)).collect(Collectors.toList());
    }

    public String getId() {
        return String.format("A<%s>", type.toString());
    }
}
