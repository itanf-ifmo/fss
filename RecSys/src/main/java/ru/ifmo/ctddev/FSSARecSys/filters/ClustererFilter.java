package ru.ifmo.ctddev.FSSARecSys.filters;

import ru.ifmo.ctddev.FSSARecSys.AEARRComputers;
import ru.ifmo.ctddev.FSSARecSys.filters.util.CustomDistanceFunction;
import ru.ifmo.ctddev.FSSARecSys.filters.util.FssasDistancesCache;
import ru.ifmo.ctddev.FSSARecSys.interfaces.AlgFilter;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.NearestDatasetSearcher;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.Instances;

import java.util.*;


public class ClustererFilter implements AlgFilter, AEARRComputers {
    private static final double DONT_USE_ZEROS = 0.0001;
    private final int desiredAlgorithmsNumber;
    private final NearestDatasetSearcher nds;
    private final FssasDistancesCache distances;
    private final String[] allFSSASNames;
    private final String clustererName;
    private final String mla;
    private final double beta;

    @SuppressWarnings("WeakerAccess")
    private ClustererFilter(int desiredAlgorithmsNumber, NearestDatasetSearcher nds, FssasDistancesCache distances, String[] allFSSASNames, String clustererName, String mla, double beta) {
        this.desiredAlgorithmsNumber = desiredAlgorithmsNumber;
        this.nds = nds;
        this.distances = distances;
        this.allFSSASNames = allFSSASNames.clone();
        this.clustererName = clustererName;
        this.mla = mla;
        this.beta = beta;
    }

    public static ClustererFilter simpleKMeansClusterFilter(int desiredAlgorithmsNumber, NearestDatasetSearcher nds, FssasDistancesCache distances, String[] allFSSASNames, String mla, double beta) {
        return new ClustererFilter(desiredAlgorithmsNumber, nds, distances, allFSSASNames, "SimpleKMeans", mla, beta);
    }

    public static ClustererFilter hierarchicalClusterFilter(int desiredAlgorithmsNumber, NearestDatasetSearcher nds, FssasDistancesCache distances, String[] allFSSASNames, String mla, double beta) {
        return new ClustererFilter(desiredAlgorithmsNumber, nds, distances, allFSSASNames, "HierarchicalClusterer", mla, beta);
    }

    /**
     * @param allFSSASNames list of recommended feature subset selection algorithms
     * @param dataset                         what we need to process
     * @return list of names of feature subset selection algorithms that should be used for rank aggregation
     */
    @Override
    public String[] filter(Dataset dataset) {
        return new Computer(dataset).get();
    }

    private class Computer {
        private final Dataset dataset;
        private final List<Pair<Double, Dataset>> wightsToDatasets;
        private final Map<String, Map<String, Double>> mDistances;
        private final Map<String, Double> mAearrs;

        Computer(Dataset dataset) {
            this.dataset = dataset;
            this.wightsToDatasets = nds.search(dataset);
            this.mDistances = countMeansDistances();
            this.mAearrs = countMeansAearrs();
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

        String[] get() {
            return searcher();
        }

        private Map<String, Map<String, Double>> countMeansDistances() {
            Map<String, Map<String, Double>> map = new HashMap<>();

            for (String nameA : allFSSASNames) {
                map.put(nameA, new TreeMap<>());

                for (String nameB : allFSSASNames) {
                    map.get(nameA).put(nameB, wightsToDatasets.stream().mapToDouble(pair -> (distances.getDistances(pair.second, nameA, nameB) + DONT_USE_ZEROS) / (pair.first + DONT_USE_ZEROS)).sum());
                }
            }

            return map;
        }

        private String[] searcher() {
            AbstractClusterer clusterer;
            DistanceFunction df = new CustomDistanceFunction(mDistances, allFSSASNames);

            try {
                switch (clustererName) {
                    case "HierarchicalClusterer":
                        clusterer = new HierarchicalClusterer();
                        ((HierarchicalClusterer) clusterer).setDistanceFunction(df);
                        ((HierarchicalClusterer) clusterer).setNumClusters(desiredAlgorithmsNumber);
                        break;
                    case "SimpleKMeans":
                        clusterer = new SimpleKMeans();
                        ((SimpleKMeans) clusterer).setDistanceFunction(df);
                        ((SimpleKMeans) clusterer).setNumClusters(desiredAlgorithmsNumber);
                        break;
                    default:
                        throw new IllegalArgumentException("Not supported clustering algorithm");
                }

            } catch (Exception e) {
                throw new Error(e);
            }

            Instances data = buildInstance(dataset);

            Map<String, Integer> clusterMap = new TreeMap<>();

            try {
                clusterer.buildClusterer(data);
                for (int i = 0; i < data.numInstances(); i++) {
                    clusterMap.put(
                            allFSSASNames[(int)data.instance(i).value(1)],
                            clusterer.clusterInstance(data.instance(i))
                    );
                }
            } catch (Exception e) {
                throw new Error(e);
            }

            return adjustSelection(clusterMap, allFSSASNames);
        }

        private String[] adjustSelection(Map<String, Integer> clusterMap, String[] selected) {
            List<String> res = new LinkedList<>();
            boolean[] usedClusters = new boolean[clusterMap.size()];

            Arrays.sort(selected, (o1, o2) -> -Double.compare(mAearrs.get(o1), mAearrs.get(o2)));

            for (String s : selected) {
                if (!usedClusters[clusterMap.get(s)]) {
                    usedClusters[clusterMap.get(s)] = true;
                    res.add(s);
                }
            }

            return res.toArray(new String[res.size()]);
        }

        private Instances buildInstance(Dataset ds) {
            ArrayList<Attribute> atts = new ArrayList<>();
            atts.add(new Attribute("feature" ));
            atts.add(new Attribute("class" ));

            Instances trainSet = new Instances("Clustering: " + ds.getName(), atts, 0);

            for (int i = 0; i < allFSSASNames.length; i++) {
                trainSet.add(new DenseInstance(1.0, new double[] {
                        (double)allFSSASNames[i].hashCode(),
                        (double)i,
                }));
            }

            return trainSet;
        }
    }

    /**
     * @return filter name and it's parameters.
     */
    @Override
    public String getId() {
        return String.format("ClustererFilter<nds:%s,des:%d,dist:%s,clus:%s>",
                nds.getId(),
                desiredAlgorithmsNumber,
                distances.getId(),
                clustererName

        );
    }
}
