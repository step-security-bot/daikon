// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.runtime.RuntimeContext;
import org.talend.daikon.properties.service.Repository;

import java.lang.reflect.Method;

/**
 * Unit-tests for {@link PropertiesDynamicMethodHelper}
 * <p>
 * Client code should call <code>isCall*()<code> methods of {@link Form} and {@link Widget} classes to discover whether it is
 * allowed to call callbacks.
 * There are 4 cases for each callback:
 * <ol>
 * <li>
 * Properties implementation has no callbacks. isCall() should return false. Exception will be thrown if client call callback
 * </li>
 * <li>
 * Properties implementation has callback without RuntimeContext parameter (old callback). This is current implementation.
 * It should work as before. isCall() should return true. Old callback may be called.
 * </li>
 * <li>
 * Properties implementation has callback with RuntimeContext parameter (new callback), but has no old callback.
 * It breaks current implementation as product may call service without passing RuntimeContext. So, it should be prohibited.
 * isCall() should return false.
 * Exception should be thrown, when client calls the callback.
 * </li>
 * <li>
 * Properties has both callbacks. This is correct implementation to support new feature. isCall() should return true. Product may
 * pass RuntimeContext argument.
 * Then new callback will be called. If Product doesn't pass RuntimeContext argument, then old callback should be called.
 * </li>
 * </ol>
 */
public class PropertiesDynamicMethodHelperTest {

    static class PropertiesWithoutCallbacks extends PropertiesImpl {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("rawtypes")
        public ReferenceProperties ref = new ReferenceProperties<>("ref", "AnyDefinitionName");

        public PropertiesWithoutCallbacks() {
            super("test");
        }

        /**
         * method with package visibility which won't be found
         */
        void beforeSomeProperty() {
            // This is test method. Implementation is empty intentionally
        }

        /**
         * method with private visibility which won't be found
         */
        @SuppressWarnings("unused")
        private void validateAnotherProperty() {
            // This is test method. Implementation is empty intentionally
        }

    }

    static class PropertiesWithOldCallbacks extends PropertiesImpl {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("rawtypes")
        public ReferenceProperties ref = new ReferenceProperties<>("ref", "AnyDefinitionName");

        public PropertiesWithOldCallbacks() {
            super("test");
        }

        public void beforeProperty() {
            // This is test method. Implementation is empty intentionally
        }

        public void validateProperty() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterProperty() {
            // This is test method. Implementation is empty intentionally
        }

        public void beforeFormPresentMain() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormBackMain() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormNextMain() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterRef() {
            // This is test method. Implementation is empty intentionally
        }

        @SuppressWarnings("rawtypes")
        public void afterFormFinishMain(Repository repository) {
            // This is test method. Implementation is empty intentionally
        }

    }

    static class PropertiesWithRuntimeContextCallbacks extends PropertiesImpl {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("rawtypes")
        public ReferenceProperties ref = new ReferenceProperties<>("ref", "AnyDefinitionName");

        public PropertiesWithRuntimeContextCallbacks() {
            super("test");
        }

        public void beforeProperty(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void validateProperty(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterProperty(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void beforeFormPresentMain(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormBackMain(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormNextMain(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        @SuppressWarnings("rawtypes")
        public void afterFormFinishMain(Repository repository, RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterRef(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }
    }

    static class PropertiesWithBothCallbacks extends PropertiesImpl {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("rawtypes")
        public ReferenceProperties ref = new ReferenceProperties<>("ref", "AnyDefinitionName");

        public PropertiesWithBothCallbacks() {
            super("test");
        }

        public void beforeProperty() {
            // This is test method. Implementation is empty intentionally
        }

        public void beforeProperty(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void validateProperty() {
            // This is test method. Implementation is empty intentionally
        }

        public void validateProperty(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterProperty() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterProperty(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void beforeFormPresentMain() {
            // This is test method. Implementation is empty intentionally
        }

        public void beforeFormPresentMain(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormBackMain() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormBackMain(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormNextMain() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterFormNextMain(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        @SuppressWarnings("rawtypes")
        public void afterFormFinishMain(Repository repository) {
            // This is test method. Implementation is empty intentionally
        }

        @SuppressWarnings("rawtypes")
        public void afterFormFinishMain(Repository repository, RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }

        public void afterRef() {
            // This is test method. Implementation is empty intentionally
        }

        public void afterRef(RuntimeContext context) {
            // This is test method. Implementation is empty intentionally
        }
    }

    @Test
    public void testFindMethodNullPropertyName() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithBothCallbacks props = new PropertiesWithBothCallbacks();
            PropertiesDynamicMethodHelper.findMethod(props, Properties.METHOD_AFTER, null, true);
        });
        assertTrue(thrown.getMessage()
                .contains("The ComponentService was used to access a property with a null(or empty) property name. Type:"));
    }

    @Test
    public void testFindMethodNullObject() {
        NullPointerException thrown = assertThrows(NullPointerException.class, () -> {
            PropertiesDynamicMethodHelper.findMethod(null, Properties.METHOD_AFTER, "property", true);
        });
        assertEquals("Instance whose method is being searched for should not be null", thrown.getMessage());
    }

    @Test
    public void testFindMethodUnknownTypeRequired() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithBothCallbacks props = new PropertiesWithBothCallbacks();
            PropertiesDynamicMethodHelper.findMethod(props, "unknownTriggerType", "property", true);
        });
        assertEquals("Method: unknownTriggerTypeProperty not found", thrown.getMessage());
    }

    @Test
    public void testFindMethodUnknownTypeNotRequired() {
        PropertiesWithBothCallbacks props = new PropertiesWithBothCallbacks();
        Method method = PropertiesDynamicMethodHelper.findMethod(props, "unknownTriggerType", "property", false);
        assertNull(method);
    }

    /**
     * Asserts public methods are found
     */
    @Test
    public void testFindMethodPublic() {
        String expectedMethodDefinition = "public void org.talend.daikon.properties.PropertiesDynamicMethodHelperTest$PropertiesWithBothCallbacks.afterProperty()";

        PropertiesWithBothCallbacks props = new PropertiesWithBothCallbacks();
        Method method = PropertiesDynamicMethodHelper.findMethod(props, Properties.METHOD_AFTER, "property", false);
        assertEquals(expectedMethodDefinition, method.toString());
    }

    /**
     * Asserts public method with parameter is found
     */
    @Test
    public void testFindMethodPublicWithParams() {
        String expectedMethodDefinition = "public void org.talend.daikon.properties.PropertiesDynamicMethodHelperTest$PropertiesWithBothCallbacks.afterProperty("
                + "org.talend.daikon.properties.runtime.RuntimeContext)";

        PropertiesWithBothCallbacks props = new PropertiesWithBothCallbacks();
        Method method = PropertiesDynamicMethodHelper.findMethod(props, Properties.METHOD_AFTER, "property", false,
                RuntimeContext.class);
        assertEquals(expectedMethodDefinition, method.toString());
    }

    /**
     * Asserts that package visible methods are not found
     */
    @Test
    public void testFindMethodPackage() {
        PropertiesWithoutCallbacks props = new PropertiesWithoutCallbacks();
        Method method = PropertiesDynamicMethodHelper.findMethod(props, Properties.METHOD_BEFORE, "someProperty", false);
        assertNull(method);
    }

    /**
     * Asserts that private visible methods are not found
     */
    @Test
    public void testFindMethodPrivate() {
        PropertiesWithoutCallbacks props = new PropertiesWithoutCallbacks();
        Method method = PropertiesDynamicMethodHelper.findMethod(props, Properties.METHOD_VALIDATE, "anotherProperty", false);
        assertNull(method);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     */
    @Test
    public void testAfterFormBackNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.afterFormBack(withoutCallbacks, Form.MAIN);
        });
        assertEquals("Method: afterFormBackMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterFormBackNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.afterFormBack(withNewCallbacks, Form.MAIN);
        });
        assertEquals("Method: afterFormBackMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterFormBackOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.afterFormBack(withOldCallbacks, Form.MAIN);
        verify(withOldCallbacks).afterFormBackMain();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testAfterFormBackBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.afterFormBack(withBothCallbacks, Form.MAIN);
        verify(withBothCallbacks).afterFormBackMain();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterFormBackRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterFormBack(withoutCallbacks, Form.MAIN, context);
        });
        assertEquals("Method: afterFormBackMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterFormBackRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterFormBack(withNewCallbacks, Form.MAIN, context);
        });
        assertEquals("Method: afterFormBackMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterFormBackRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterFormBack(withOldCallbacks, Form.MAIN, context);
        verify(withOldCallbacks).afterFormBackMain();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testAfterFormBackRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterFormBack(withBothCallbacks, Form.MAIN, context);
        verify(withBothCallbacks).afterFormBackMain(context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterFormFinishkNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            Repository repository = mock(Repository.class);
            PropertiesDynamicMethodHelper.afterFormFinish(withoutCallbacks, Form.MAIN, repository);
        });
        assertEquals("Method: afterFormFinishMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterFormFinishNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            Repository repository = mock(Repository.class);
            PropertiesDynamicMethodHelper.afterFormFinish(withNewCallbacks, Form.MAIN, repository);
        });
        assertEquals("Method: afterFormFinishMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterFormFinishOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        Repository repository = mock(Repository.class);
        PropertiesDynamicMethodHelper.afterFormFinish(withOldCallbacks, Form.MAIN, repository);
        verify(withOldCallbacks).afterFormFinishMain(repository);
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testAfterFormFinishBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        Repository repository = mock(Repository.class);
        PropertiesDynamicMethodHelper.afterFormFinish(withBothCallbacks, Form.MAIN, repository);
        verify(withBothCallbacks).afterFormFinishMain(repository);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterFormFinishRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            Repository repository = mock(Repository.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterFormFinish(withoutCallbacks, Form.MAIN, repository, context);
        });
        assertEquals("Method: afterFormFinishMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterFormFinishRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            Repository repository = mock(Repository.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterFormFinish(withNewCallbacks, Form.MAIN, repository, context);
        });
        assertEquals("Method: afterFormFinishMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterFormFinishRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        Repository repository = mock(Repository.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterFormFinish(withOldCallbacks, Form.MAIN, repository, context);
        verify(withOldCallbacks).afterFormFinishMain(repository);
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testAfterFormFinishRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        Repository repository = mock(Repository.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterFormFinish(withBothCallbacks, Form.MAIN, repository, context);
        verify(withBothCallbacks).afterFormFinishMain(repository, context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterFormNextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.afterFormNext(withoutCallbacks, Form.MAIN);
        });
        assertEquals("Method: afterFormNextMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterFormNextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.afterFormNext(withNewCallbacks, Form.MAIN);
        });
        assertEquals("Method: afterFormNextMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterFormNextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.afterFormNext(withOldCallbacks, Form.MAIN);
        verify(withOldCallbacks).afterFormNextMain();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testAfterFormNextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.afterFormNext(withBothCallbacks, Form.MAIN);
        verify(withBothCallbacks).afterFormNextMain();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterFormNextRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterFormNext(withoutCallbacks, Form.MAIN, context);
        });
        assertEquals("Method: afterFormNextMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterFormNextRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterFormNext(withNewCallbacks, Form.MAIN, context);
        });
        assertEquals("Method: afterFormNextMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterFormNextRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterFormNext(withOldCallbacks, Form.MAIN, context);
        verify(withOldCallbacks).afterFormNextMain();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testAfterFormNextRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterFormNext(withBothCallbacks, Form.MAIN, context);
        verify(withBothCallbacks).afterFormNextMain(context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterPropertyNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.afterProperty(withoutCallbacks, "property");
        });
        assertEquals("Method: afterProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterPropertyNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.afterProperty(withNewCallbacks, "property");
        });
        assertEquals("Method: afterProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterPropertyOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.afterProperty(withOldCallbacks, "property");
        verify(withOldCallbacks).afterProperty();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testAfterPropertyBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.afterProperty(withBothCallbacks, "property");
        verify(withBothCallbacks).afterProperty();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testAfterPropertyRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterProperty(withoutCallbacks, "property", context);
        });
        assertEquals("Method: afterProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testAfterPropertyRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.afterProperty(withNewCallbacks, "property", context);
        });
        assertEquals("Method: afterProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterPropertyRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterProperty(withOldCallbacks, "property", context);
        verify(withOldCallbacks).afterProperty();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testAfterPropertyRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterProperty(withBothCallbacks, "property", context);
        verify(withBothCallbacks).afterProperty(context);
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterReferenceOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = spy(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.afterReference(withOldCallbacks, withOldCallbacks.ref);
        verify(withOldCallbacks).afterRef();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testAfterReferenceBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = spy(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.afterReference(withBothCallbacks, withBothCallbacks.ref);
        verify(withBothCallbacks).afterRef();
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testAfterReferenceRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = spy(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterReference(withOldCallbacks, withOldCallbacks.ref, context);
        verify(withOldCallbacks).afterRef();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testAfterReferenceRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = spy(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.afterReference(withBothCallbacks, withBothCallbacks.ref, context);
        verify(withBothCallbacks).afterRef(context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testBeforeFormPresentNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.beforeFormPresent(withoutCallbacks, Form.MAIN);
        });
        assertEquals("Method: beforeFormPresentMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testBeforeFormPresentNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.beforeFormPresent(withNewCallbacks, Form.MAIN);
        });
        assertEquals("Method: beforeFormPresentMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testBeforeFormPresentOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.beforeFormPresent(withOldCallbacks, Form.MAIN);
        verify(withOldCallbacks).beforeFormPresentMain();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testBeforeFormPresentBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.beforeFormPresent(withBothCallbacks, Form.MAIN);
        verify(withBothCallbacks).beforeFormPresentMain();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testBeforeFormPresentRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.beforeFormPresent(withoutCallbacks, Form.MAIN, context);
        });
        assertEquals("Method: beforeFormPresentMain not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testBeforeFormPresentRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.beforeFormPresent(withNewCallbacks, Form.MAIN, context);
        });
        assertEquals("Method: beforeFormPresentMain not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testBeforeFormPresentRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.beforeFormPresent(withOldCallbacks, Form.MAIN, context);
        verify(withOldCallbacks).beforeFormPresentMain();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testBeforeFormPresentRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.beforeFormPresent(withBothCallbacks, Form.MAIN, context);
        verify(withBothCallbacks).beforeFormPresentMain(context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.beforePropertyActivate(withoutCallbacks, "property");
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.beforePropertyActivate(withNewCallbacks, "property");
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.beforePropertyActivate(withOldCallbacks, "property");
        verify(withOldCallbacks).beforeProperty();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.beforePropertyActivate(withBothCallbacks, "property");
        verify(withBothCallbacks).beforeProperty();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.beforePropertyActivate(withoutCallbacks, "property", context);
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.beforePropertyActivate(withNewCallbacks, "property", context);
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.beforePropertyActivate(withOldCallbacks, "property", context);
        verify(withOldCallbacks).beforeProperty();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testBeforePropertyActivateRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.beforePropertyActivate(withBothCallbacks, "property", context);
        verify(withBothCallbacks).beforeProperty(context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.beforePropertyPresent(withoutCallbacks, "property");
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.beforePropertyPresent(withNewCallbacks, "property");
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.beforePropertyPresent(withOldCallbacks, "property");
        verify(withOldCallbacks).beforeProperty();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.beforePropertyPresent(withBothCallbacks, "property");
        verify(withBothCallbacks).beforeProperty();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.beforePropertyPresent(withoutCallbacks, "property", context);
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.beforePropertyPresent(withNewCallbacks, "property", context);
        });
        assertEquals("Method: beforeProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.beforePropertyPresent(withOldCallbacks, "property", context);
        verify(withOldCallbacks).beforeProperty();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testBeforePropertyPresentRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.beforePropertyPresent(withBothCallbacks, "property", context);
        verify(withBothCallbacks).beforeProperty(context);
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testValidatePropertyNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            PropertiesDynamicMethodHelper.validateProperty(withoutCallbacks, "property");
        });
        assertEquals("Method: validateProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testValidatePropertyNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            PropertiesDynamicMethodHelper.validateProperty(withNewCallbacks, "property");
        });
        assertEquals("Method: validateProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testValidatePropertyOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        PropertiesDynamicMethodHelper.validateProperty(withOldCallbacks, "property");
        verify(withOldCallbacks).validateProperty();
    }

    /**
     * Asserts that callback without parameters is called even if {@link Properties} class has both kinds of callbacks
     *
     * @
     */
    @Test
    public void testValidatePropertyBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        PropertiesDynamicMethodHelper.validateProperty(withBothCallbacks, "property");
        verify(withBothCallbacks).validateProperty();
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has no triggers at all
     *
     * @
     */
    @Test
    public void testValidatePropertyRuntimeContextNoCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithoutCallbacks withoutCallbacks = mock(PropertiesWithoutCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.validateProperty(withoutCallbacks, "property", context);
        });
        assertEquals("Method: validateProperty not found", thrown.getMessage());
    }

    /**
     * Asserts that method throws {@link IllegalArgumentException}, when {@link Properties} class has callback with
     * {@link RuntimeContext} parameter, but has no callback without parameters
     *
     * @
     */
    @Test
    public void testValidatePropertyRuntimeContextNewCallback() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            PropertiesWithRuntimeContextCallbacks withNewCallbacks = mock(PropertiesWithRuntimeContextCallbacks.class);
            RuntimeContext context = mock(RuntimeContext.class);
            PropertiesDynamicMethodHelper.validateProperty(withNewCallbacks, "property", context);
        });
        assertEquals("Method: validateProperty not found", thrown.getMessage());

    }

    /**
     * Asserts that callback without parameters is called, when it is the only callback present in {@link Properties} class
     *
     * @
     */
    @Test
    public void testValidatePropertyRuntimeContextOldCallback() throws Throwable {
        PropertiesWithOldCallbacks withOldCallbacks = mock(PropertiesWithOldCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.validateProperty(withOldCallbacks, "property", context);
        verify(withOldCallbacks).validateProperty();
    }

    /**
     * Asserts that callback with {@link RuntimeContext} parameter is called, when {@link Properties} class has both kinds of
     * callbacks
     * and product passes {@link RuntimeContext} argument to the method
     *
     * @
     */
    @Test
    public void testValidatePropertyRuntimeContextBothCallbacks() throws Throwable {
        PropertiesWithBothCallbacks withBothCallbacks = mock(PropertiesWithBothCallbacks.class);
        RuntimeContext context = mock(RuntimeContext.class);
        PropertiesDynamicMethodHelper.validateProperty(withBothCallbacks, "property", context);
        verify(withBothCallbacks).validateProperty(context);
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setFormLayoutMethods(Properties, String, Form)} sets {@link Form}'s callback
     * flags to <code>false</code>,
     * when {@link Properties} without callbacks is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Form} instance
     * When {@link Properties} class has no callbacks, Product shouldn't try to call them
     * Product should use isCall*() methods to discover whether callbacks are present
     */
    @Test
    public void testSetFormLayoutMethodsNoCallback() {
        PropertiesWithoutCallbacks withoutCallbacks = new PropertiesWithoutCallbacks();
        Form form = new Form(withoutCallbacks, Form.MAIN);
        PropertiesDynamicMethodHelper.setFormLayoutMethods(withoutCallbacks, Form.MAIN, form);
        assertFalse(form.isCallBeforeFormPresent());
        assertFalse(form.isCallAfterFormBack());
        assertFalse(form.isCallAfterFormNext());
        assertFalse(form.isCallAfterFormFinish());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setFormLayoutMethods(Properties, String, Form)} sets {@link Form}'s callback
     * flags to <code>true</code>,
     * when {@link Properties} with old callbacks is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Form} instance
     * When {@link Properties} class has callbacks without parameters, Product may call them
     * Product should use isCall*() methods to discover whether callbacks are present
     */
    @Test
    public void testSetFormLayoutMethodsOldCallback() {
        PropertiesWithOldCallbacks withOldCallbacks = new PropertiesWithOldCallbacks();
        Form form = new Form(withOldCallbacks, Form.MAIN);
        PropertiesDynamicMethodHelper.setFormLayoutMethods(withOldCallbacks, Form.MAIN, form);
        assertTrue(form.isCallBeforeFormPresent());
        assertTrue(form.isCallAfterFormBack());
        assertTrue(form.isCallAfterFormNext());
        assertTrue(form.isCallAfterFormFinish());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setFormLayoutMethods(Properties, String, Form)} sets {@link Form}'s callback
     * flags to <code>false</code>,
     * when {@link Properties} with new callbacks only is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Form} instance
     * When {@link Properties} class has only callbacks with {@link RuntimeContext} parameter, but has no callbacks without
     * parameters, Product shouldn't try to call them
     * Product should use isCall*() methods to discover whether callbacks without parameters are present
     */
    @Test
    public void testSetFormLayoutMethodsNewCallback() {
        PropertiesWithRuntimeContextCallbacks withRuntimeContextCallbacks = new PropertiesWithRuntimeContextCallbacks();
        Form form = new Form(withRuntimeContextCallbacks, Form.MAIN);
        PropertiesDynamicMethodHelper.setFormLayoutMethods(withRuntimeContextCallbacks, Form.MAIN, form);
        assertFalse(form.isCallBeforeFormPresent());
        assertFalse(form.isCallAfterFormBack());
        assertFalse(form.isCallAfterFormNext());
        assertFalse(form.isCallAfterFormFinish());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setFormLayoutMethods(Properties, String, Form)} sets {@link Form}'s callback
     * flags to <code>true</code>,
     * when {@link Properties} with both callbacks is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Form} instance
     * When {@link Properties} class has both callbacks, Product may call them
     * Product should use isCall*() methods to discover whether callbacks without parameters are present
     */
    @Test
    public void testSetFormLayoutMethodsBothCallbacks() {
        PropertiesWithBothCallbacks withBothCallbacks = new PropertiesWithBothCallbacks();
        Form form = new Form(withBothCallbacks, Form.MAIN);
        PropertiesDynamicMethodHelper.setFormLayoutMethods(withBothCallbacks, Form.MAIN, form);
        assertTrue(form.isCallBeforeFormPresent());
        assertTrue(form.isCallAfterFormBack());
        assertTrue(form.isCallAfterFormNext());
        assertTrue(form.isCallAfterFormFinish());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setWidgetLayoutMethods(Properties, String, Widget)} sets {@link Widget}'s
     * callbacks flags to <code>false</code>,
     * when {@link Properties} without callbacks is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Widget} instance
     * When {@link Properties} class has no callbacks, Product shouldn't try to call them
     * Product should use isCall*() methods to discover whether callbacks are present
     */
    @Test
    public void testSetWidgetLayoutMethodsNoCallback() {
        PropertiesWithoutCallbacks withoutCallbacks = new PropertiesWithoutCallbacks();
        Widget widget = new Widget(withoutCallbacks);
        PropertiesDynamicMethodHelper.setWidgetLayoutMethods(withoutCallbacks, "property", widget);
        assertFalse(widget.isCallBeforePresent());
        assertFalse(widget.isCallBeforeActivate());
        assertFalse(widget.isCallValidate());
        assertFalse(widget.isCallAfter());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setWidgetLayoutMethods(Properties, String, Widget)} sets {@link Widget}'s
     * callbacks flags to <code>true</code>,
     * when {@link Properties} with callbacks only is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Widget} instance
     * When {@link Properties} class has old callbacks, Product may call them
     * Product should use isCall*() methods to discover whether callbacks are present
     */
    @Test
    public void testSetWidgetLayoutMethodsOldCallback() {
        PropertiesWithOldCallbacks withOldCallbacks = new PropertiesWithOldCallbacks();
        Widget widget = new Widget(withOldCallbacks);
        PropertiesDynamicMethodHelper.setWidgetLayoutMethods(withOldCallbacks, "property", widget);
        assertTrue(widget.isCallBeforePresent());
        assertFalse(widget.isCallBeforeActivate());
        assertTrue(widget.isCallValidate());
        assertTrue(widget.isCallAfter());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setWidgetLayoutMethods(Properties, String, Widget)} sets {@link Widget}'s
     * callbacks flags to <code>false</code>,
     * when {@link Properties} without new callbacks only is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Widget} instance
     * When {@link Properties} class has new callbacks only, Product shouldn't try to call them
     * Product should use isCall*() methods to discover whether callbacks are present
     */
    @Test
    public void testSetWidgetLayoutMethodsNewCallback() {
        PropertiesWithRuntimeContextCallbacks withRuntimeContextCallbacks = new PropertiesWithRuntimeContextCallbacks();
        Widget widget = new Widget(withRuntimeContextCallbacks);
        PropertiesDynamicMethodHelper.setWidgetLayoutMethods(withRuntimeContextCallbacks, "property", widget);
        assertFalse(widget.isCallBeforePresent());
        assertFalse(widget.isCallBeforeActivate());
        assertFalse(widget.isCallValidate());
        assertFalse(widget.isCallAfter());
    }

    /**
     * Asserts {@link PropertiesDynamicMethodHelper#setWidgetLayoutMethods(Properties, String, Widget)} sets {@link Widget}'s
     * callbacks flags to <code>true</code>,
     * when {@link Properties} with both callbacks is passed
     * Callbacks flags are checked by calling isCall*() methods of {@link Widget} instance
     * When {@link Properties} class has both callbacks, Product may call them
     * Product should use isCall*() methods to discover whether callbacks are present
     */
    @Test
    public void testSetWidgetLayoutMethodsBothCallbacks() {
        PropertiesWithBothCallbacks withBothCallbacks = new PropertiesWithBothCallbacks();
        Widget widget = new Widget(withBothCallbacks);
        PropertiesDynamicMethodHelper.setWidgetLayoutMethods(withBothCallbacks, "property", widget);
        assertTrue(widget.isCallBeforePresent());
        assertFalse(widget.isCallBeforeActivate());
        assertTrue(widget.isCallValidate());
        assertTrue(widget.isCallAfter());
    }

}
