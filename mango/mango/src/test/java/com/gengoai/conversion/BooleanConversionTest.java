package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class BooleanConversionTest {

   @Test
   public void testGood() {
      assertEquals(true, Converter.convertSilently("true", Boolean.class));
      assertEquals(true, Converter.convertSilently("true", boolean.class));
      assertEquals(false, Converter.convertSilently("false", Boolean.class));
      assertEquals(false, Converter.convertSilently("false", boolean.class));
      assertEquals(true, Converter.convertSilently("TRUE", Boolean.class));
      assertEquals(true, Converter.convertSilently("TRUE", boolean.class));
      assertEquals(false, Converter.convertSilently("FALSE", Boolean.class));
      assertEquals(false, Converter.convertSilently("FALSE", boolean.class));
      assertEquals(true, Converter.convertSilently(1, Boolean.class));
      assertEquals(true, Converter.convertSilently(1.0, boolean.class));
      assertEquals(false, Converter.convertSilently(999, Boolean.class));
      assertEquals(false, Converter.convertSilently(885, boolean.class));
      assertEquals(true, Converter.convertSilently(true, Boolean.class));
      assertEquals(true, Converter.convertSilently(Boolean.TRUE, boolean.class));
      assertEquals(false, Converter.convertSilently(false, Boolean.class));
      assertEquals(false, Converter.convertSilently(Boolean.FALSE, boolean.class));
      assertEquals(true, Converter.convertSilently(JsonEntry.from("true"), Boolean.class));
      assertEquals(true, Converter.convertSilently(JsonEntry.from("true"), boolean.class));
      assertEquals(false, Converter.convertSilently(JsonEntry.from("false"), Boolean.class));
      assertEquals(false, Converter.convertSilently(JsonEntry.from("false"), boolean.class));
      assertEquals(true, Converter.convertSilently(JsonEntry.from(true), Boolean.class));
      assertEquals(true, Converter.convertSilently(JsonEntry.from(Boolean.TRUE), boolean.class));
      assertEquals(false, Converter.convertSilently(JsonEntry.from(false), Boolean.class));
      assertEquals(false, Converter.convertSilently(JsonEntry.from(Boolean.FALSE), boolean.class));
      assertEquals(true, Converter.convertSilently(JsonEntry.from(1), Boolean.class));
      assertEquals(true, Converter.convertSilently(JsonEntry.from(1.0), boolean.class));
      assertEquals(false, Converter.convertSilently(JsonEntry.from(999), Boolean.class));
      assertEquals(false, Converter.convertSilently(JsonEntry.from(885), boolean.class));
      assertEquals(false, Converter.convertSilently("string", boolean.class));
      assertNull(Converter.convertSilently(arrayOf("1", "@"), Boolean.class));
      assertNull(Converter.convertSilently(null, Boolean.class));
   }

   @Test(expected = TypeConversionException.class)
   public void badConversion() throws TypeConversionException {
      Converter.convert(arrayOf("1", "@"), Boolean.class);
   }

   @Test(expected = TypeConversionException.class)
   public void badJson() throws TypeConversionException {
      Converter.convert(JsonEntry.array("1", "2"), Boolean.class);
   }

   @Test
   public void nullValue() throws TypeConversionException {
      assertNull(Converter.convert(null, Boolean.class));
   }


}//END OF BooleanConversionTest
