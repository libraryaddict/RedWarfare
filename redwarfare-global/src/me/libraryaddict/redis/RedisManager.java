package me.libraryaddict.redis;

import java.io.IOException;

import me.libraryaddict.core.utils.UtilError;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;

public class RedisManager {
    private static JedisPool _redis;

    public static void addListener(JedisPubSub listener, RedisKey notify) {
        if (notify.getKey().contains("*")) {
            System.out.println("Registered patterned listener for " + notify.getKey());

            // XXX Overrides correct method

            try {
                getRedis().psubscribe(listener, notify.getKey());
            }
            catch (Exception throwable) {
                UtilError.handle(throwable);

                if (throwable instanceof IOException)
                    addListener(listener, notify);
            }
        } else {
            System.out.println("Registered listener for " + notify.getKey());

            try {
                getRedis().subscribe(listener, notify.getKey());
            }
            catch (Exception throwable) {
                UtilError.handle(throwable);

                if (throwable instanceof IOException)
                    addListener(listener, notify);
            }
        }
    }

    public static Jedis getRedis() {
        assert _redis != null;

        synchronized (_redis) {
            // System.out.println("Creating new redis connection.. " + _redis.getNumActive() + " connections active");
            return _redis.getResource();
        }
    }

    private String _password = "Debian!";
    private String _url = "localhost";

    public RedisManager() {
        this(4);
    }

    public RedisManager(int minIdle) {
        System.out.println("Enabling: Redis Manager");

        if (_url.isEmpty())
            throw new UnsupportedOperationException("No redis database has been setup");

        JedisPoolConfig config = new JedisPoolConfig();

        config.setMinIdle(minIdle);
        config.setMaxIdle(99);
        config.setNumTestsPerEvictionRun(4);
        config.setTestWhileIdle(true);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setMinEvictableIdleTimeMillis(15000);
        config.setMaxTotal(100);
        config.setMaxWaitMillis(120000);
        config.setBlockWhenExhausted(false);

        _redis = new JedisPool(config, _url, Protocol.DEFAULT_PORT, 0, _password, Protocol.DEFAULT_DATABASE, null);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public boolean equals(Object obj) {
                return obj != null && hashCode() == obj.hashCode();
            }

            public int hashCode() {
                return "redis".hashCode();
            }

            public void run() {
                _redis.close();
            }
        });
    }
}
