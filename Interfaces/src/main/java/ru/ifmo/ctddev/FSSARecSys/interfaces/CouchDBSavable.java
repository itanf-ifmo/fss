package ru.ifmo.ctddev.FSSARecSys.interfaces;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public interface CouchDBSavable<T extends CouchDBSavable> {

    DBObject getDBObject();
    default DBObject getDBObject(DB db) {
        throw new NotImplementedException();
    }

    T loadFromCouchDB(String name);
    T loadFromCouchDB(DBObject DBObject);

    default void saveToCouchDB() {
        getDBObject().save();
    }

    default void saveToCouchDB(DB db) {
        getDBObject(db).save();
    }

//    static <T extends CouchDBSavable> T loadFromCouchDB(String id) {
//        T a;
//        a.loadFromCouchDB();
//        return null;
//    }
}
