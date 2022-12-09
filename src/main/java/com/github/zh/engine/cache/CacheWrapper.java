package com.github.zh.engine.cache;

import org.springframework.stereotype.Component;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2022/3/3 19:54
 */
@Component
public interface CacheWrapper {

    /**
     * 设置key是否存在
     *
     * @param key
     */
    void set(String key);

    /**
     * 判断Key是否存在
     *
     * @param key
     * @return
     */
    Boolean get(String key);

    void clearAll();
}
