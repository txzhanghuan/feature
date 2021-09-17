package com.github.zh.engine.co;

/**
 * @author 阿桓
 * Date: 2020/3/26
 * Time: 9:50 上午
 * Description:
 */
@FunctionalInterface
public interface IFeatureBean {

    /**
     * 运算
     *
     * @param args
     * @return
     */
    Object execute(Object[] args);
}
