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

package top.volusus.engine.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Default in-memory implementation of {@link CacheWrapper}.
 *
 * @author zhanghuan
 * @version 1.0
 * @since 2022/3/3 20:04
 * @deprecated See {@link CacheWrapper}. Reserved for future use. Not currently utilized in the engine.
 */
@Deprecated
@Component
@ConditionalOnProperty(name = "feature.cache.enabled", havingValue = "true", matchIfMissing = false)
public class DefaultCacheImpl implements CacheWrapper {

    private final HashSet<String> hashSet = new HashSet<>();

    @Override
    public void set(String key) {
        hashSet.add(key);
    }

    @Override
    public Boolean get(String key) {
        return hashSet.contains(key);
    }

    @Override
    public void clearAll() {
        hashSet.clear();
    }
}
