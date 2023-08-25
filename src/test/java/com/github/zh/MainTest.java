package com.github.zh;

import com.github.zh.bean.OuterFeatureBean;
import com.github.zh.engine.FeatureEngine;
import com.github.zh.engine.co.AbstractFeatureBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

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
        System.out.println(result.toString());
    }

    @Test
    public void testCalcWithOuterFeatureBean() {
        Map<String, AbstractFeatureBean> map = new HashMap<>();
        OuterFeatureBean outerFeatureBean = new OuterFeatureBean();
        outerFeatureBean.setParents(new ArrayList<>(Arrays.asList("testE")));
        outerFeatureBean.setName("testOuter");
        outerFeatureBean.setChildren(new ArrayList<>());
        outerFeatureBean.setOutput(true);
        map.put(outerFeatureBean.getName(), outerFeatureBean);
        Map<String, Object> result = featureEngine.calcWithOuterFeatureBean(null, new HashSet<>(Arrays.asList("testF")), map);
        System.out.println(result.toString());
    }
}
