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

package com.github.zh.engine.cache;

import org.springframework.stereotype.Component;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2022/3/3 19:54
 * @deprecated Reserved for future use. Not currently used in the engine.
 */
@Deprecated
@Component
public interface CacheWrapper {

    /**
     * 设置key是否存在
     *
     * @param key
     */
    void set(String key);

    /**
     * 判断Key是否存在
     *
     * @param key
     * @return
     */
    Boolean get(String key);

    void clearAll();
}
