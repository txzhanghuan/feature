package com.github.zh.feature;

import org.springframework.stereotype.Component;

/**
 * @author zhanghuan
 * @version 1.0
 * @date 2021/9/3 14:54
 */
@Component
public class OtherBean {

    public String getTest() {
        return "OtherBean call";
    }
}
