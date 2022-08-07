package cn.hfbin.seckill.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description redis service
 * @Date 2022/8/7 12:17
 * @Author zhuzhiwei
 */
@Service
public class RedisService {

    private final JedisPool jedisPool;

    public RedisService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public <T> T get(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String str = jedis.get(key);
            return stringToBean(str, clazz);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 获取当个对象
     */
    public <T> T get(String prefix, String key, Class<T> clazz) {
        return get(prefix + key, clazz);
    }

    public Long expice(String prefix, String key, int exTime) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.expire(prefix + key, exTime);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 设置对象
     */
    public <T> boolean set(String prefix, String key, T value, int exTime) {
        return set(prefix + key, value, exTime);
    }

    /**
     * 设置对象
     */
    public <T> boolean set(String key, T value, int exTime) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            if (exTime == 0) {
                //直接保存
                jedis.set(key, str);
            } else {
                //设置过期时间
                jedis.setex(key, exTime, str);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }
    }

    public Long del(String prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(prefix + key);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     */
    public Long incr(String prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incr(prefix + key);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public Long decr(String prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.decr(prefix + key);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * bean 转 String
     *
     * @param value 对象
     * @return json
     */
    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == Integer.class || clazz == String.class || clazz == Long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }


    /**
     * string转bean
     *
     * @param str   json
     * @param clazz class
     * @return 实例对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}
