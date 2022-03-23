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
package org.talend.daikon.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.ReferenceExampleProperties.TestAProperties;
import org.talend.daikon.properties.ReferenceExampleProperties.TestBProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ExtendWith(SoftAssertionsExtension.class)
public class ReferencePropertiesTest {

    @InjectSoftAssertions
    BDDSoftAssertions errorCollector;

    @Test
    public void testI18N() {
        PropertiesTestUtils.checkAllI18N(new ReferenceProperties<>("", "").init(), errorCollector);
    }

    @Test
    public void testResolveReferencePropertiesWithMaps() throws ParseException, JsonProcessingException, IOException {

        ReferenceExampleProperties refEProp = new ReferenceExampleProperties(null);
        TestAProperties testAProp = new TestAProperties(null);
        TestBProperties testBProp = new TestBProperties(null);

        assertNull(refEProp.testAPropReference.getReference());
        assertNull(refEProp.testAPropReference2.getReference());
        assertNull(testAProp.testBPropReference.getReference());

        // merge everything to the parent
        Map<String, Properties> definition2PropertiesMap = new HashMap<>();
        definition2PropertiesMap.put("no_used", refEProp);
        definition2PropertiesMap.put(TestAProperties.TEST_A_PROPERTIES_DEFINTION_NAME, testAProp);
        definition2PropertiesMap.put(TestBProperties.TEST_B_PROPERTIES_DEFINTION_NAME, testBProp);
        ReferenceProperties.resolveReferenceProperties(definition2PropertiesMap);

        assertEquals(testAProp, refEProp.testAPropReference.getReference());
        assertEquals(testAProp, refEProp.testAPropReference2.getReference());
        assertEquals(testBProp, testAProp.testBPropReference.getReference());
    }

    @Test
    public void testResolveReferencePropertiesWithRegistryDef() throws ParseException, JsonProcessingException, IOException {

        ReferenceExampleProperties refEProp = new ReferenceExampleProperties(null);
        TestAProperties testAProp = new TestAProperties(null);
        TestBProperties testBProp = new TestBProperties(null);

        assertNull(refEProp.testAPropReference.getReference());
        assertNull(testAProp.testBPropReference.getReference());

        // mock the registry
        DefinitionRegistryService defRegServ = mock(DefinitionRegistryService.class);
        Definition repDef = when(mock(Definition.class).getName()).thenReturn("we_dont_care").getMock();
        when(defRegServ.getDefinitionForPropertiesType(ReferenceExampleProperties.class))
                .thenReturn(Collections.singleton(repDef));
        Definition apDef = when(mock(Definition.class).getName()).thenReturn(TestAProperties.TEST_A_PROPERTIES_DEFINTION_NAME)
                .getMock();
        when(defRegServ.getDefinitionForPropertiesType(TestAProperties.class)).thenReturn(Collections.singleton(apDef));
        Definition bpDef = when(mock(Definition.class).getName()).thenReturn(TestBProperties.TEST_B_PROPERTIES_DEFINTION_NAME)
                .getMock();
        when(defRegServ.getDefinitionForPropertiesType(TestBProperties.class)).thenReturn(Collections.singleton(bpDef));

        // resolve everything
        ReferenceProperties.resolveReferenceProperties(Arrays.asList(refEProp, testAProp, testBProp), defRegServ);

        assertEquals(testAProp, refEProp.testAPropReference.getReference());
        assertEquals(testBProp, testAProp.testBPropReference.getReference());
    }

    @Test
    public void testReferencedPropertiesVisited() throws ParseException, IOException {

        ReferenceExampleProperties refEProp = new ReferenceExampleProperties(null);

        TestAProperties testAProp = new TestAProperties("testAProp");
        refEProp.testAPropReference.setReference(testAProp);

        refEProp.init();

        final Set<AnyProperty> visited = new HashSet<>();
        refEProp.accept(new AnyPropertyVisitor() {

            @Override
            public void visit(Property property, Properties parent) {
                visited.add(property);
            }

            @Override
            public void visit(Properties properties, Properties parent) {
                visited.add(properties);
            }
        }, null);

        assertNotNull(visited.contains(testAProp), "Referenced properties visited");
    }

    @Test
    public void testResolveReferenceCallBackCalled() throws ParseException, IOException {

        // given
        ReferenceExampleProperties refEProp = new ReferenceExampleProperties(null);
        TestAProperties testAProp = new TestAProperties(null);
        TestBProperties testBProp = new TestBProperties(null);

        assertFalse(refEProp.afterRefCallbackCalled);

        // when

        // merge everything to the parent
        Map<String, Properties> definition2PropertiesMap = new HashMap<>();
        definition2PropertiesMap.put("no_used", refEProp);
        definition2PropertiesMap.put(TestAProperties.TEST_A_PROPERTIES_DEFINTION_NAME, testAProp);
        definition2PropertiesMap.put(TestBProperties.TEST_B_PROPERTIES_DEFINTION_NAME, testBProp);
        ReferenceProperties.resolveReferenceProperties(definition2PropertiesMap, true);

        // then
        assertTrue(refEProp.afterRefCallbackCalled);
    }

}
