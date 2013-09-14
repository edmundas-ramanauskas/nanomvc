package com.nanomvc;

import redis.clients.jedis.Jedis;

public class RedisFactory {

    private static Jedis jedis = null;

    public static Jedis getInstance() {
        if (jedis == null) {
            jedis = new Jedis("localhost");
        }
        return jedis;
    }
}