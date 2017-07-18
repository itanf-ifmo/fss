package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseMetaFeaturesExtractor;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.MetaFeatureExtractor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractMetaFeatures {
    private static final CouchDB mainDB = new CouchDB("https://couchdb.home.tanfilyev.ru", "main");

    private static final List<Dataset> datasets = BaseDataset.getNotIncluded(mainDB).stream().map(d -> (Dataset)d).collect(Collectors.toList());
    private static final List<MetaFeatureExtractor> mfes = BaseMetaFeaturesExtractor.getAll(mainDB).values().stream().map(d -> (MetaFeatureExtractor)d).collect(Collectors.toList());


    public static void main(String[] ignore) {
        datasets.parallelStream().forEach(ExtractMetaFeatures::countDataset);
    }

    private static void countDataset(Dataset dataset) {
        Map<String, Double> datasetsMetaFeatures = dataset.getMetaFeatures();

        for (MetaFeatureExtractor mfe : mfes) {
            if (!datasetsMetaFeatures.containsKey(mfe.getName())) {
                System.out.println("Extracting " + dataset.getName() + " -> " + mfe.getName());
                double f = -1;
                try {
                    f = mfe.extract(dataset);
                } catch (Throwable e) {
                    System.err.println("Error " + dataset.getName() + " -> " + mfe.getName() + ": " + e.getMessage());
                }

                datasetsMetaFeatures.put(mfe.getName(), f);
                dataset.saveToCouchDB();
            }
        }

        System.out.println("Done with " + dataset.getName());
    }
}
