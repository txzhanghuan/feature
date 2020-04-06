package com.github.zh.engine.co;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 阿桓
 * Date: 2020/3/26
 * Time: 10:24 上午
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractFeatureBean implements IFeatureBean {

    protected String name;

    protected String description;

    protected List<String> parents;

    protected List<String> children;
}
