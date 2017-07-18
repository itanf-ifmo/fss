package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSubsetSelectionAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectAttributes {
    private static final CouchDB mainDB = new CouchDB("https://couchdb.home.tanfilyev.ru", "main");
    private static final CouchDB selectionResults = new CouchDB("https://couchdb.home.tanfilyev.ru", "selection-results");
    private static final List<ClassifierAlgorithm> mlas = new ArrayList<>(ClassifierAlgorithm.getAll(mainDB).values());
    private static final List<BaseDataset> datasets = BaseDataset.getNotIncluded(mainDB);
    private static final List<BaseFeatureSubsetSelectionAlgorithm> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB);


    public static void main(String[] ignore) {
//        moveToNewDBSelectionResults();
//        BaseDataset.getNotIncluded(mainDB).parallelStream().forEach(ds -> fssas.parallelStream().forEach(fss -> job(ds, fss)));
        Collections.shuffle(datasets);
        Collections.shuffle(fssas);

//        datasets.parallelStream().forEach(ds -> fssas.parallelStream().forEach(fss -> mlas.parallelStream().forEach(ml -> remove(ds, fss, ml))));
        datasets.parallelStream().forEach(ds -> fssas.parallelStream().forEach(fss -> mlas.parallelStream().forEach(ml -> job(ds, fss, ml))));
    }

    private static void job(Dataset ds, FeatureSubsetSelectionAlgorithm fssa) {
        String docId = "SR::" + ds.getName() + ":" + fssa.getName();

        try {
            if (selectionResults.object(docId).exists()) {
                System.out.println("Already selected " + docId);
                return;
            }
            FeatureSelectionResult m = fssa.selectAttributes(ds);
            m.saveToCouchDB(selectionResults);
            System.out.println("Selected " + m.getName());
        } catch (Throwable e) {
            System.out.println(ds.getName() + " with " + fssa.getName() + " failed with " + e.getMessage());
        }
    }

    private static void remove(Dataset ds, FeatureSubsetSelectionAlgorithm fssa, ClassifierAlgorithm cr) {
        String docId = "SR:" + cr.getName() + ":" + ds.getName() + ":" + fssa.getName();
        try {
            selectionResults.object(docId).load().delete();
        } catch (Exception e) {}
    }

    private static void job(Dataset ds, FeatureSubsetSelectionAlgorithm fssa, ClassifierAlgorithm cr) {
        String docId = "SR:" + cr.getName() + ":" + ds.getName() + ":" + fssa.getName();

        try {
            if (selectionResults.object(docId).exists()) {
                System.out.println("Already selected " + docId);
                return;
            }
            FeatureSelectionResult m = fssa.preOrderAndSelectAttributes(cr, ds);
            m.saveToCouchDB(selectionResults);
            System.out.println("Selected " + m.getName());
        } catch (Throwable e) {
            System.out.println(ds.getName() + " with " + fssa.getName() + " using " + cr.getName() + " failed with " + e.getMessage());
        }
    }

    private static void moveToNewDBSelectionResults() {
        datasets.forEach(dataset -> {
            for (BaseFeatureSubsetSelectionAlgorithm fssa : fssas) {
                FeatureSelectionResult result = dataset.getResult(fssa.getName());
                result.saveToCouchDB(selectionResults);
                result.getDBObject().load().delete();
            }
        });
    }
}
