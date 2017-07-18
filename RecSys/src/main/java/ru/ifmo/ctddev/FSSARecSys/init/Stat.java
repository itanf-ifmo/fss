package ru.ifmo.ctddev.FSSARecSys.init;

import ru.ifmo.ctddev.FSSARecSys.AEARRComputers;
import ru.ifmo.ctddev.FSSARecSys.privetives.ClassificationResult;
import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Stat {
//    private static final List<Dataset> datasets = ImplementationManager.database.getAllDatasets(ImplementationManager.datasetClass, BaseFeatureSelectionResult.class, ClassificationResult.class);
//    private static final List<String> fssas = ImplementationManager.database.getAllFeatureSubsetSelectionAlgorithms(BaseFeatureSubsetSelectionAlgorithm.class).stream().map(FeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());

    public static void main(String[] ignore) {
//        compareBeforeAndAfter();
    }

//    public static void stat1() {
//        for (String metaFeature : fssas) {
//            boolean f = false;
//
//            l:for (Dataset dataset : datasets) {
//                FeatureSelectionResult fsr = dataset.getResult(metaFeature);
////                System.out.println(metaFeature);
//                int[] features = fsr.getSelectedFeatures();
////                System.out.println(Arrays.toString(features));
//
//                for (int i = 0; i < features.length - 2; i++) {
//                    if (features[i] > features[i + 1]) {
//                        f = true;
//                        break l;
//                    }
//                }
//            }
//
//            System.out.println(metaFeature + " " + String.valueOf(f));
//        }
//
//        ImplementationManager.database.close();
//    }
//
//    public static void stat2() {
//        double t = System.currentTimeMillis();
//        List<Dataset> datasets = ImplementationManager.database.getAllDatasets(ImplementationManager.datasetClass, BaseFeatureSelectionResult.class, ClassificationResult.class);
////        List<String> metaFeatures = ImplementationManager.database.getAllDatasetMetaFeatureNames();
////        List<String> fssas = ImplementationManager.database.getAllFeatureSubsetSelectionAlgorithms(BaseFeatureSubsetSelectionAlgorithm.class).stream().map(FeatureSubsetSelectionAlgorithm::getName).collect(Collectors.toList());
//
////        CouchDB a = new CouchDB("http://localhost:5984", "testDB");
////        Dataset d = datasets.get(0);
//        System.out.println((System.currentTimeMillis() - t) / 1000);
//        t = System.currentTimeMillis();
//        System.out.println("loaded");
//
//
//        Map<String, BaseFeatureSelectionResult> results = BaseFeatureSelectionResult.getAll();
//        List<ClassifierAlgorithm> classifiers = ClassifierAlgorithm.getAll();
//        List<? extends FeatureSubsetSelectionAlgorithm> fssas = BaseFeatureSubsetSelectionAlgorithm.getAll();
////        List<Dataset> datasets = BaseDataset.getAll();
//
//        for (Dataset d : datasets) {
//            for (FeatureSubsetSelectionAlgorithm fssa : fssas) {
//                FeatureSelectionResult result = d.getResult(fssa.getName());
//                result.setDataset(d);
//                result.setFeatureSubsetSelectionAlgorithm(fssa);
//                FeatureSelectionResult result2 = results.get("SR:" + d.getName() + ":" + fssa.getName());
//
//                for (ClassifierAlgorithm classifier : classifiers) {
//                    MetaLearningResult cr = result.getResult(classifier.getName());
//                    cr.setFeatureSelectionResult(result2);
//                    cr.setMetaLearningAlgorithm(classifier);
//                    cr.saveToCouchDB();
////                    BaseFeatureSelectionResult result = new BaseFeatureSelectionResult();
////                    result.loadFromCouchDB(d.getName() + ":" + fssa.getName());
////                    System.out.println(result);
////                FeatureSelectionResult result = d.getResult(fssa.getName());
////                result.setDataset(d);
////                result.setFeatureSubsetSelectionAlgorithm(fssa);
//                System.out.println(result);
////                result.saveToCouchDB();
//                }
//            }
//        }
//
////        for (MetaLearningAlgorithm algorithm : ImplementationManager.database.getAllMetaLearningAlgorithms(ClassifierAlgorithm.class)) {
////            algorithm.saveToCouchDB();
////        }
////
////        for (FeatureSubsetSelectionAlgorithm fssa : ImplementationManager.database.getAllFeatureSubsetSelectionAlgorithms(BaseFeatureSubsetSelectionAlgorithm.class)) {
////            fssa.saveToCouchDB();
////        }
//
//
//        System.out.println((System.currentTimeMillis() - t) / 1000);
//        System.out.println("saved");
//        ImplementationManager.database.close();
//
////        for (FeatureSubsetSelectionAlgorithm fssa : ImplementationManager.database.getAllFeatureSubsetSelectionAlgorithms(BaseFeatureSubsetSelectionAlgorithm.class)) {
////            fssa.saveToCouchDB();
////        }
//    }
    public static void removeAllCRLs() {
//        ImplementationManager.couchdb.getAll("oSR:").parallelStream().forEach(CouchDBObject::delete);
//        ImplementationManager.couchdbResults.getAll("CRL:").parallelStream().forEach(CouchDBObject::delete);
//        ImplementationManager.couchdbResults.getAll("R:").parallelStream().forEach(CouchDBObject::delete);

//        Set<String> Rs = ImplementationManager.couchdbResults.getAll("R:").stream().map(r -> String.valueOf(r.getDouble("beta")) + ":" + String.valueOf(r.getInt("L")) + ":" + r.getString("classifier")).collect(Collectors.toSet());
//        List<DBObject> crls = ImplementationManager.couchdbResults.getAll("CRL:");
//        crls.stream().filter(r -> !Rs.contains(String.valueOf(r.getDouble("beta")) + ":" + String.valueOf(r.getInt("L")) + ":" + r.getString("classifier"))).forEach(DBObject::delete);

//        ImplementationManager.couchdbResults.getAll("R:").parallelStream().forEach(DBObject::delete);
//        ImplementationManager.couchdbResults.getAll("CRL:").parallelStream().forEach(DBObject::delete);

        List<Pair<String, DBObject>> Rs = ImplementationManager.couchdbResults.getAll("CRL:").stream().map(r -> Pair.of(String.valueOf(r.getDouble("beta")) + "::" + String.valueOf(r.getInt("L")) + "::" + r.getString("classifier") + "::" + r.getString("dataset"), r)).collect(Collectors.toList());
        Set<String> a = new HashSet<>();
//
        for (Pair<String, DBObject> r : Rs) {
            if (a.contains(r.first)) {
                System.out.println(r.first);
//                r.second.delete();
                continue;
            }

            a.add(r.first);
        }

//        crls.stream().filter(r -> !Rs.contains(String.valueOf(r.getDouble("beta")) + ":" + String.valueOf(r.getInt("L")) + ":" + r.getString("classifier"))).forEach(DBObject::delete);
//        crls.stream().filter(r -> !Rs.contains(String.valueOf(r.getDouble("beta")) + ":" + String.valueOf(r.getInt("L")) + ":" + r.getString("classifier"))).forEach(System.out::println);
    }

//
//    public static void compareBeforeAndAfter() {
//        ClassifierAlgorithm mla = new ClassifierAlgorithm().loadFromCouchDB("NaiveBayes"); // constant could not change
//        double beta = 0.1;
//
//        List<Dataset> datasets = BaseDataset.getAll().stream().map(d -> (Dataset)d).collect(Collectors.toList());
//
//        for (BaseFeatureSubsetSelectionAlgorithm fss : BaseFeatureSubsetSelectionAlgorithm.getAll()) {
//            double b = 0;
//            double n = 0;
//            double a = 0;
//
//            double b2 = 0;
//            double n2 = 0;
//
//            for (Dataset dataset : datasets) {
//                FeatureSelectionResult result = dataset.getResult(fss.getName());
//                FeatureSelectionResult resultOrdered = dataset.getResultOrdered(fss.getName());
//
//                int[] sfB = resultOrdered.getSelectedFeatures();
//                int[] sfN = result.getSelectedFeatures();
////                int[] snA = rankFeatures(mla, dataset, sfN);
//                int[] snA = sfN;
//
//                ClassificationResult crB = mla.evaluate(dataset, sfB);
//                ClassificationResult crN = mla.evaluate(dataset, sfN);
////                ClassificationResult crB2 = (ClassificationResult) resultOrdered.getResult(mla.getName());
//                ClassificationResult crN2 = (ClassificationResult) result.getResult(mla.getName());
//                ClassificationResult crA = mla.evaluate(dataset, snA);
//
//                List<Pair<Double, ClassificationResult>> r = earr(Arrays.asList(
//                        crB, crN, crA,
//                        crN2
//                ), beta);
//
//                b += r.get(0).first;
//                n += r.get(1).first;
//                a += r.get(2).first;
//
//                b2 += r.get(3).first;
////                n2 += r.get(4).first;
//            }
//
//            System.out.print(fss.getName());
//            System.out.print(',');
//            System.out.print(b);
//            System.out.print(',');
//            System.out.print(n);
//            System.out.print(',');
//            System.out.print(a);
//
//            System.out.print(',');
//            System.out.print(b2);
//            System.out.print(',');
//            System.out.print(n2);
//
//            System.out.println();
//        }
//
//    }

    private static List<Pair<Double, ClassificationResult>> earr(List<ClassificationResult> results, double beta) {
        long normalizationFactor = results.size() - 1;
        return results.stream().map(a -> Pair.of((results.stream().mapToDouble(b -> AEARRComputers.computeAearr(a, b, beta)).sum() - 1) / normalizationFactor, a)).collect(Collectors.toList());
    }
}
