package org.talend.daikon.security.url;

import java.net.Inet6Address;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class URLUtilsTest {

    @org.junit.Test
    public void testIsLocalUrlLoopbackAddress() throws MalformedURLException {
        // loopback address
        assertTrue(URLUtils.isLocalUrl(new URL("http://127.0.0.1/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://127.0.0.1:80/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://localhost/my/api")));
        assertFalse(URLUtils.isLocalUrl(new URL("http://myapp.localhost.com:80/my/api")));
        assertFalse(URLUtils.isLocalUrl(new URL("http://www.google.fr")));
    }

    @org.junit.Test
    public void testIsLocalUrlPrivateAddress() throws MalformedURLException {
        // RFC1918 / Private Address Space : https://tools.ietf.org/html/rfc1918
        assertTrue(URLUtils.isLocalUrl(new URL("http://10.0.0.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://10.10.0.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://10.10.10.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://10.10.10.10/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://10.255.255.255/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://172.16.0.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://172.16.255.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://172.25.25.25/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://172.31.0.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://172.31.0.255/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://192.168.0.0/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://192.168.128.10/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("http://192.168.255.255/my/api")));
        assertTrue(URLUtils.isLocalUrl(new URL("https://172.18.0.7/my/api")));
    }

    @org.junit.Test
    public void testIsLocalUrlIPV6() throws Exception {
        assertTrue(URLUtils.isLocalAddress(Inet6Address.getByName("fe80::9656:d028:8652:66b6")));
        assertFalse(URLUtils.isLocalAddress(Inet6Address.getByName("2001:db8::8c28:c929:72db:49fe")));
    }

    @org.junit.Test
    public void testIsLocalAWSMetadataService() throws Exception {
        assertTrue(URLUtils.isLocalUrl(new URL("http://169.254.169.254")));
        assertTrue(URLUtils.isLocalAddress(Inet6Address.getByName("fd00:ec2::254")));
    }
}
