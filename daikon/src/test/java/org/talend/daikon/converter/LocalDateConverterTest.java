package org.talend.daikon.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * To find more test, please refer to TypeConverterTest
 */
public class LocalDateConverterTest {

    @Test
    public void testAsLocalDateWithDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        assertEquals(LocalDate.of(2007, 12, 03),
                TypeConverter.asLocalDate().withDateTimeFormatter(formatter).convert("03/12/2007"));
    }

    @Test
    public void testError() {
        TalendRuntimeException thrown = assertThrows(TalendRuntimeException.class, () -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            TypeConverter.asLocalDate().withDateTimeFormatter(formatter).convert("  03/12/2007");
        });
        assertEquals(TypeConverterErrorCode.CANNOT_PARSE, thrown.getCode());
        assertEquals("Cannot parse '  03/12/2007' using the specified format.", thrown.getMessage());
    }

}
