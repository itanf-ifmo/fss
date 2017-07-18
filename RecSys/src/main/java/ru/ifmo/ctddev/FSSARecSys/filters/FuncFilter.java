package ru.ifmo.ctddev.FSSARecSys.filters;

import ru.ifmo.ctddev.FSSARecSys.AEARRComputers;
import ru.ifmo.ctddev.FSSARecSys.filters.util.FssasDistancesCache;
import ru.ifmo.ctddev.FSSARecSys.interfaces.AlgFilter;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.*;

import static ru.ifmo.ctddev.FSSARecSys.recsys.DistRecommender2.distToWeights;


public class FuncFilter implements AlgFilter, AEARRComputers {
    private final int desiredAlgorithmsNumber;
    private final NearestDatasetSearcher nds;
    private final FssasDistancesCache distances;
    private final String[] allFSSASNames;
    private final String mla;
    private final double beta;
    private final double gamma;

    @SuppressWarnings("WeakerAccess")
    public FuncFilter(int desiredAlgorithmsNumber, NearestDatasetSearcher nds, FssasDistancesCache distances, String[] allFSSASNames, String mla, double beta, double gamma) {
        this.desiredAlgorithmsNumber = desiredAlgorithmsNumber;
        this.nds = nds;
        this.distances = distances;
        this.allFSSASNames = allFSSASNames.clone();
        this.mla = mla;
        this.beta = beta;
        this.gamma = gamma;
    }

    /**
     * @param dataset                         what we need to process
     * @return list of names of feature subset selection algorithms that should be used for rank aggregation
     */
    @Override
    public String[] filter(Dataset dataset) {
        return new Computer(dataset).get();
    }

    private class Computer {
        private final List<Pair<Double, Dataset>> wightsToDatasets;
        private final Map<String, Map<String, Double>> mDistances;
        private final Map<String, Double> mAearrs;

        Computer(Dataset dataset) {
            this.wightsToDatasets = distToWeights(nds.search(dataset));
            this.mDistances = countMeansDistances();
            this.mAearrs = countMeansAearrs();
        }

        String[] get() {
            return searcher();
        }

        private Map<String, Map<String, Double>> countMeansDistances() {
            Map<String, Map<String, Double>> map = new HashMap<>();

            for (String nameA : allFSSASNames) {
                map.put(nameA, new TreeMap<>());

                for (String nameB : allFSSASNames) {
                    // ordered Pairs of distances and dataset
                    map.get(nameA).put(nameB, wightsToDatasets.stream().mapToDouble(pair -> distances.getDistances(pair.second, nameA, nameB) * pair.first).sum());
                }
            }

            return map;
        }

        private Map<String, Double> countMeansAearrs() {
            Map<String, Double> map = new HashMap<>();

            for (String fssa : allFSSASNames) {
                map.put(fssa, wightsToDatasets.stream().mapToDouble(pair -> {
                    Map<String, Double> aearrAndMLResults = aearrAndMLResultsAsMap(Arrays.asList(allFSSASNames), pair.second, mla, beta);
                    return aearrAndMLResults.get(fssa) * pair.first;
                }).sum());
            }

            return map;
        }

        @SuppressWarnings("Duplicates")
        private Pair<Double, String[]> searcherRec(String[] fssas, int depth, int sf) {
            if (depth >= fssas.length) {
                return Pair.of(func(fssas), fssas);
            }

            int left = allFSSASNames.length - fssas.length + depth + 1;
            Pair<Double, String[]> best = Pair.of(-Double.MAX_VALUE, null);

            for (int i = sf; i < left; i++) {
                fssas[depth] = allFSSASNames[i];

                Pair<Double, String[]> res = searcherRec(fssas, depth + 1, i + 1);

                if (best.first < res.first) {
                    best = Pair.of(res.first, res.second.clone());
                }
            }

            return best;
        }

        private String[] searcher() {
            return searcherRec(new String[desiredAlgorithmsNumber], 0, 0).second;
        }

        private double func(String[] fssas) {
            double sum = 0;
            int counter = 0;

            for (int i = 0; i < fssas.length - 1; i++) {
                for (int j = i + 1; j < fssas.length; j++) {
                    counter++;
                    sum += mDistances.get(fssas[i]).get(fssas[j]);
                }
            }

            sum /= counter;
            sum *= 1 - gamma;

            sum += gamma * Arrays.stream(fssas).mapToDouble(mAearrs::get).sum() / fssas.length;
            return sum;
        }
    }

    /**
     * @return filter name and it's parameters.
     */
    @Override
    public String getId() {
        return String.format("FuncFilter<nds:%s,des:%d,dist:%s,mla:%s,beta:%f,gamma:%f>",
                nds.getId(),
                desiredAlgorithmsNumber,
                distances.getId(),
                mla,
                beta,
                gamma
        );
    }
}
