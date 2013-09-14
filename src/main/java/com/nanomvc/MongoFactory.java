package com.nanomvc;

public class MongoFactory
{
  public static Mongo loadMongo(Class mongoClass)
  {
    return new Mongo("movies", mongoClass);
  }
}