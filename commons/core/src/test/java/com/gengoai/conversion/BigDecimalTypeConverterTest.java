package com.gengoai.conversion;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class BigDecimalTypeConverterTest {

   @Test
   public void convertNumber() throws TypeConversionException {
      assertEquals(BigDecimal.valueOf(10),
                   Converter.convert(10, BigDecimal.class));

   }
}