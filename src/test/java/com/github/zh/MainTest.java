package com.github.zh;

import com.github.zh.engine.FeatureEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * @author 阿桓
 * Date: 2020/4/8
 * Time: 7:51 下午
 * Description:
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MainTest {

    @Autowired
    FeatureEngine featureEngine;

    @Test
    public void testCalc() {
        Map<String, Object> result = featureEngine.calc(null, new HashSet<>(Arrays.asList("testF")));
//        Assert.assertEquals(2, result.get("test5"));
        System.out.println(result.toString());
    }
}
