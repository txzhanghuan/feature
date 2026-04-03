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

/**
 * Cache wrapper interface for key existence tracking.
 *
 * @author zhanghuan
 * @version 1.0
 * @date 2022/3/3 19:54
 * @deprecated Reserved for future use. Not currently utilized in the engine.
 *             Consider using external caching solutions if needed.
 */
@Deprecated
public interface CacheWrapper {

    /**
     * Sets a key in the cache.
     *
     * @param key the key to set
     */
    void set(String key);

    /**
     * Checks if a key exists in the cache.
     *
     * @param key the key to check
     * @return {@code true} if the key exists; {@code false} otherwise
     */
    Boolean get(String key);

    /**
     * Clears all entries from the cache.
     */
    void clearAll();
}
