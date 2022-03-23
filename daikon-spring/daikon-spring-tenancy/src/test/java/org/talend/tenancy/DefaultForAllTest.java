package org.talend.tenancy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultForAllTest {

    @Test
    public void shouldExecute() throws Exception {
        // Given
        ForAll forAll = new DefaultForAll();

        // When
        AtomicBoolean executed = new AtomicBoolean(false);
        forAll.execute(() -> executed.set(true));

        // Then
        assertTrue(executed.get());
    }

    @Test
    public void shouldExecuteWithMatchCondition() throws Exception {
        // Given
        ForAll forAll = new DefaultForAll();

        // When
        AtomicBoolean executed = new AtomicBoolean(false);
        forAll.execute(forAll.condition().operational(new Object()), () -> executed.set(true));

        // Then
        assertTrue(executed.get());
    }

    @Test
    public void shouldNotExecuteWithNoMatchCondition() throws Exception {
        // Given
        ForAll forAll = new DefaultForAll();

        // When
        AtomicBoolean executed = new AtomicBoolean(false);
        forAll.execute(() -> false, () -> executed.set(true));

        // Then
        assertFalse(executed.get());
    }

}