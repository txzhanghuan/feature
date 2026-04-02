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

package com.github.zh.engine.enums;

/**
 * Enumeration of feature computation states.
 * <p>
 * Represents the lifecycle states of a {@link com.github.zh.engine.co.FeatureEntity}
 * during computation. The state transitions follow this flow:
 * <pre>
 * INIT → PROCESSING → SUCCESS
 *                 ↖
 *                   → FAILED
 * </pre>
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/20
 * Time: 11:49 AM
 * @see com.github.zh.engine.co.FeatureEntity
 */
public enum FeatureStates {

    /**
     * Initial state before computation begins.
     * <p>
     * All features start in this state and transition to PROCESSING when computation starts.
     * </p>
     */
    INIT,
    /**
     * Computation is currently in progress.
     * <p>
     * The feature is actively executing its computation logic.
     * </p>
     */
    PROCESSING,
    /**
     * Computation completed successfully.
     * <p>
     * The feature's result is now available and can be used by dependent features.
     * </p>
     */
    SUCCESS,
    /**
     * Computation failed with an error.
     * <p>
     * The feature encountered an exception during execution. All dependent features
     * will also fail as a result.
     * </p>
     */
    FAILED;

    /**
     * Checks if this state is a terminal (end) state.
     * <p>
     * Terminal states indicate that the feature has completed processing,
     * either successfully or with failure.
     * </p>
     *
     * @return true if this state is SUCCESS or FAILED; false otherwise
     */
    public boolean isEndStates() {
        return this.equals(FAILED) || this.equals(SUCCESS);
    }

}
