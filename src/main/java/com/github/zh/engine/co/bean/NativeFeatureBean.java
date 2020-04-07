package com.github.zh.engine.co.bean;

import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.clz.IFeature;
import com.github.zh.engine.co.AbstractFeatureBean;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 阿桓
 * Date: 2020/3/20
 * Time: 11:48 上午
 * Description: Feature的MetaData
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NativeFeatureBean extends AbstractFeatureBean {

    private Feature featureMetaData;

    private IFeature feature;

    private Class<?> returnType;

    @Builder
    public NativeFeatureBean(String name, boolean output, List<String> parents,
                             List<String> children, Feature featureMetaData, IFeature feature,
                             Class<?> returnType) {
        super(name, output, parents, children);
        this.featureMetaData = featureMetaData;
        this.feature = feature;
        this.returnType = returnType;
    }

    @Override
    public Object execute(Object[] args) {
        return feature.execute(args);
    }
}
