package ru.ifmo.ctddev.FSSARecSys.init;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ru.ifmo.ctddev.FSSARecSys.privetives.BaseDataset;
import ru.ifmo.ctddev.FSSARecSys.ImplementationManager;
import ru.ifmo.ctddev.FSSARecSys.interfaces.Dataset;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public class Initialize {
    public static void main(String[] ignore) throws MalformedURLException, URISyntaxException {

        File folder = new File("/home/itan/MasterDissertation/Projects/system/Datasets/ds2");
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            Arrays.sort(listOfFiles);
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".arff")) {
                    datasetFrom(file.getName().replace(".arff", "").replace(".", "-").replace("_", "-"), file.toURI().toURL());
                }
            }
        }


        //        initialize("init/Datasets2.csv", Initialize::datasetFromCSVRecord);



//        // todo get rid of duplication errors
//        initialize("init/FSSAlgorithms.csv", Initialize::fssAlgorithmFromCSVRecord);
//
//        // todo get rid of duplication errors
//        initialize("init/DatasetMetaFeatureExtractors.csv", Initialize::datasetMetaFeatureExtractorFromCSVRecord);

//        initialize("init/Classifiers.csv", Initialize::loadMetaLearningAlgorithms);
    }
//
//    private static void loadMetaLearningAlgorithms(CSVRecord csvRecord) {
//        MetaLearningAlgorithm classifier = new ClassifierAlgorithm(); // todo migrate to abstract meta-learning algorithms
//        classifier.setName(csvRecord.get(0));
//        classifier.setClassifier(ClassUtils.load(Classifier.class, csvRecord.get(1))); // todo migrate to abstract meta-learning algorithms
//        classifier.setOptions(csvRecord.get(2));
//        classifier.setTaskType(csvRecord.get(3));  // todo?
//
//        ImplementationManager.database.save(classifier);
//    }
//
//    private static void datasetMetaFeatureExtractorFromCSVRecord(CSVRecord csvRecord) {
//        MetaFeatureExtractor mfExtractor = ClassUtils.load(MetaFeatureExtractor.class, csvRecord.get(0));
//        ImplementationManager.database.save(mfExtractor);
//
//        System.out.println(mfExtractor);
//    }

    private static void datasetFrom(String name, URL url) {
        Dataset dataset = new BaseDataset(name, url, "");

        if (ImplementationManager.couchdb.object("DS:" + dataset.getName()).exists()) {
//            System.out.println("Already here " + dataset.getName());
            return;
        }


        System.out.print("Loading " + dataset.getName() + ' ');
        System.out.print(dataset.getInstances().classAttribute().index());
        System.out.print('#');
        System.out.println(dataset.getInstances().classAttribute().name());

        dataset.setIncluded(false);
        dataset.saveToCouchDB();
    }

//    private static void fssAlgorithmFromCSVRecord(CSVRecord csvRecord) {
//        FeatureSubsetSelectionAlgorithm fssAlgorithm = new BaseFeatureSubsetSelectionAlgorithm();
//        fssAlgorithm.setName(csvRecord.get(0));
//        fssAlgorithm.setSearcher(ClassUtils.load(ASSearch.class, csvRecord.get(1)));
//        fssAlgorithm.setSearchOptions(csvRecord.get(2));
//        fssAlgorithm.setEvaluator(ClassUtils.load(ASEvaluation.class, csvRecord.get(3)));
//        fssAlgorithm.setEvaluatorOptions(csvRecord.get(4));
//
//        System.out.println(fssAlgorithm);
//        ImplementationManager.database.save(fssAlgorithm);
//    }
//
    public static void initialize(String file, Consumer<CSVRecord> function) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL recourse = classloader.getResource(file);

        if (recourse == null) {
            throw new Error("Unable to find " + file);
        }

        CSVParser parser;

        try {
            parser = CSVParser.parse(new File(recourse.getFile()), StandardCharsets.UTF_8, CSVFormat.EXCEL);
        } catch (IOException e) {
            throw new Error("Error during parsing " + file + " file", e);
        }

        for (CSVRecord csvRecord : parser) {
            function.accept(csvRecord);
        }
    }
}
