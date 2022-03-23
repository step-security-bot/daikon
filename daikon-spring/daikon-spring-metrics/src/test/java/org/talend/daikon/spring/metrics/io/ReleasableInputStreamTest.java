package org.talend.daikon.spring.metrics.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.io.input.NullInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReleasableInputStreamTest {

    private final AtomicBoolean wasCalled = new AtomicBoolean();

    private ReleasableInputStream releasableInputStream;

    private ReleasableInputStream failedReleasableInputStream;

    @BeforeEach
    public void setUp() {
        releasableInputStream = new ReleasableInputStream(new NullInputStream(2048), () -> wasCalled.set(true));
        failedReleasableInputStream = new ReleasableInputStream(new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException("Oops");
            }

            @Override
            public int available() throws IOException {
                throw new IOException("Oops");
            }

            @Override
            public synchronized void mark(int readlimit) {
                throw new IllegalArgumentException("Oops");
            }
        }, () -> wasCalled.set(true));
    }

    @AfterEach
    public void tearDown() throws Exception {
        releasableInputStream.close();
        wasCalled.set(false);
    }

    @Test
    public void read() throws Exception {
        // When
        releasableInputStream.read();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedRead() {
        assertThrows(IOException.class, () -> {
            // When
            failedReleasableInputStream.read();

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void read1() throws Exception {
        // When
        releasableInputStream.read(new byte[1024]);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedRead1() {
        assertThrows(IOException.class, () -> {
            // When
            failedReleasableInputStream.read(new byte[1024]);

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void read2() throws Exception {
        // When
        releasableInputStream.read(new byte[1024], 0, 1024);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedRead2() {
        assertThrows(IOException.class, () -> {
            // When
            failedReleasableInputStream.read(new byte[1024], 0, 1024);

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void skip() throws Exception {
        // When
        releasableInputStream.skip(100);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedSkip() {
        assertThrows(IOException.class, () -> {
            // When
            failedReleasableInputStream.skip(100);

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void available() throws Exception {
        // When
        releasableInputStream.available();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedAvailable() {
        assertThrows(IOException.class, () -> {
            // When
            failedReleasableInputStream.available();

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void close() throws Exception {
        // When
        releasableInputStream.close();

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void mark() {
        // When
        releasableInputStream.mark(0);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedMark() {
        assertThrows(RuntimeException.class, () -> {
            // When
            failedReleasableInputStream.mark(0);

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void reset() throws Exception {
        // When
        releasableInputStream.mark(10);
        releasableInputStream.reset();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedReset() throws Exception {
        assertThrows(IOException.class, () -> {
            // When
            failedReleasableInputStream.reset();

            // Then
            assertTrue(wasCalled.get());
        });
    }

    @Test
    public void markSupported() throws Exception {
        // When
        releasableInputStream.markSupported();

        // Then
        assertFalse(wasCalled.get());
    }
}