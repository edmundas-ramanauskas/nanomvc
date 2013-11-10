package org.nanomvc.libs;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.MapreduceResults;
import com.google.code.morphia.MapreduceType;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.FieldEnd;
import com.google.code.morphia.query.Query;
import com.mongodb.MongoURI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mongo {

    private static Logger _log = LoggerFactory.getLogger(Mongo.class);
    private Class mongoClass;
    private static Datastore ds;
    private String database;
    private Integer limit = null;
    private Integer offset = null;
    private String orderField = null;
    private String orderDirection = null;
    private Map<String, Object> criteria = null;
    private List<String> select = null;
    public static String ASC = "";
    public static String DESC = "-";

    public Mongo(String database, Class mongoClass) {
        this.database = database;
        this.mongoClass = mongoClass;
        init();
    }

    private void init() {
        try {
            if (ds == null) {
                try {
                    String services = System.getenv("VCAP_SERVICES");
                    try {
                        JSONObject json = new JSONObject(services);
                        Iterator iter = json.keys();
                        while (iter.hasNext()) {
                            String key = iter.next().toString();
                            if (key.startsWith("mongo")) {
                                String url = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("url");
                                String db = json.getJSONArray(key).getJSONObject(0).getJSONObject("credentials").getString("db");
                                com.mongodb.Mongo m = new com.mongodb.Mongo(new MongoURI(url));
                                ds = new Morphia().createDatastore(m, db);
                                break;
                            }
                        }
                    } catch (JSONException ex) {
                        throw new Exception("");
                    }
                } catch (Throwable t) {
                    ds = new Morphia().createDatastore(new com.mongodb.Mongo(), this.database);
                }
                ds.ensureIndexes();
            }
        } catch (UnknownHostException | NullPointerException ex) {
        }
    }

    public Mongo init(Class mongoClass) {
        this.mongoClass = mongoClass;
        return this;
    }

    public Mongo setSelect(String field) {
        if (this.select == null) {
            this.select = new ArrayList();
        }
        this.select.add(field);
        return this;
    }

    public Mongo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Mongo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Mongo setOrder(String field, String direction) {
        this.orderField = field;
        this.orderDirection = direction;
        return this;
    }

    public Mongo setOrder(String field) {
        this.orderField = field;
        this.orderDirection = "";
        return this;
    }

    public Mongo addCriteria(String field, Object value) {
        if (this.criteria == null) {
            this.criteria = new HashMap();
        }
        this.criteria.put(field, value);

        return this;
    }

    public Mongo addCriteriaIn(String field, List items) {
        Criteria criteria = new Criteria(Type.IN, items);
        addCriteria(field, criteria);
        return this;
    }

    public Mongo addCriteriaNotIn(String field, List items) {
        Criteria criteria = new Criteria(Type.NOTIN, items);
        addCriteria(field, criteria);
        return this;
    }

    public Object findByPk(String pk) {
        return ds.find(this.mongoClass).filter("id", new ObjectId(pk)).get();
    }

    public Key getKeyByPk(String pk) {
        return getKey(findByPk(pk));
    }

    public Key getKey(Object obj) {
        return ds.getKey(obj);
    }

    public List findByKeys(Iterable keys) {
        return ds.getByKeys(keys);
    }

    public List find() {
        return query().asList();
    }

    public Long count() {
        return Long.valueOf(ds.getCount(query()));
    }

    public Iterable findIterable() {
        return query().fetch();
    }

    public Object findOne() {
        return query().get();
    }

    public void delete() {
        ds.delete(query());
    }

    public void deleteByPk(String pk) {
        Query q = ds.createQuery(this.mongoClass).filter("id", new ObjectId(pk));
        ds.delete(q);
    }

    public Key save(Object obj) {
        return ds.save(obj);
    }

    private Query query() {
        return createQuery();
    }

    public Object getSum(String groupBy, String fieldSum) {
        String map = "function() { emit(this." + groupBy + ", { " + fieldSum + ": this." + fieldSum + " }); }";
        String reduce = "function(key, values) { return Array.sum(values); };";

        MapreduceResults result = ds.mapReduce(MapreduceType.REDUCE, createQuery(), map, reduce, "", null, this.mongoClass);
        if (result.isOk()) {
            return result.createQuery().get();
        }
        return null;
    }

    private Query createQuery() {
        Query query = ds.createQuery(this.mongoClass);
        if (this.limit != null) {
            query.limit(this.limit.intValue());
        }
        if (this.offset != null) {
            query.offset(this.offset.intValue());
        }
        if (this.criteria != null) {
            for (Map.Entry entry : this.criteria.entrySet()) {
                if ((entry.getValue() instanceof Criteria)) {
                    Criteria criteria = (Criteria) entry.getValue();
                    switch (criteria.type) {
                        case IN:
                            query.field((String) entry.getKey()).in((List) criteria.value);
                            break;
                        case NOTIN:
                            query.field((String) entry.getKey()).notIn((List) criteria.value);
                    }
                } else {
                    query.filter((String) entry.getKey(), entry.getValue());
                }
            }
        }
        if (this.select != null) {
            String[] fields = (String[]) this.select.toArray(new String[0]);
            query.retrievedFields(true, fields);
        }
        if (this.orderField != null) {
            query.order(this.orderDirection + this.orderField);
        }
        clear();
        return query;
    }

    private void clear() {
        this.criteria = null;
        this.limit = null;
        this.offset = null;
        this.orderField = null;
        this.orderDirection = null;
    }

    class Criteria {

        public Mongo.Type type;
        public Object value;

        public Criteria(Mongo.Type type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    public static enum Type {

        IN, NOTIN;
    }
}