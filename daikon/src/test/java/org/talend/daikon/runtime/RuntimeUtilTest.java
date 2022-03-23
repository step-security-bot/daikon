package org.talend.daikon.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class RuntimeUtilTest {

    @Test
    public void testMvnUrlParseUrl() throws MalformedURLException, IOException {
        RuntimeUtil.registerMavenUrlHandler();
        URL url = new URL("mvn:org/art/1.2");
        assertEquals("org/art/1.2", url.getPath());
        // check that foo is not parsed at all
        URL badurl = new URL(url, "foo");
        assertEquals("org/art/1.2", badurl.getPath());
    }

    @Test
    public void testRegisterMavenUrlFactory() {
        try {
            RuntimeUtil.registerMavenUrlFactory();
            RuntimeUtil.registerMavenUrlFactory();
        } catch (Error err) {
            fail(err.getMessage());
        }
    }

}
