package com.gengoai.conversion;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class UrlTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException, MalformedURLException {
      assertEquals(URI.create("file:/home/user/test").toURL(),
                   Converter.convert("//home/user/test", URL.class));
      assertEquals(URI.create("https://home/user/test").toURL(),
                   Converter.convert("https://home/user/test", URL.class));
   }

   @Test(expected = TypeConversionException.class)
   public void fail() throws TypeConversionException {
      Converter.convert(new String[]{"A"}, URL.class);
   }

}