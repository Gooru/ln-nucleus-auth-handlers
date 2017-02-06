package org.gooru.nucleus.auth.handlers.app.components;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializer;
import org.gooru.nucleus.auth.handlers.constants.ConfigConstants;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisClient implements Initializer, Finalizer {

    private JedisPool pool = null;

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        JsonObject redisConfig = config.getJsonObject(ConfigConstants.REDIS);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1000);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxWaitMillis(30000);
        jedisPoolConfig.setTestOnBorrow(true);
        pool =
            new JedisPool(jedisPoolConfig, redisConfig.getString(ConfigConstants.HOST),
                redisConfig.getInteger(ConfigConstants.PORT));
    }

    public static RedisClient instance() {
        return Holder.INSTANCE;
    }

    public JsonObject getJsonObject(final String key) {
        JsonObject result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String json = jedis.get(key);
            if (json != null) {
                result = new JsonObject(json);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public String get(final String key) {
        String value;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            value = jedis.get(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }

    public void del(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void expire(String key, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.expire(key, seconds);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, String value, int expireInSeconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(key, value);
            jedis.expire(key, expireInSeconds);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private Jedis getJedis() {

        return pool.getResource();
    }

    @Override
    public void finalizeComponent() {
        if (pool != null) {
            pool.destroy();
        }
    }

    private static final class Holder {
        private static final RedisClient INSTANCE = new RedisClient();
    }
}
