package com.github.zh.engine.cache;

import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2022/3/3 20:04
 */
@Component
public class DefaultCacheImpl implements CacheWrapper {

    private final HashSet<String> hashSet = new HashSet<>();

    @Override
    public void set(String key) {
        hashSet.add(key);
    }

    @Override
    public Boolean get(String key) {
        return hashSet.contains(key);
    }

    @Override
    public void clearAll() {
        hashSet.clear();
    }
}
