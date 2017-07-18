package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class MLUtils {
    public static final double SMALL_BUT_NOT_ZERO = 0.000000001;

    public static int[] rankFeatures(ClassifierAlgorithm mla, Dataset dataset, int[] features) {
        int cI = dataset.getInstances(features).classIndex();

        // already ordered? than skip!
        for (int i = 1; i < features.length; i++) {
            // ignore class index
            if (i - 1 == cI || i == cI) {
                continue;
            }

            if (features[i - 1] > features[i]) {
                return features;
            }
        }

        int[] selected = new int[features.length - 1];
        List<Pair<Double, Integer>> result = new ArrayList<>();

        System.arraycopy(features, 1, selected, 0, selected.length);

        for (int i = 0; i < features.length; i++) {
            if (i == cI) {
                result.add(Pair.of(Double.MAX_VALUE, features[i]));
            } else {
                result.add(Pair.of(mla.evaluate(dataset, selected).getF1Measure(), features[i]));
            }

            if (i != selected.length) {
                selected[i] = features[i];
            }
        }

        return result.stream().sorted().mapToInt(p -> p.second).toArray();
    }

    public static Pair<Instances, Integer[]> preRankInstances(ClassifierAlgorithm mla, Dataset dataset) {
        int[] oldOrder = IntStream.range(0, dataset.getInstances().numAttributes()).toArray();
        int[] newOrder = rankFeatures(mla, dataset, oldOrder);
        Integer[] map = Arrays.stream(newOrder).boxed().toArray(Integer[]::new);

        try {
            Reorder reorder = new Reorder();
            reorder.setAttributeIndicesArray(newOrder);
            reorder.setInputFormat(dataset.getInstances());
            Instances inst =  Filter.useFilter(dataset.getInstances(), reorder);
            return Pair.of(inst, map);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
