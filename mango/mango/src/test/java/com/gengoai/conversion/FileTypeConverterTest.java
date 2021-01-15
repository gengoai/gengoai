package com.gengoai.conversion;

import com.gengoai.io.Resources;
import com.gengoai.json.JsonEntry;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class FileTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(new File("/home/user/"),
                   Converter.convert("/home/user", File.class));
      assertEquals(new File("/home/user/"),
                   Converter.convert(Resources.from("/home/user"), File.class));
      assertEquals(new File("/home/user/"),
                   Converter.convert(new File("/home/user"), File.class));
      assertEquals(new File("/home/user/"),
                   Converter.convert(JsonEntry.from("/home/user"), File.class));
   }

   @Test(expected = TypeConversionException.class)
   public void failURL() throws TypeConversionException {
      Converter.convert("http://wwww.google.com", File.class);
   }

   @Test(expected = TypeConversionException.class)
   public void failObject() throws TypeConversionException {
      Converter.convert(Collections.emptyList(), File.class);
   }
}