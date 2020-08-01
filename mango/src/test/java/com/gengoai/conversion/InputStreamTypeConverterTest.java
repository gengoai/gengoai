package com.gengoai.conversion;

import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class InputStreamTypeConverterTest {

   @Test
   public void convert() throws IOException {
      try (InputStream is = Resources.fromString().inputStream()) {
         assertNotNull(Converter.convertSilently(is, InputStream.class));
      }
      assertNotNull(Converter.convertSilently(new byte[1024], InputStream.class));
   }

   @Test(expected = TypeConversionException.class)
   public void fail() throws TypeConversionException {
      Converter.convert(Paths.get("//abaf/dafs/adfsd  /adfsdf"), InputStream.class);
   }
}