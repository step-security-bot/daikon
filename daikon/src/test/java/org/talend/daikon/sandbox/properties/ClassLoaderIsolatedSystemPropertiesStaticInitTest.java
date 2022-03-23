package org.talend.daikon.sandbox.properties;
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

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.talend.daikon.runtime.RuntimeInfo;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ClassLoaderIsolatedSystemPropertiesStaticInitTest {

    public static final int TEST_TIMES = 5;

    private Properties previous;

    private class TestRuntime implements RuntimeInfo {

        private String name;

        public TestRuntime(String name) {
            this.name = name;
        }

        @Override
        public String getRuntimeClassName() {
            return name;
        }

        @Override
        public List<URL> getMavenUrlDependencies() {
            return Collections.EMPTY_LIST;
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        previous = System.getProperties();
        assertFalse(previous instanceof ClassLoaderIsolatedSystemProperties);
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.setProperties(previous);
    }

}
