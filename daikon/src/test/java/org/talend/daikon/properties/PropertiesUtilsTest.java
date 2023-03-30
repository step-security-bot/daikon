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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

public class PropertiesUtilsTest {

    private static class InjectToProperties extends PropertiesImpl {

        @Inject
        public Object obj;

        public InjectToProperties(String name) {
            super(name);
        }

    }

    @Test
    public void injectObjectTest() {
        InjectToProperties props = new InjectToProperties("props");
        Object injectedObject = new Object();

        PropertiesUtils.injectObject(props, injectedObject);

        assertSame(injectedObject, props.obj, "Injected object is not the same");
    }

}
