package com.github.zh.engine.clz;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
public interface IFeature {

    /**
     * 执行接口
     *
     * @param args 入参
     * @return 结果
     */
    Object execute(Object[] args);
}
