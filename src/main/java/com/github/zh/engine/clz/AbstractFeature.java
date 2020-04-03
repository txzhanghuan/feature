package com.github.zh.engine.clz;

/**
 * @author zhanghuan
 * @created 2020/01/29
 */
public abstract class AbstractFeature implements IFeature {

    protected Object bean;

    public AbstractFeature(Object bean) {
        this.bean = bean;
    }
}
