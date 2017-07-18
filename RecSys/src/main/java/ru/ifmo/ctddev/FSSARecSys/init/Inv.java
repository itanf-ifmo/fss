package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;
import ru.ifmo.ctddev.FSSARecSys.interfaces.FeatureSelectionResult;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseFeatureSubsetSelectionAlgorithm;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassifierAlgorithm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.ifmo.ctddev.FSSARecSys.MLUtils.rankFeatures;

public class Inv {
    private static final String DB_URL = "https://couchdb.home.tanfilyev.ru";
    private static final CouchDB mainDB = new CouchDB(DB_URL, "main");
    private static final CouchDB resultsDB = new CouchDB(DB_URL, "results-all");
    private static final List<ClassifierAlgorithm> mlas = new ArrayList<>(ClassifierAlgorithm.getAll(mainDB).values());
    private static final List<BaseFeatureSubsetSelectionAlgorithm> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll(mainDB);
    private static final List<BaseDataset> datasets;

    static {
        List<BaseDataset> datasets_ = BaseDataset.getAll(mainDB);
        Collections.shuffle(datasets_, new Random(20));
        datasets = datasets_.stream().limit(20).collect(Collectors.toList());
    }

    public static void main(String[] ignore) {
        Dataset ds = datasets.get(3);

        System.out.println(ds);
        for (ClassifierAlgorithm mla : mlas.stream().limit(1).collect(Collectors.toList())) {
            System.out.println("  " + mla.getName());
            for (BaseFeatureSubsetSelectionAlgorithm fssa : fssas.stream().limit(1).collect(Collectors.toList())) {
                System.out.println("   " + fssa.getName());

                System.out.print("    ne:");
                System.out.println(mla.evaluate(ds, ds.getResult(fssa.getName())));
                System.out.print("      f:");
                System.out.println(Arrays.toString(ds.getResult(fssa.getName()).getSelectedFeatures()));
                System.out.println();

                FeatureSelectionResult result = ds.getResult(fssa.getName(), mla.getName());
                System.out.print("    pr:");
                System.out.println(mla.evaluate(ds, result));
                System.out.print("      f:");
                System.out.println(Arrays.toString(result.getSelectedFeatures()));
                System.out.println();

                result = fssa.preOrderAndSelectAttributes(mla, ds);
                System.out.print("    pr*:");
                System.out.println(mla.evaluate(ds, result));
                System.out.print("      f:");
                System.out.println(Arrays.toString(result.getSelectedFeatures()));
//                int[] f = shuffle(result.getSelectedFeatures());
//                System.out.print("    pr+:");
//                System.out.println(mla.evaluate(ds, f));
//                System.out.print("      g:");
//                System.out.println(Arrays.toString(result.getSelectedFeatures()));
//                System.out.print("      f:");
//                System.out.println(Arrays.toString(f));
                System.out.println();

                int[] features = IntStream.of(ds.getResult(fssa.getName()).getSelectedFeatures()).toArray();
                features = rankFeatures(mla, ds, features);
                System.out.print("    po:");
                System.out.println(mla.evaluate(ds, features));
                System.out.print("      f:");
                System.out.println(Arrays.toString(features));
            }
        }
    }
}
