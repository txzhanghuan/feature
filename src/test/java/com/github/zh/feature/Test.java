package com.github.zh.feature;


import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.annotation.FeatureClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Smaple
 *
 * @author zhanghuan
 * @created 2020/01/27
 */
@FeatureClass
@Component
@Slf4j
public class Test {

    @Feature(name = "test5")
    public Integer test5(Integer test4) {
        return test4 + 1;
    }

    @Feature(name = "test4", output = false)
    public Integer test4() {
        return 1;
    }
}
