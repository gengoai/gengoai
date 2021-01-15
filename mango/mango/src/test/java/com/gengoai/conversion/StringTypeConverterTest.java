package com.gengoai.conversion;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author David B. Bracewell
 */
public class StringTypeConverterTest {

   @Test
   public void convert() throws MalformedURLException, TypeConversionException {
      URL google = new URL("http://www.google.com");
   }
}