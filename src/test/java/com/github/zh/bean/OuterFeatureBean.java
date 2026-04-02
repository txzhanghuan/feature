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

package com.github.zh.bean;

import com.github.zh.engine.co.AbstractFeatureBean;

/**
 * @author zhanghuan
 * @version 1.0
 * @date 2021/9/17 16:09
 */
public class OuterFeatureBean extends AbstractFeatureBean {
    @Override
    public Object execute(Object[] args) {
        return args[0];
    }
}
