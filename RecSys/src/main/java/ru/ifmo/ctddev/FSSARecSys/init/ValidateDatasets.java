package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseMetaFeaturesExtractor;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaFeatureExtractor;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidateDatasets {
    private static final CouchDB mainDB = new CouchDB("https://couchdb.home.tanfilyev.ru", "main");
    private static final CouchDB selectionResults = new CouchDB("https://couchdb.home.tanfilyev.ru", "selection-results");

    private static final List<BaseDataset> datasets = BaseDataset.getNotIncluded(mainDB);
    private static final List<MetaFeatureExtractor> mfes = BaseMetaFeaturesExtractor.getAll(mainDB).values().stream().map(d -> (MetaFeatureExtractor)d).collect(Collectors.toList());

    private static final List<ClassifierAlgorithm> mlas = new ArrayList<>(ClassifierAlgorithm.getAll(mainDB).values());
    private static final List<BaseFeatureSubsetSelectionAlgorithm> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB);


    public static void main(String[] ignore) {
        System.out.print("Number of counted datasets: ");
        System.out.println(BaseDataset.getAll(mainDB).size());
        System.out.print("Number of in progress datasets: ");
        System.out.println(BaseDataset.getNotIncluded(mainDB).size());
        validate(BaseDataset.getNotIncluded(mainDB));
//        exclude(BaseDataset.getAll(mainDB));
//        enableDatasets(new String[]{
//                "weather.numeric",
//                "weather.nominal",
//                "monk3",
//                "monk1",
//                "shuttle-landing-control",
//                "monk2",
//                "contact-lenses",
//                "hutsof99_child_witness",
//                "sleuth_ex1221",
//                "iris-2D",
//                "desharnais",
//                "pollution",
//                "tae",
//                "fertility",
//                "lowbwt",
//                "haberman",
//                "breast-tissue",
//                "monks-problems-3_train",
//                "monks-problems-1_train",
//                "analcatdata_olympic2000",
//        });
    }

    private static void exclude(List<BaseDataset> datasets) {
        for (BaseDataset dataset : datasets) {
            dataset.setIncluded(false);
            dataset.saveToCouchDB();
        }
    }

    private static void validate(List<BaseDataset> datasets) {
        for (BaseDataset dataset : datasets) {
//            dataset.setIncluded(false);
//            dataset.saveToCouchDB();

            Pair<Integer, Integer> a = checkMetaFeatures(dataset);
            Pair<Integer, Integer> b = checkSelection(dataset);
            Pair<Integer, Integer> c = checkSelectionPre(dataset);
            boolean valid = a.first.equals(a.second) && b.first.equals(b.second) && c.first.equals(c.second);

            System.out.format("| %40s   | %10s   | ::%9s   :x: %10s   |  %10s   | %10s   |  %1s  |\n", (Object[]) new String[] {
                    dataset.getName(),
                    a.toString(),
                    checkSelected(dataset).toString(),
                    checkSelectedPre(dataset).toString(),
                    b.toString(),
                    c.toString(),
                    valid ?  "+" : "-"
            });

//            if (valid) {
//                dataset.setIncluded(true);
//                dataset.saveToCouchDB();
//            }
        }

    }

    private static void enableDatasets(String[] datasetsNames) {
        for (String datasetName : datasetsNames) {
            for (BaseDataset dataset : datasets) {
//                System.out.println(dataset.getName());
                if (dataset.getName().contains(datasetName)) {
                    System.out.println("Enable: " + dataset);
                    dataset.setIncluded(true);
                    dataset.saveToCouchDB();
                    break;
                }
            }
        }
    }

    private static void disableDatasets(List<BaseDataset> datasets) {
        for (BaseDataset dataset : datasets) {
            dataset.setIncluded(false);
            dataset.saveToCouchDB();
        }
    }

    private static Pair<Integer, Integer> checkMetaFeatures(Dataset dataset) {
        Map<String, Double> datasetsMetaFeatures = dataset.getMetaFeatures();

        int count = 0;
        for (MetaFeatureExtractor mfe : mfes) {
            if (datasetsMetaFeatures.containsKey(mfe.getName())) {
                count++;
            }
        }

        return Pair.of(count, mfes.size());
    }

    private static Pair<Integer, Integer> checkSelected(Dataset dataset) {
        return Pair.of(fssas.parallelStream().mapToInt(fssa -> selectionResults.object("SR::" + dataset.getName() + ":" + fssa.getName()).exists() ? 1 : 0).sum(), fssas.size());
    }

    private static Pair<Integer, Integer> checkSelectedPre(Dataset dataset) {
        return Pair.of(fssas.parallelStream().mapToInt(fssa -> mlas.parallelStream().mapToInt(mla2 -> selectionResults.object("SR:" + mla2.getName() + ":" + dataset.getName() + ":" + fssa.getName()).exists() ? 1 : 0).sum()).sum(), fssas.size() * mlas.size());
    }

    private static Pair<Integer, Integer> checkSelection(Dataset dataset) {
        return Pair.of(fssas.parallelStream().mapToInt(fssa -> mlas.parallelStream().mapToInt(mla -> mainDB.object("LR:SR:" + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName()).exists() ? 1 : 0).sum()).sum(), fssas.size() * mlas.size());
    }

    private static Pair<Integer, Integer> checkSelectionPre(Dataset dataset) {
        return Pair.of(fssas.parallelStream().mapToInt(fssa -> mlas.parallelStream().mapToInt(mla2 -> mlas.parallelStream().mapToInt(mla -> mainDB.object("LR:SR:" + mla2.getName() + ":" + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName()).exists() ? 1 : 0).sum()).sum()).sum(), fssas.size() * mlas.size() * mlas.size());
    }

//    private static Pair<Integer, Integer> checkSelection(Dataset dataset) {
//        int count = 0;
//        for (BaseFeatureSubsetSelectionAlgorithm fssa : fssas) {
//            for (ClassifierAlgorithm mla : mlas) {
//                String docId = "LR:SR:" + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName();
//                if (mainDB.object(docId).exists()) {
//                    count++;
//                }
//            }
//        }
//
//        return Pair.of(count, fssas.size() * mlas.size());
//    }
}
