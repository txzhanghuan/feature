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

import com.github.zh.engine.enums.FeatureStates;
import com.github.zh.engine.exception.CalculateException;
import com.github.zh.engine.exception.FeatureCreationException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for exceptions and enums.
 * These tests do not require Spring context.
 */
public class ExceptionTest {

    // ==================== CalculateException 测试 ====================

    @Test
    public void testCalculateExceptionWithMessage() {
        String message = "Test calculation error";
        CalculateException exception = new CalculateException(message);

        assertEquals("Message should match", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testCalculateExceptionWithThrowable() {
        RuntimeException cause = new RuntimeException("Root cause");
        CalculateException exception = new CalculateException(cause);

        assertNotNull("Message should not be null", exception.getMessage());
        assertTrue("Message should contain cause info",
                exception.getMessage().contains("Root cause"));
        assertEquals("Cause should be set", cause, exception.getCause());
    }

    @Test
    public void testCalculateExceptionWithMessageAndCause() {
        String message = "Calculation failed";
        RuntimeException cause = new RuntimeException("Root cause");
        CalculateException exception = new CalculateException(message, cause);

        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should be set", cause, exception.getCause());

        // Verify exception chain is intact
        assertNotNull("Exception chain should be intact", exception.getCause());
        assertEquals("Cause message should match", "Root cause", exception.getCause().getMessage());
    }

    @Test
    public void testCalculateExceptionIsRuntimeException() {
        CalculateException exception = new CalculateException("Test");

        assertTrue("CalculateException should be a RuntimeException",
                exception instanceof RuntimeException);
    }

    @Test
    public void testCalculateExceptionWithNestedCause() {
        // Create a chain of exceptions
        Exception rootCause = new IllegalStateException("Root");
        Exception middleCause = new RuntimeException("Middle", rootCause);
        CalculateException exception = new CalculateException("Top level", middleCause);

        // Verify the entire chain
        assertEquals("Direct cause should be middle", middleCause, exception.getCause());
        assertEquals("Root cause should be accessible", rootCause, exception.getCause().getCause());
    }

    // ==================== FeatureCreationException 测试 ====================

    @Test
    public void testFeatureCreationExceptionWithMessage() {
        String message = "Failed to create feature bean";
        FeatureCreationException exception = new FeatureCreationException(message);

        assertEquals("Message should match", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testFeatureCreationExceptionWithMessageAndCause() {
        String message = "Feature creation failed";
        Exception cause = new IllegalArgumentException("Invalid method signature");
        FeatureCreationException exception = new FeatureCreationException(message, cause);

        // BeansException includes nested exception info in getMessage()
        assertTrue("Message should contain original message", exception.getMessage().contains(message));
        assertEquals("Cause should be set", cause, exception.getCause());
    }

    @Test
    public void testFeatureCreationExceptionIsBeansException() {
        FeatureCreationException exception = new FeatureCreationException("Test");

        assertTrue("FeatureCreationException should be a BeansException",
                exception instanceof org.springframework.beans.BeansException);
    }

    // ==================== FeatureStates 测试 ====================

    @Test
    public void testFeatureStatesIsEndStates() {
        // SUCCESS is an end state
        assertTrue("SUCCESS should be an end state", FeatureStates.SUCCESS.isEndStates());

        // FAILED is an end state
        assertTrue("FAILED should be an end state", FeatureStates.FAILED.isEndStates());

        // INIT is not an end state
        assertFalse("INIT should not be an end state", FeatureStates.INIT.isEndStates());

        // PROCESSING is not an end state
        assertFalse("PROCESSING should not be an end state", FeatureStates.PROCESSING.isEndStates());
    }

    @Test
    public void testFeatureStatesValues() {
        // Verify all expected states exist
        FeatureStates[] states = FeatureStates.values();
        assertEquals("Should have 4 states", 4, states.length);

        // Verify each state by name
        assertNotNull("INIT should exist", FeatureStates.valueOf("INIT"));
        assertNotNull("PROCESSING should exist", FeatureStates.valueOf("PROCESSING"));
        assertNotNull("SUCCESS should exist", FeatureStates.valueOf("SUCCESS"));
        assertNotNull("FAILED should exist", FeatureStates.valueOf("FAILED"));
    }

    @Test
    public void testFeatureStatesOrdinal() {
        // Verify ordinal order: INIT, PROCESSING, SUCCESS, FAILED
        assertEquals("INIT ordinal should be 0", 0, FeatureStates.INIT.ordinal());
        assertEquals("PROCESSING ordinal should be 1", 1, FeatureStates.PROCESSING.ordinal());
        assertEquals("SUCCESS ordinal should be 2", 2, FeatureStates.SUCCESS.ordinal());
        assertEquals("FAILED ordinal should be 3", 3, FeatureStates.FAILED.ordinal());
    }

    @Test
    public void testFeatureStatesEquality() {
        // Test equality
        assertEquals("Same state should be equal", FeatureStates.INIT, FeatureStates.INIT);
        assertNotEquals("Different states should not be equal", FeatureStates.INIT, FeatureStates.SUCCESS);
    }

    @Test
    public void testFeatureStatesEndStatesConsistency() {
        // Only terminal states should return true for isEndStates
        int endStateCount = 0;
        for (FeatureStates state : FeatureStates.values()) {
            if (state.isEndStates()) {
                endStateCount++;
            }
        }
        assertEquals("Should have exactly 2 end states", 2, endStateCount);
    }
}
