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
        return result;
    }

    @Property(key = "a", value = "b")
    @Property(key = "b", value = "b")
    @Feature(output = false)
    public Integer testB(Integer testA) throws InterruptedException {
        int result = testA + 1;
        return result;
    }

    @Feature(output = false)
    public Integer testC(Integer testA) throws InterruptedException {
        int result = testA + 1;
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
