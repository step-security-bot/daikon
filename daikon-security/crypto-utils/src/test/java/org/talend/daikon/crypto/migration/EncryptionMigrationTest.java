package org.talend.daikon.crypto.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.daikon.crypto.CipherSources;
import org.talend.daikon.crypto.Encryption;
import org.talend.daikon.crypto.KeySources;

public class EncryptionMigrationTest {

    @Test
    public void shouldNotAcceptEmptySource() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionMigration.build(null, new Encryption(KeySources.random(6), CipherSources.getDefault()));
        });
    }

    @Test
    public void shouldNotAcceptEmptyTarget() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionMigration.build(new Encryption(KeySources.random(6), CipherSources.getDefault()), null);
        });
    }

    @Test
    public void shouldReencrypt() throws Exception {
        // given
        final String plainText = "5ecr3t";
        final Encryption source = new Encryption(KeySources.fixedKey("DataPrepIsSoCool"), CipherSources.aes());
        final Encryption target = new Encryption(KeySources.random(16), CipherSources.getDefault());
        final EncryptionMigration migration = EncryptionMigration.build(source, target);
        final String originalEncrypted = source.encrypt(plainText);

        // when
        final String reencrypt = migration.migrate(originalEncrypted);
        final String decrypted = target.decrypt(reencrypt);

        // then
        assertEquals(plainText, decrypted);
        assertNotEquals(originalEncrypted, reencrypt);
    }

    @Test
    public void shouldMigrateEncryptedString() throws Exception {
        // given
        final String plainText = "5ecr3t";
        final Encryption source = new Encryption(KeySources.fixedKey("DataPrepIsSoCool"), CipherSources.aes());
        final Encryption target = new Encryption(KeySources.random(16), CipherSources.getDefault());
        final EncryptionMigration migration = EncryptionMigration.build(source, target);
        final String originalEncrypted = source.encrypt(plainText);

        // when
        final String decrypt = source.decrypt(originalEncrypted);
        final String encryptAndMigrated1 = migration.migrate(originalEncrypted);
        final String encryptAndMigrated2 = migration.migrate(originalEncrypted);
        final String decryptAfterMigrated1 = target.decrypt(encryptAndMigrated1);
        final String decryptAfterMigrated2 = target.decrypt(encryptAndMigrated2);

        // then
        assertEquals(plainText, decrypt);
        assertEquals(plainText, decryptAfterMigrated1);
        assertEquals(plainText, decryptAfterMigrated2);
        assertNotEquals(originalEncrypted, encryptAndMigrated1);
        assertNotEquals(encryptAndMigrated1, encryptAndMigrated2);
    }

}