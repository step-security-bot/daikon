/**
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.daikon.i18n;

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CustomExtension implements AfterEachCallback, BeforeEachCallback {

    private Runnable callback;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        callback = ProviderLocator.instance().register(Thread.currentThread().getContextClassLoader(), new BaseProvider() {

            @Override
            protected ResourceBundle createBundle(final String baseName, final Locale locale) {
                return new BaseBundle() {

                    private int incr = 1;

                    @Override
                    protected Set<String> doGetKeys() {
                        return Collections.singleton("thekey");
                    }

                    @Override
                    protected Object handleGetObject(final String key) {
                        return "thekey".equals(key) ? "thevalue" + incr++ : null;
                    }
                };
            }

            @Override
            protected boolean supports(final String baseName) {
                return "test".equals(baseName);
            }
        });
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        callback.run();
    }
}
