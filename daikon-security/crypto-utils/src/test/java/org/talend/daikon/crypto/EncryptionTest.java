package org.talend.daikon.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.crypto.IllegalBlockSizeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EncryptionTest {

    public static final String DECRYPTED_URI = "mongodb://toto:truc@talend.org/talend-db?ssl=toto&truc=machin";

    public static final String ENCRYPTED_URI = "mongodb://toto:qxjQWF%2FZsuzzeLzKIop2pQ%3D%3D@talend.org/talend-db?ssl=toto&truc=machin";

    private Encryption encryption;

    @BeforeEach
    public void setUp() {
        encryption = new Encryption(KeySources.fixedKey("DataPrepIsSoCool"), CipherSources.aes());
    }

    @Test
    public void should_get_the_same_string_after_encrypt_then_decrypt() throws Exception {
        // given
        String src = "ApplicationName";

        // when
        String encrypted = encryption.encrypt(src);
        String decrypted = encryption.decrypt(encrypted);

        // then
        assertEquals(src, decrypted);
    }

    @Test
    public void should_throw_an_exception_when_trying_to_decrypt_a_non_encrypted_value() throws Exception {
        assertThrows(IllegalBlockSizeException.class, () -> {
            // given
            String src = "ApplicationName";

            // when
            encryption.decrypt(src);
        });
    }

    @Test
    public void encryptUriPassword_shouldEncryptCredentials() throws Exception {
        String encrypted = encryption.encryptUriPassword(DECRYPTED_URI);

        // then
        assertEquals(ENCRYPTED_URI, encrypted);
    }

    @Test
    public void decryptUriPassword_shouldDecryptCredentials() throws Exception {
        // when
        String decrypted = encryption.decryptUriPassword(ENCRYPTED_URI);

        // then
        assertEquals(DECRYPTED_URI, decrypted);
    }

}
