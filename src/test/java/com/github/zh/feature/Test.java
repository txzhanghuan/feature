package com.github.zh.feature;


import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.annotation.FeatureClass;
import com.github.zh.engine.annotation.properties.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Smaple
 *
 * @author zhanghuan
 * @date 2020/01/27
 */
@FeatureClass
@Component
@Slf4j
public class Test {

    @Autowired
    private OtherBean otherBean;

    @Feature
    public Integer testA() {
        int result = 5;
        System.out.println(otherBean.getTest());
//        System.out.println("testA = " + result);
        return result;
    }

    @Property(key = "a", value = "b")
    @Property(key = "b", value = "b")
    @Feature(output = false)
    public Integer testB(Integer testA) throws InterruptedException {
        int result = testA + 1;
//        Thread.sleep(3000);
        return result;
    }

    @Feature(output = false)
    public Integer testC(Integer testA) throws InterruptedException {
        int result = testA + 1;
//        Thread.sleep(4000);
        return result;
    }

    @Feature
    public Integer testD(Integer testB, Integer testA) {
        int result = testB + testA;
        return result;
    }

    @Feature
    public Map<String, Integer> testE(Integer testD) {

        Map<String, Integer> result = new HashMap<>();
        result.put("testD", testD);
        return result;
    }

    @Feature
    public Integer testF(Map<String, Integer> testE, Integer testC) {
        return 1;
    }
}
