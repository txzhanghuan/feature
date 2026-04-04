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

package top.volusus.postprocessor;

import top.volusus.engine.co.bean.NativeFeatureBean;
import top.volusus.engine.interfaces.FeatureBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

/**
 * @author zhanghuan
 * @version 1.0
 * @date 2021/9/17 14:35
 */
@Component
public class SignUpFeatureBeanPostProcessor implements FeatureBeanPostProcessor<NativeFeatureBean> {

    @Override
    public NativeFeatureBean postProcessAfterInitializationFeature(NativeFeatureBean featureBean) throws BeansException {
        System.out.println(featureBean.getName());
        System.out.println(featureBean.getProperties().toString());
        return featureBean;
    }

    @Override
    public Class<?> returnClass() {
        return NativeFeatureBean.class;
    }
}
