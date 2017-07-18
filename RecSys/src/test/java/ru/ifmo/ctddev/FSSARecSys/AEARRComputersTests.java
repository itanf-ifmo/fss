package ru.ifmo.ctddev.FSSARecSys;

public class AEARRComputersTests {
//    private static double doTest(double fa, double fb, double na, double nb, double beta) {
//        ClassificationResult ra = new ClassificationResult();
//        TreeMap<String, Double> pra = new TreeMap<>();
//        pra.put(ClassificationResult.PROPERTIES.F1_MEASURE.toString(), fa);
//        pra.put(ClassificationResult.PROPERTIES.FEATURES_NUMBER.toString(), na);
//        ra.setProperties(pra);
//
//        ClassificationResult rb = new ClassificationResult();
//        TreeMap<String, Double> prb = new TreeMap<>();
//        prb.put(ClassificationResult.PROPERTIES.F1_MEASURE.toString(), fb);
//        prb.put(ClassificationResult.PROPERTIES.FEATURES_NUMBER.toString(), nb);
//        rb.setProperties(prb);
//
//        return AEARRComputers.computeAearr(ra, rb, beta);
//    }
//
//    private static double doTest2(int n, double f1, double nr, double beta) {
//        Dataset ds = new AEARRComputersTests.DatasetTest(n);
//
//        ClassificationResult rb = new ClassificationResult();
//        TreeMap<String, Double> prb = new TreeMap<>();
//        prb.put(ClassificationResult.PROPERTIES.F1_MEASURE.toString(), f1);
//        prb.put(ClassificationResult.PROPERTIES.FEATURES_NUMBER.toString(), nr);
//        rb.setProperties(prb);
//
//        return AEARRComputers.computeAearr(ds, rb, beta);
//    }
////
//    @Test
//    public void countRelativeAearrTest() {
//        assertEquals(doTest(0.98, 0.98, 100, 100, 0), 1.0, 0.0001);
//        assertEquals(doTest(0.97, 0.98, 100, 100, 0), 0.9897959183674511, 0.0001);
//        assertEquals(doTest(0.99, 0.98, 100, 100, 0), 1.010204081632549, 0.0001);
//
//        assertEquals(doTest(0.98, 0.98, 100, 100, 0.1), 1.0, 0.0001);
//        assertEquals(doTest(0.97, 0.98, 100, 100, 0.1), 0.9897959183674511, 0.0001);
//        assertEquals(doTest(0.99, 0.98, 100, 100, 0.1), 1.010204081632549, 0.0001);
//
//
//        assertEquals(doTest(0.98, 0.98, 100, 101, 0), 1.0, 0.0001);
//        assertEquals(doTest(0.97, 0.98, 100, 101, 0), 0.9897959183674511, 0.0001);
//        assertEquals(doTest(0.99, 0.98, 100, 101, 0), 1.010204081632549, 0.0001);
//
//        assertEquals(doTest(0.98, 0.98, 101, 100, 0.1), 0.9995680492837857, 0.0001);
//        assertEquals(doTest(0.97, 0.98, 101, 100, 0.1), 0.9893683753116063, 0.0001);
//        assertEquals(doTest(0.99, 0.98, 101, 100, 0.1), 1.0097677232559652, 0.0001);
//
//
//        assertEquals(doTest(0.98, 0.98, 1, 1000, 0.1), 1.0, 0.0001);
//        assertEquals(doTest(0.97, 0.98, 1, 1000, 0.1), 0.9897959183674511, 0.0001);
//        assertEquals(doTest(0.99, 0.98, 1, 1000, 0.1), 1.010204081632549, 0.0001);
//
//        assertEquals(doTest(0.98, 0.98, 1000, 1, 0.1), 0.769230769231026, 0.0001);
//        assertEquals(doTest(0.97, 0.98, 1000, 1, 0.1), 0.7613814756675241, 0.0001);
//        assertEquals(doTest(0.99, 0.98, 1000, 1, 0.1), 0.7770800627945278, 0.0001);
//    }
//
//    private static class DatasetTest implements Dataset {
//        private int fn;
//
//        DatasetTest(int fn) {
//            this.fn = fn;
//        }
//
//        @Override
//        public DBObject getDBObject() {
//            return null;
//        }
//
//        @Override
//        public Dataset loadFromCouchDB(String name) {
//            return null;
//        }
//
//        @Override
//        public Dataset loadFromCouchDB(DBObject DBObject) {
//            return null;
//        }
//
//        @Override
//        public String getName() {
//            return null;
//        }
//
//        @Override
//        public void setName(String name) {
//
//        }
//
//        @Override
//        public Instances getInstances() {
//            return null;
//        }
//
//        @Override
//        public Instances getInstances(FeatureSelectionResult featureSelectionResult) {
//            return null;
//        }
//
//        @Override
//        public Instances getInstances(int[] selectedFeatures) {
//            return null;
//        }
//
//        @Override
//        public void setInstances(URL url) {
//
//        }
//
//        @Override
//        public void setInstances(Instances instances) {
//
//        }
//
//        @Override
//        public Map<String, Double> getMetaFeatures() {
//            return null;
//        }
//
//        @Override
//        public void setMetaFeatures(Map<String, Double> metaFeatures) {
//
//        }
//
//        @Override
//        public String getTaskType() {
//            return null;
//        }
//
//        @Override
//        public void setTaskType(String taskType) {
//
//        }
//
//        @Override
//        public void setResults(String fssAlgorithmName, FeatureSelectionResult featureSelectionResult) {
//
//        }
//
//        @Override
//        public void setIncluded(boolean flag) {
//        }
//
//        @Override
//        public boolean getIncluded() {
//            return false;
//        }
//
//        @Override
//        public int getFeaturesNumber() {
//            return fn;
//        }
//
//        @Override
//        public FeatureSelectionResult getResult(String fssAlgorithmName) {
//            return null;
//        }
//
//        @Override
//        public FeatureSelectionResult getResult(String fssAlgorithmName, String b) {
//            return null;
//        }
//    }
//
//    @Test
//    public void countAbsoluteAearrTest() {
//        assertEquals(doTest2(1000, 0.98, 1000, 0), 0.98, 0.00001);
//        assertEquals(doTest2(1000, 0.98, 1000, 0.1), 0.98, 0.00001);
//        assertEquals(doTest2(1000, 0.98, 100, 0.1), 1.08888888888888, 0.00001);
//        assertEquals(doTest2(1000, 0.97, 100, 0.1), 1.07777777777777, 0.00001);
//    }
}
