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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.daikon.properties.TestPropertiesList.TestEnum;
import org.talend.daikon.properties.TestPropertiesList.TestProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.PropertiesServiceImpl;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class PropertiesListTest {

    private PropertiesService<Properties> propService;

    @BeforeEach
    public void init() {
        propService = new PropertiesServiceImpl();
    }

    @Test
    public void testSetRows() {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        TestProperties row1 = propertiesList.getNestedPropertiesFactory().createAndInit("row1");
        TestProperties row2 = propertiesList.getNestedPropertiesFactory().createAndInit("row2");
        TestProperties row3 = propertiesList.getNestedPropertiesFactory().createAndInit("row3");
        propertiesList.setRows(Arrays.asList(row1, row2, row3));

        // check rows quantity and order
        ArrayList<TestProperties> subProps = new ArrayList<>(propertiesList.getPropertiesList());

        assertEquals(3, subProps.size(), "Number of added properties is wrong");
        assertEquals(row1, subProps.get(0));
        assertEquals(row2, subProps.get(1));
        assertEquals(row3, subProps.get(2));
    }

    @Test
    public void testReSetRows() {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        propertiesList.init();
        TestProperties row1 = propertiesList.getNestedPropertiesFactory().createAndInit("row1");
        propertiesList.setRows(Arrays.asList(row1));

        TestProperties row2 = propertiesList.getNestedPropertiesFactory().createAndInit("row2");
        TestProperties row3 = propertiesList.getNestedPropertiesFactory().createAndInit("row3");
        propertiesList.setRows(Arrays.asList(row2, row3));

        // check rows quantity and order
        ArrayList<TestProperties> subProps = new ArrayList<>(propertiesList.getPropertiesList());

        assertEquals(2, subProps.size(), "Number of added properties is wrong");
        assertEquals(row2, subProps.get(0));
        assertEquals(row3, subProps.get(1));
    }

    @Test
    public void testAddRow() {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        propertiesList.init();
        TestProperties row1 = propertiesList.getNestedPropertiesFactory().createAndInit("row1");
        propertiesList.addRow(row1);

        ArrayList<TestProperties> subProps = new ArrayList<>(propertiesList.getPropertiesList());

        assertEquals(1, subProps.size(), "Number of added properties is wrong");
        assertEquals(row1, subProps.get(0));
    }

    @Test
    public void testAddRows() {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        TestProperties row1 = propertiesList.getNestedPropertiesFactory().createAndInit("row1");
        TestProperties row2 = propertiesList.getNestedPropertiesFactory().createAndInit("row2");
        TestProperties row3 = propertiesList.getNestedPropertiesFactory().createAndInit("row3");
        propertiesList.addAllRows(Arrays.asList(row1, row2, row3));

        // check rows quantity and order
        ArrayList<TestProperties> subProps = new ArrayList<>(propertiesList.getPropertiesList());

        assertEquals(3, subProps.size(), "Number of added properties is wrong");
        assertEquals(row1, subProps.get(0));
        assertEquals(row2, subProps.get(1));
        assertEquals(row3, subProps.get(2));

        TestProperties row4 = propertiesList.getNestedPropertiesFactory().createAndInit("row4");
        TestProperties row5 = propertiesList.getNestedPropertiesFactory().createAndInit("row5");
        propertiesList.addAllRows(Arrays.asList(row4, row5));
        subProps = new ArrayList<>(propertiesList.getPropertiesList());
        assertEquals(5, subProps.size(), "Number of added properties is wrong");
        assertEquals(row4, subProps.get(3));
        assertEquals(row5, subProps.get(4));
    }

    @Test
    public void testCreateAndAddRow() {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        TestProperties row = propertiesList.createAndAddRow();

        ArrayList<TestProperties> subProps = new ArrayList<>(propertiesList.getPropertiesList());

        assertEquals(1, subProps.size());
        assertEquals(row, subProps.get(0));
    }

    @Test
    public void testAfterPropertyChange() throws Throwable {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        TestProperties row1 = propertiesList.getNestedPropertiesFactory().createAndInit("row1");
        row1.intProp.setValue(1);
        PropertiesTestUtils.checkAndAfter(propService, row1.getForm(Form.MAIN), row1.intProp.getName(), row1);
        assertEquals(1, (int) row1.intProp.getValue());
        assertEquals(TestEnum.ONE, row1.stringProp.getValue());

        row1.intProp.setValue(2);
        PropertiesTestUtils.checkAndAfter(propService, row1.getForm(Form.MAIN), row1.intProp.getName(), row1);
        assertEquals(2, (int) row1.intProp.getValue());
        assertEquals(TestEnum.TWO, row1.stringProp.getValue());
    }

    @Test
    public void testAfterPropertiesListChange() throws Throwable {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        TestProperties row1 = propertiesList.getNestedPropertiesFactory().createAndInit("row1");
        propertiesList.addRow(row1);
        row1.intProp.setValue(1);

        assertFalse(propertiesList.getForm(Form.MAIN).getWidget(row1.getName()).isCallAfter());
        PropertiesTestUtils.checkAndAfter(propService, row1.getForm(Form.MAIN), row1.intProp.getName(), row1);

        assertEquals(1, (int) row1.intProp.getValue());
        assertEquals(TestEnum.ONE, row1.stringProp.getValue());
    }

    @Test
    public void testMinMaxGetterSetter() {
        PropertiesList<TestProperties> propertiesList = createPropertiesList();
        assertEquals(propertiesList.getMinItems(), "1");
        assertEquals(propertiesList.getMaxItems(), "10");
        propertiesList.setMinItems("0");
        propertiesList.setMaxItems("20");
        assertEquals(propertiesList.getMinItems(), "0");
        assertEquals(propertiesList.getMaxItems(), "20");
    }

    @Test
    public void testi18N() {
        TestPropertiesList.TestComponentProperties tpl = (TestPropertiesList.TestComponentProperties) new TestPropertiesList.TestComponentProperties(
                "foo").init();
        assertEquals(tpl.filters.getDisplayName(), "Filters");
    }

    private PropertiesList<TestProperties> createPropertiesList() {
        PropertiesList<TestProperties> propertiesList = new PropertiesList<>("propertiesList",
                new PropertiesList.NestedPropertiesFactory<TestProperties>() {

                    @Override
                    public TestProperties createAndInit(String name) {
                        return (TestProperties) new TestProperties(name).init();
                    }

                });
        propertiesList.init();
        return propertiesList;
    }

}
