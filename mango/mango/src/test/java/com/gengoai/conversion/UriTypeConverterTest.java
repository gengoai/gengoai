package com.gengoai.conversion;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class UriTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(URI.create("file:/home/user/test"),
                   Converter.convert("//home/user/test", URI.class));
      assertEquals(URI.create("https://home/user/test"),
                   Converter.convert("https://home/user/test", URI.class));
   }

   @Test(expected = TypeConversionException.class)
   public void fail() throws TypeConversionException {
      Converter.convert(new String[]{"A"}, URI.class);
   }
}