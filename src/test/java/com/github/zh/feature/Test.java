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

    @Feature(name = "testA")
    public Integer testA() {
        int result = 5;
        System.out.println("testA = " + result);
        return result;
    }

    @Feature(name = "testB", output = false)
    public Integer testB(Integer testA) throws InterruptedException {
        int result = testA + 1;
        System.out.println("testB sleep start time: " + System.currentTimeMillis());
        Thread.sleep(3000);
        System.out.println("testB sleep stop time: " + System.currentTimeMillis());
        return result;
    }

    @Feature(name = "testC", output = false)
    public Integer testC(Integer testA) throws InterruptedException {
        int result = testA + 1;
        System.out.println("testC sleep start time: " + System.currentTimeMillis());
        Thread.sleep(4000);
        System.out.println("testC sleep stop time: " + System.currentTimeMillis());
        return result;
    }

    @Feature(name = "testD")
    public Integer testD(Integer testB, Integer testC) {
        int result = testB + testC;
        System.out.println("testD = " + result);
        return result;
    }
}
