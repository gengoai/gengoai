package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class CharsetTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(StandardCharsets.UTF_8, Converter.convert("utf-8", Charset.class));
      assertEquals(StandardCharsets.UTF_8, Converter.convert(StandardCharsets.UTF_8, Charset.class));
      assertEquals(StandardCharsets.UTF_8, Converter.convert(JsonEntry.from("utf-8"), Charset.class));
   }

   @Test(expected = TypeConversionException.class)
   public void badConvert() throws TypeConversionException {
      Converter.convert("NOT_ONE-8", Charset.class);
   }
}