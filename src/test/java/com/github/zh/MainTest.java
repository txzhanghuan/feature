/*
 * Copyright (C) 2026 zhanghuan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zh;

import com.github.zh.bean.OuterFeatureBean;
import com.github.zh.engine.FeatureEngine;
import com.github.zh.engine.co.AbstractFeatureBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author zhanghuan
 * Date: 2020/4/8
 * Time: 7:51 PM
 * Description:
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MainTest {

    @Autowired
    FeatureEngine featureEngine;

    @Test
    public void testCalcShouldReturnOutputFeatures() {
        Map<String, Object> result = featureEngine.calc(null, new HashSet<>(Arrays.asList("testF")));
        
        assertNotNull("Result should not be null", result);
        // testF is output=true, should be in result
        assertTrue("Result should contain testF", result.containsKey("testF"));
        assertEquals("testF should return 1", 1, result.get("testF"));
        
        // testA, testD, testE are output=true (default)
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertEquals("testA should return 5", 5, result.get("testA"));
        
        assertTrue("Result should contain testD", result.containsKey("testD"));
        assertEquals("testD should return 11 (testB + testA = 6 + 5)", 11, result.get("testD"));
        
        assertTrue("Result should contain testE", result.containsKey("testE"));
        @SuppressWarnings("unchecked")
        Map<String, Integer> testEResult = (Map<String, Integer>) result.get("testE");
        assertEquals("testE should contain testD=11", Integer.valueOf(11), testEResult.get("testD"));
        
        // testB and testC are output=false, should NOT be in result
        assertFalse("Result should not contain testB (output=false)", result.containsKey("testB"));
        assertFalse("Result should not contain testC (output=false)", result.containsKey("testC"));
    }

    @Test
    public void testCalcWithOuterFeatureBeanShouldIncludeOuterBean() {
        Map<String, AbstractFeatureBean> map = new HashMap<>();
        OuterFeatureBean outerFeatureBean = new OuterFeatureBean();
        outerFeatureBean.setParents(new ArrayList<>(Arrays.asList("testE")));
        outerFeatureBean.setName("testOuter");
        outerFeatureBean.setChildren(new ArrayList<>());
        outerFeatureBean.setOutput(true);
        map.put(outerFeatureBean.getName(), outerFeatureBean);
        Map<String, Object> result = featureEngine.calcWithOuterFeatureBean(null, new HashSet<>(Arrays.asList("testF")), map);
        
        assertNotNull("Result should not be null", result);
        // testF should still be calculated
        assertTrue("Result should contain testF", result.containsKey("testF"));
        assertEquals("testF should return 1", 1, result.get("testF"));
        
        // outerFeatureBean should be executed and included (output=true)
        // OuterFeatureBean.execute returns args[0] which is testE
        assertTrue("Result should contain testOuter", result.containsKey("testOuter"));
        @SuppressWarnings("unchecked")
        Map<String, Integer> testOuterResult = (Map<String, Integer>) result.get("testOuter");
        assertEquals("testOuter should return testE value", Integer.valueOf(11), testOuterResult.get("testD"));
    }
}
