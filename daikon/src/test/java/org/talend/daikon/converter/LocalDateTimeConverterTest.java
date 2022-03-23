package org.talend.daikon.converter;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.daikon.exception.TalendRuntimeException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * To find more test, please refer to TypeCOnverterTest
 */
public class LocalDateTimeConverterTest {

    @Test
    public void testAsLocalDateTimeWithDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        assertEquals(LocalDateTime.of(2007, 12, 03, 10, 15, 30),
                TypeConverter.asLocalDateTime().withDateTimeFormatter(formatter).convert("03/12/2007 10:15:30"));
    }

    @Test
    public void testError() {
        TalendRuntimeException thrown = assertThrows(TalendRuntimeException.class, () -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            TypeConverter.asLocalDateTime().withDateTimeFormatter(formatter).convert("dd/12/2007 10:15:30");
        });
        assertEquals(TypeConverterErrorCode.CANNOT_PARSE, thrown.getCode());
        assertEquals("Cannot parse 'dd/12/2007 10:15:30' using the specified format.", thrown.getMessage());
    }

}
