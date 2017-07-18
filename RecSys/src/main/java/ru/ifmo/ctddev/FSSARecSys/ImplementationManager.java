package ru.ifmo.ctddev.FSSARecSys;

import ru.ifmo.ctddev.FSSARecSys.db.CouchDB;

public class ImplementationManager {
//    public static Database database = new HibernateDatabase();
//    public static CouchDB couchdb = new CouchDB("http://couchdb.home.tanfilyev.ru", "main");
//    public static CouchDB couchdbResults = new CouchDB("http://couchdb.home.tanfilyev.ru", "results3");
    public static CouchDB couchdb = new CouchDB("https://couchdb.home.tanfilyev.ru", "main");
    public static CouchDB couchdbResults = new CouchDB("https://couchdb.home.tanfilyev.ru", "results3");
//    public static Class<? extends Dataset> datasetClass = BaseDataset.class;
//    public static Class<? extends FeatureSelectionResult> featureSelectionResult = BaseFeatureSelectionResult.class;
}
