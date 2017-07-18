package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvaluateSelectedFeatures {
    private static final CouchDB mainDB = new CouchDB("https://couchdb.home.tanfilyev.ru", "main");

    private static final List<BaseDataset> datasets = BaseDataset.getNotIncluded(mainDB);
    private static final List<ClassifierAlgorithm> mlas = new ArrayList<>(ClassifierAlgorithm.getAll(mainDB).values());
    private static final List<BaseFeatureSubsetSelectionAlgorithm> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB);


    public static void main(String[] ignore) {
        Collections.shuffle(datasets);
        Collections.shuffle(mlas);
        Collections.shuffle(fssas);


//        datasets.parallelStream().forEach(d -> fssas.parallelStream().forEach(fssa -> EvaluateSelectedFeatures.remove(d, fssa)));
        datasets.parallelStream().forEach(d -> fssas.parallelStream().forEach(fssa -> EvaluateSelectedFeatures.processDataset(d, fssa)));
    }

    private static void processDataset(Dataset dataset, BaseFeatureSubsetSelectionAlgorithm fssa) {
        FeatureSelectionResult fssResultsNotOrdered;
        FeatureSelectionResult fssResultsOrdered;

        for (ClassifierAlgorithm mla : mlas) {
            for (ClassifierAlgorithm mla2 : mlas) {
                try {
                    fssResultsNotOrdered = dataset.getResult(fssa.getName());
                    fssResultsOrdered = dataset.getResult(fssa.getName(), mla2.getName());

                } catch (NullPointerException e) {
                    System.err.println("Not ready: " + dataset.getName() + ":" + fssa.getName());
                    return;
                } catch (Throwable e) {
                    System.err.println("Error: " + dataset.getName() + ":" + fssa.getName() + " -> " + e.getMessage());
                    return;
                }

                String docId;

                try {
                    docId = "LR:SR:" + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName();
                    if (!mainDB.object(docId).exists()) {
                        System.out.println("Evaluating(n) " + docId);
                        mla.evaluate(dataset, fssResultsNotOrdered).saveToCouchDB();
                        System.out.println("Done(n)       " + docId);
                    }


                    docId = "LR:SR:" + mla2.getName() + ":" + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName();
                    if (!mainDB.object(docId).exists()) {
                        System.out.println("Evaluating(o) " + docId);
                        mla.evaluate(dataset, fssResultsOrdered).saveToCouchDB();
                        System.out.println("Done(o)       " + docId);
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                    System.err.println("Error: " + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName() + " -> " + e.getMessage());
                }
            }
        }

        System.out.println("Done                      " + dataset);
    }

    private static void remove(Dataset dataset, BaseFeatureSubsetSelectionAlgorithm fssa) {
        for (ClassifierAlgorithm mla : mlas) {
            for (ClassifierAlgorithm mla2 : mlas) {
                String docId;

                try {
    //                docId = "LR:SR:" + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName();
    //                mainDB.object(docId).load().delete();

                    docId = "LR:SR:" + mla.getName() + ":" + dataset.getName() + ":" + fssa.getName() + ":" + mla2.getName();
                    mainDB.object(docId).load().delete();

                } catch (Throwable e) {
    //                e.printStackTrace();
    //                System.err.println("Error: " + dataset.getName() + ":" + fssa.getName() + ":" + mla.getName() + " -> " + e.getMessage());
                }
            }
        }

        System.out.println("Done                      " + dataset);
    }
}
