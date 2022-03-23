// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ClassLoaderBasedI18nMessagesTest {

    @Test
    public void testGetMessageWithSpecificUnknownKeyPrefix() {
        I18nMessages i18nMessages = new ClassLoaderBasedI18nMessages("org.talend.daikon.i18n.testMessage", "!"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("", i18nMessages.getMessage("ze.empty.key"));
        assertEquals("normal", i18nMessages.getMessage("ze.normal.key"));
        assertEquals("normal", i18nMessages.getMessage("ze.normal.key", "foo"));
        assertEquals("test {0} and {1}", i18nMessages.getMessage("ze.message.key"));
        assertEquals("test foo and bar", i18nMessages.getMessage("ze.message.key", "foo", "bar"));
        assertEquals("!unknown.key", i18nMessages.getMessage("unknown.key"));
    }

}
