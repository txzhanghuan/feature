package engine.co.bean;

import engine.annotation.Feature;
import engine.clz.IFeature;
import engine.co.AbstractFeatureBean;
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
    public NativeFeatureBean(String name, List<String> parents,
                             List<String> children, Feature featureMetaData, IFeature feature,
                             Class<?> returnType){
        super(name, parents, children);
        this.featureMetaData = featureMetaData;
        this.feature = feature;
        this.returnType = returnType;
    }

    @Override
    public Object execute(Object[] args) {
        return feature.execute(args);
    }
}
