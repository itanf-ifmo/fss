package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.log10;
import static ru.ifmo.ctddev.FSSARecSys.MLUtils.SMALL_BUT_NOT_ZERO;

public interface AEARRComputers {
    static double computeAearr(ClassificationResult a, ClassificationResult b, double beta) {
        double na = a.getFeaturesNumber() + SMALL_BUT_NOT_ZERO;
        double fa = a.getF1Measure() + SMALL_BUT_NOT_ZERO;
        double nb = b.getFeaturesNumber() + SMALL_BUT_NOT_ZERO;
        double fb = b.getF1Measure() + SMALL_BUT_NOT_ZERO;

        double l = log10(na / nb);

        if (beta * l < -0.99) {
            System.err.println("log10(n/n) < -0.99");
            l = -0.99;
        }
        if (l >= Double.MAX_VALUE) {
            System.err.println("log10(n/n) > inf");
            l = 1 / SMALL_BUT_NOT_ZERO;
        }

        return (fa / fb) / (1 + beta * l);
    }

    default double aearr(ClassificationResult a, ClassificationResult b, double beta) {
        return AEARRComputers.computeAearr(a, b, beta);
    }

    static double computeAearr(Dataset dataset, ClassificationResult classificationResult, double beta) {
        // todo get dataset directly?

        double was = (((double) dataset.getFeaturesNumber()) + SMALL_BUT_NOT_ZERO);
        double become = (((double) classificationResult.getFeaturesNumber()) + SMALL_BUT_NOT_ZERO);

        return classificationResult.getF1Measure() / (1 + beta * log10(become / was));
    }

    default double aearr(Dataset dataset, ClassificationResult classificationResult, double beta) {
        return AEARRComputers.computeAearr(dataset, classificationResult, beta);
    }

    default List<Pair<Double, ClassificationResult>> aearrAndMLResults(List<String> fssas, Dataset dataset, String classifierName, double beta) {
        return earr(fssas.stream().map(r -> (ClassificationResult) dataset.getResult(r).getResult(classifierName)).collect(Collectors.toList()), beta);
    }

    default List<Pair<Double, ClassificationResult>> earr(List<ClassificationResult> results, double beta) {
        long normalizationFactor = results.size() - 1;
        return results.stream().map(a -> Pair.of((results.stream().mapToDouble(b -> aearr(a, b, beta)).sum() - 1) / normalizationFactor, a)).collect(Collectors.toList());
    }

    default Map<String, Double> aearrAndMLResultsAsMap(List<String> fssas, Dataset dataset, String classifierName, double beta) {
        HashMap<String, Double> map = new HashMap<>();

        for (Pair<Double, ClassificationResult> pair : aearrAndMLResults(fssas, dataset, classifierName, beta)) {
            map.put(pair.second.getFeatureSelectionResult().getName(), pair.first);
        }

        return map;
    }
}
