package org.talend.daikon.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.daikon.exception.TalendRuntimeException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * To find more test, please refer to TypeCOnverterTest
 */
public class LocalTimeConverterTest {

    @Test
    public void testAsLocalTimeWithDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ss:mm:HH");
        assertEquals(LocalTime.of(8, 15, 20), TypeConverter.asLocalTime().withDateTimeFormatter(formatter).convert("20:15:08"));
    }

    @Test
    public void testError() {
        TalendRuntimeException thrown = assertThrows(TalendRuntimeException.class, () -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ss:mm:HH");
            TypeConverter.asLocalTime().withDateTimeFormatter(formatter).convert("ss:15:08");
        });
        assertEquals(TypeConverterErrorCode.CANNOT_PARSE, thrown.getCode());
        assertEquals("Cannot parse 'ss:15:08' using the specified format.", thrown.getMessage());

    }

}
