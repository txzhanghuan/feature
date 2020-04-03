package engine.co.bean;

import engine.co.AbstractFeatureBean;

import java.util.function.Function;

/**
 * @author 阿桓
 * Date: 2020/3/27
 * Time: 2:17 下午
 * Description:
 */
public class OuterFeatureBean extends AbstractFeatureBean {

    private Function<Object[], Object> fn;

    @Override
    public Object execute(Object[] args) {
        return fn.apply(args);
    }

    public OuterFeatureBean(String name, Function<Object[], Object> fn){
        this.name = name;
        this.fn = fn;
    }
}
