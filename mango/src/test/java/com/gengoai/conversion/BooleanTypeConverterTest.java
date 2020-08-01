package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class BooleanTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertTrue(Converter.convert("true", boolean.class));
      assertTrue(Converter.convert("true", Boolean.class));
      assertFalse(Converter.convert("false", boolean.class));
      assertFalse(Converter.convert("false", Boolean.class));
      assertTrue(Converter.convert(1, boolean.class));
      assertTrue(Converter.convert(1.0, Boolean.class));
      assertFalse(Converter.convert(2.0, boolean.class));
      assertFalse(Converter.convert(-9, Boolean.class));
      assertFalse(Converter.convert(JsonEntry.from(false), boolean.class));
      assertFalse(Converter.convert(JsonEntry.from(0), boolean.class));
      assertTrue(Converter.convert(JsonEntry.from(true), boolean.class));
      assertTrue(Converter.convert(JsonEntry.from("TRUE"), boolean.class));
   }

   @Test(expected = TypeConversionException.class)
   public void failBadType() throws TypeConversionException {
      Converter.convert(arrayOf(1), boolean.class);
   }

   @Test(expected = TypeConversionException.class)
   public void failBadJson() throws TypeConversionException {
      Converter.convert(JsonEntry.array(1), boolean.class);
   }

}