package com.gengoai.conversion;

import com.gengoai.io.Resources;
import com.gengoai.json.JsonEntry;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class PathTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(Paths.get("/home/user/"),
                   Converter.convert("/home/user", Path.class));
      assertEquals(Paths.get("/home/user/"),
                   Converter.convert(Resources.from("/home/user"), Path.class));
      assertEquals(Paths.get("/home/user/"),
                   Converter.convert(new File("/home/user"), Path.class));
      assertEquals(Paths.get("/home/user/"),
                   Converter.convert(Paths.get("/home/user"), Path.class));
      assertEquals(Paths.get("/home/user/"),
                   Converter.convert(JsonEntry.from("/home/user"), Path.class));
   }

   @Test(expected = TypeConversionException.class)
   public void failURL() throws TypeConversionException {
      Converter.convert("http://wwww.google.com", Path.class);
   }

   @Test(expected = TypeConversionException.class)
   public void failObject() throws TypeConversionException {
      Converter.convert(Collections.emptyList(), Path.class);
   }
}