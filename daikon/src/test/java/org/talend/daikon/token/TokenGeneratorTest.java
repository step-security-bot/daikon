package org.talend.daikon.token;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.talend.daikon.security.CryptoHelper;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenGeneratorTest {

    @Test
    public void testArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            TokenGenerator.generateMachineToken(null);
        });
    }

    @Test
    public void testTokenGeneration() throws Exception {
        // When
        final CryptoHelper cryptoHelper = new CryptoHelper("passphrase");
        final String token = TokenGenerator.generateMachineToken(cryptoHelper);

        // Then
        assertNotNull(token);
        final String decryptedToken = cryptoHelper.decrypt(token);
        final String hostName = InetAddress.getLocalHost().getHostName();
        final String osPart = "-" + System.getProperty("os.name") + System.getProperty("os.version");
        final String hostNamePart = "-" + hostName;
        // Contains host name
        assertTrue(decryptedToken.contains(hostNamePart));
        // Contains OS information
        assertTrue(decryptedToken.contains(osPart));
        // Remaining is constructed from of one or more MAC addresses, so it should contain only hexadecimal characters
        final String patternString = "^[A-Fa-f0-9]*$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(decryptedToken.substring(0, decryptedToken.indexOf("-")));
        assertTrue(matcher.matches());
    }
}
