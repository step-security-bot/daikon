package org.talend.daikon.security.url;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to validate URLs
 */
public final class URLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLUtils.class);

    /**
     * Taken from the example validation method in OWASP doc:
     * https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html#application-layer_1
     * Additional documentation:
     * https://en.wikipedia.org/wiki/Unique_local_address
     * https://en.wikipedia.org/wiki/Private_network
     * https://simpledns.com/private-ipv6
     */
    private static final Set<String> IPV6_LOCAL_ADDRESSES_PREFIX = new HashSet<>(Arrays.asList("fc", "fd", "fe", "ff"));

    private URLUtils() {
    }

    /**
     * Tests if the given url resolves to a local address (localhost, or any private / local ip range)
     * NB: this requires a dns resolution, as domains can point to local addresses.
     * <p>
     * If the domain cannot be resolved, false is returned
     */
    public static boolean isLocalUrl(final URL url) {

        final String hostDomain = url.getHost();

        try {
            final InetAddress inetAddress = InetAddress.getByName(hostDomain);

            // Test for private ip ranges or localhost
            return isLocalAddress(inetAddress);

        } catch (final UnknownHostException e) {
            LOGGER.debug("Could not resolve host of URL", e);
        }

        return false;
    }

    /**
     * Check if {@link InetAddress} is local. (localhost, or any private / local ip range)
     */
    public static boolean isLocalAddress(final InetAddress inetAddress) {
        return isLocalUsingBuiltinJavaChecks(inetAddress) || isLocalUsingExtraIpv6Checks(inetAddress);
    }

    private static boolean isLocalUsingBuiltinJavaChecks(final InetAddress inetAddress) {
        return inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()
                || inetAddress.isAnyLocalAddress() // Unicast
                || inetAddress.isMCLinkLocal() || inetAddress.isMCSiteLocal() || inetAddress.isMCNodeLocal()
                || inetAddress.isMCOrgLocal(); // Multicast
    }

    private static boolean isLocalUsingExtraIpv6Checks(final InetAddress inetAddress) {
        if (!(inetAddress instanceof Inet6Address)) {
            return false;
        }
        final String inet6AddressString = inetAddress.getHostAddress();
        return IPV6_LOCAL_ADDRESSES_PREFIX.stream()
                .anyMatch(addressStart -> inet6AddressString.trim().toLowerCase().startsWith(addressStart));
    }
}
