// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.talend.daikon.sandbox.SandboxControl.CLASSLOADER_REUSABLE;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.daikon.sandbox.properties.ClassLoaderIsolatedSystemProperties;
import org.talend.daikon.token.TokenGenerator;

public class SandboxedInstanceTest {

    private static final String TEST_CLASS_NAME = "org.talend.test.MyClass1";

    private Properties previous;

    @BeforeEach
    public void setUp() {
        previous = System.getProperties();
        assertFalse(System.getProperties() instanceof ClassLoaderIsolatedSystemProperties);
        System.setProperties(ClassLoaderIsolatedSystemProperties.getInstance());
    }

    @AfterEach
    public void tearDown() {
        System.setProperties(previous);
    }

    /**
     * Test method for {@link org.talend.daikon.sandbox.SandboxedInstance#close()}.
     *
     */
    @Test
    public void testClose() {
        URLClassLoader urlClassLoader = URLClassLoader
                .newInstance(Collections.singleton(this.getClass().getResource("zeLib-0.0.1.jar")).toArray(new URL[1]));
        SandboxedInstance sandboxedInstance = new SandboxedInstance(TEST_CLASS_NAME, false, urlClassLoader, CLASSLOADER_REUSABLE);
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        Object instance = sandboxedInstance.getInstance();
        assertTrue(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(instance.getClass().getClassLoader()));
        sandboxedInstance.close();
        assertEquals(previousClassLoader, Thread.currentThread().getContextClassLoader());
        assertTrue(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(instance.getClass().getClassLoader()));
    }

    public void testAutoClose() {
        URLClassLoader urlClassLoader = URLClassLoader
                .newInstance(Collections.singleton(this.getClass().getResource("zeLib-0.0.1.jar")).toArray(new URL[1]));
        ClassLoader sandboxedClassLoader;
        try (SandboxedInstance sandboxedInstance = new SandboxedInstance(TEST_CLASS_NAME, true, urlClassLoader,
                !CLASSLOADER_REUSABLE)) {
            Object instance = sandboxedInstance.getInstance();
            sandboxedClassLoader = instance.getClass().getClassLoader();
            assertTrue(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(sandboxedClassLoader));
        }
        assertFalse(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(sandboxedClassLoader));
    }

    /**
     * Test method for {@link org.talend.daikon.sandbox.SandboxedInstance#getInstance()}.
     *
     */
    @Test
    public void testGetInstanceWithDefault() {
        URLClassLoader urlClassLoader = URLClassLoader
                .newInstance(Collections.singleton(this.getClass().getResource("zeLib-0.0.1.jar")).toArray(new URL[1]));
        try (SandboxedInstance sandboxedInstance = new SandboxedInstance(TEST_CLASS_NAME, true, urlClassLoader,
                !CLASSLOADER_REUSABLE)) {
            assertNull(sandboxedInstance.isolatedThread);
            assertNull(sandboxedInstance.previousContextClassLoader);
            assertTrue(sandboxedInstance.useCurrentJvmProperties);
            ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
            assertNotEquals(urlClassLoader, previousClassLoader);
            assertFalse(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(urlClassLoader));
            Object instance = sandboxedInstance.getInstance();
            assertNotNull(instance);
            assertTrue(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(urlClassLoader));
            assertEquals(Thread.currentThread(), sandboxedInstance.isolatedThread);
            assertEquals(previousClassLoader, sandboxedInstance.previousContextClassLoader);
            assertEquals(urlClassLoader, Thread.currentThread().getContextClassLoader());
            assertEquals(previous, System.getProperties());
        }
    }

    @Test
    public void getInstanceWithStandardProperties() {
        assertThrows(IllegalArgumentException.class, () -> {
            TokenGenerator.generateMachineToken(null);
            URLClassLoader urlClassLoader = URLClassLoader
                    .newInstance(Collections.singleton(this.getClass().getResource("zeLib-0.0.1.jar")).toArray(new URL[1]));
            SandboxedInstance sandboxedInstance = new SandboxedInstance(TEST_CLASS_NAME, false, urlClassLoader,
                    !CLASSLOADER_REUSABLE);
            try {
                assertNull(sandboxedInstance.isolatedThread);
                assertNull(sandboxedInstance.previousContextClassLoader);
                assertFalse(sandboxedInstance.useCurrentJvmProperties);
                ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
                assertNotEquals(urlClassLoader, previousClassLoader);
                assertFalse(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(urlClassLoader));
                Object instance = sandboxedInstance.getInstance();
                assertTrue(ClassLoaderIsolatedSystemProperties.getInstance().isIsolated(urlClassLoader));
                assertNotNull(instance);
                assertEquals(Thread.currentThread(), sandboxedInstance.isolatedThread);
                assertEquals(previousClassLoader, sandboxedInstance.previousContextClassLoader);
                assertEquals(urlClassLoader, Thread.currentThread().getContextClassLoader());
                assertNotEquals(previous, System.getProperties());
            } finally {
                // check that an exception is thrown once the close is called.
                sandboxedInstance.close();
            }
            sandboxedInstance.getInstance();
        });
    }

    public Object createNewInstanceWithNewClassLoader()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        URLClassLoader urlClassLoader = URLClassLoader
                .newInstance(Collections.singleton(this.getClass().getResource("zeLib-0.0.1.jar")).toArray(new URL[1]));
        Class<?> testClass = urlClassLoader.loadClass(TEST_CLASS_NAME);
        return testClass.newInstance();
    }

}
