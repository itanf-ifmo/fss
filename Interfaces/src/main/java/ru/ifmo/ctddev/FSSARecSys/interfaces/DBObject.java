package ru.ifmo.ctddev.FSSARecSys.interfaces;

import java.util.List;

public interface DBObject {
    String getId();

    Integer getInt(String paramName);

    Integer getInt(String paramName, Object def);

    Double getDouble(String paramName);

    Double getDouble(String paramName, Object def);

    String getString(String paramName);

    String getString(String paramName, Object def);

    Boolean getBoolean(String paramName);

    Boolean getBoolean(String paramName, Object def);

    <T> T getClazz(Class<? super T> baseClazz, String paramName);

    Object getObject(String paramName);

    Object getObject(String paramName, Object def);

    List getList(String paramName);

    DBObject put(String paramName, Object object);

    DBObject put(String paramName, int[] object);

    DBObject put(String paramName, double[] object);

    DBObject put(String paramName, List<? extends CouchDBSavable> list);

    <T extends CouchDBSavable> DBObject put(String paramName, T object);

    DBObject remove(String paramName);

    boolean exists();

    DBObject load();

    DBObject save();

    DBObject delete();
}

