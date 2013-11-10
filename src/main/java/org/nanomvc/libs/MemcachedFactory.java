package org.nanomvc.libs;

import java.io.IOException;
import java.net.InetSocketAddress;
import net.spy.memcached.MemcachedClient;

public class MemcachedFactory {

    private static MemcachedClient memcache = null;

    public static MemcachedClient getInstance() throws IOException {
        if (memcache == null) {
            memcache = new MemcachedClient(new InetSocketAddress[]{new InetSocketAddress("127.0.0.1", 11211)});
        }
        return memcache;
    }
}