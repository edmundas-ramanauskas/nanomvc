package org.nanomvc.libs;

public class MongoFactory {
    public static Mongo loadMongo(Class mongoClass) {
        // @TODO: change to use configurable DB name
        return new Mongo("movies", mongoClass);
    }
}