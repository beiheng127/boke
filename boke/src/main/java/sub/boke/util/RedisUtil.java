package sub.boke.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
        } catch (Exception e) {
            System.out.println("❌ Redis设置缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            System.out.println("❌ Redis获取缓存失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            System.out.println("❌ Redis删除缓存失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除缓存
     */
    public long delete(Set<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.out.println("❌ Redis批量删除缓存失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取匹配的键
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            System.out.println("❌ 获取Redis键失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, time, timeUnit));
        } catch (Exception e) {
            System.out.println("❌ 设置Redis过期时间失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            System.out.println("❌ 获取Redis过期时间失败: " + e.getMessage());
            return -2;
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            System.out.println("❌ 检查Redis键是否存在失败: " + e.getMessage());
            return false;
        }
    }
}