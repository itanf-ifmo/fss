package ru.ifmo.ctddev.FSSARecSys.db;

import org.ektorp.*;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DB;
import ru.ifmo.ctddev.FSSARecSys.interfaces.DBObject;
import ru.ifmo.ctddev.FSSARecSys.utils.Pair;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CouchDB implements DB {
    private final String url;
    private final String databaseName;
    private CouchDbConnector db;

    public CouchDB(String url, String databaseName) {
        this.url = url;
        this.databaseName = databaseName;
    }

    public static void main(String[] ignore) throws MalformedURLException {
        new CouchDB("http://localhost:5984", "testDB").start();
    }

    private void start() throws MalformedURLException {
        HttpClient httpClient = new StdHttpClient.Builder()
                .url(url)
                .build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        CouchDbConnector db = new StdCouchDbConnector(databaseName, dbInstance);

        db.createDatabaseIfNotExists();


//        Map<String,Object> userData = new HashMap<>();
//        Map<String,String> nameStruct = new HashMap<>();
//        nameStruct.put("first", "Joe");
//        nameStruct.put("last", "Sixpack");
//        userData.put("name", nameStruct);
//        userData.put("_id", "aaa-a-a--a-a---aa");
//        userData.put("gender", "MALE");
//        userData.put("verified", Boolean.FALSE);
//        userData.put("userImage", "Rm9vYmFyIQ==");
//
//
//        db.create(userData);

//        System.out.println(db.get(Map.class, "aaa-a-a--a-a---aa"));
//        System.out.println(((int)((Map)db.get(Map.class, "aaa-a-a--a-a---aa").get("name")).get("last")) + 1);

//        getAll("SR:SR:").parallelStream().forEach(o -> o.put("_id", "LR:" + o.getId().substring(3)).remove("_rev").save());
//        getAll("SR:SR:").parallelStream().forEach(o -> o.delete());
//        for (DBObject o : ) {
//            System.out.println(o);
////            o.delete();
//            o.put("_id", "LR:" + o.getId().substring(3));
//            o.remove("_rev");
//            o.save();
//        }


    }

    private CouchDbConnector getConnection() {
        if (db == null) {
            HttpClient httpClient = null;
            try {
                httpClient = new StdHttpClient
                        .Builder()
                        .socketTimeout(100000)
                        .connectionTimeout(10000000)
                        .url(url)
                        .username("admin")
                        .password(System.getProperty("COUCHDB_PASSWORD", "changeme42"))
                        .build();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
            db = new StdCouchDbConnector(databaseName, dbInstance);

            if (!dbInstance.checkIfDbExists(new DbPath(databaseName))) {
                db.createDatabaseIfNotExists();

            }
        }

        return db;
    }

    @Override
    public List<DBObject> getAll(String keyPrefix) {
        ViewQuery query = new ViewQuery().designDocId("_design/all").viewName("all").startKey(keyPrefix).endKey(keyPrefix.substring(0, keyPrefix.length() - 1) + '!');
        return getConnection().queryView(query, Map.class).stream().map(m -> new CouchDBObject(getConnection(), m)).collect(Collectors.toList());
    }

    @Override
    public DBObject object(String id) {
        return new CouchDBObject(getConnection(), id);
    }

    @Override
    public Map<String, Pair<Double, Double>> getMinMax() {
        ViewQuery query = new ViewQuery().designDocId("_design/all").viewName("min-max-features").queryParam("group", "true");

        return getConnection().queryView(query).getRows().stream().collect(Collectors.toMap(ViewResult.Row::getKey, row -> Pair.of(row.getValueAsNode().get(0).asDouble(), row.getValueAsNode().get(1).asDouble())));
    }
}
