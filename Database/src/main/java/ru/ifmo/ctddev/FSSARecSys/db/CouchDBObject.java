package ru.ifmo.ctddev.FSSARecSys.db;

import org.ektorp.CouchDbConnector;
import ru.ifmo.ctddev.FSSARecSys.interfaces.CouchDBSavable;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.utils.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CouchDBObject implements DBObject {
    private final CouchDbConnector connection;
    private Map<String, Object> data = new HashMap<>();

    CouchDBObject(CouchDbConnector connection, String id) {
        this.connection = connection;
        data.put("_id", id);
    }

    CouchDBObject(CouchDbConnector connection, Map data) {
        this.connection = connection;
        //noinspection unchecked
        this.data = data;
    }

    @Override
    public String getId() {
        return getString("_id");
    }

    @Override
    public Integer getInt(String paramName) {
        return getInt(paramName, null);
    }

    @Override
    public Integer getInt(String paramName, Object def) {
        return (Integer) data.getOrDefault(paramName, def);
    }

    @Override
    public Double getDouble(String paramName) {
        return getDouble(paramName, null);
    }

    @Override
    public Double getDouble(String paramName, Object def) {
        return ((Number) data.getOrDefault(paramName, def)).doubleValue();
    }

    @Override
    public String getString(String paramName) {
        return getString(paramName, null);
    }

    @Override
    public String getString(String paramName, Object def) {
        return (String) data.getOrDefault(paramName, def);
    }

    @Override
    public Boolean getBoolean(String paramName) {
        return getBoolean(paramName, null);
    }

    @Override
    public Boolean getBoolean(String paramName, Object def) {
        return (Boolean) data.getOrDefault(paramName, def);
    }

    @Override
    public<T> T getClazz(Class<? super T> baseClazz, String paramName) {
        //noinspection unchecked
        return (T) ClassUtils.load(baseClazz, getString(paramName));
    }

    @Override
    public Object getObject(String paramName) {
        return getObject(paramName, null);
    }

    @Override
    public Object getObject(String paramName, Object def) {
        return data.getOrDefault(paramName, def);
    }

    @Override
    public List getList(String paramName) {
        return (List) getObject(paramName);
    }

    @Override
    public DBObject put(String paramName, Object object) {
        data.put(paramName, object);
        return this;
    }
    @Override
    public DBObject put(String paramName, double[] object) {
        data.put(paramName, Arrays.stream(object).boxed().collect(Collectors.toList()));
        return this;
    }
    @Override
    public DBObject put(String paramName, int[] object) {
        data.put(paramName, Arrays.stream(object).boxed().collect(Collectors.toList()));
        return this;
    }

    @Override
    public DBObject put(String paramName, List<? extends CouchDBSavable> list) {
        data.put(paramName, list.stream().map(i -> ((CouchDBObject)i.getDBObject()).data).collect(Collectors.toList()));
        return this;
    }

    @Override
    public<T extends CouchDBSavable> DBObject put(String paramName, T object) {
        data.put(paramName, ((CouchDBObject)object.getDBObject()).data);
        return this;
    }

    @Override
    public DBObject remove(String paramName) {
        data.remove(paramName);
        return this;
    }

    public DBObject save() {
        if (exists()) {
            put("_rev", connection.get(Map.class, (String)data.get("_id")).get("_rev"));
            connection.update(data);
        } else {
            connection.create(getString("_id"), data);
        }
        return this;
    }

    @Override
    public DBObject load() {
        //noinspection unchecked
        data = connection.get(Map.class, (String)data.get("_id"));
        return this;
    }

    @Override
    public boolean exists() {
        return connection.contains((String)data.get("_id"));
    }

    @Override
    public DBObject delete() {

        if (!data.containsKey("_rev")) {
            throw new IllegalStateException("Should be loaded firstly!");
        }

        try {
            connection.delete(data);
            data.remove("_rev");
        } catch (NullPointerException e) {
            throw new IllegalStateException("Already deleted");
        }

        return this;
    }

    public String toString() {
        return (String)data.get("_id") + '@' + data.toString();
    }
}
