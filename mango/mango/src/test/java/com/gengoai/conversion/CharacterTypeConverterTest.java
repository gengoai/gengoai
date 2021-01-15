package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CharacterTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      int a = 'a';
      assertEquals(Character.valueOf('a'), Converter.convert("a", char.class));
      assertEquals(Character.valueOf('a'), Converter.convert('a', char.class));
      assertEquals(Character.valueOf('a'), Converter.convert(JsonEntry.from("a"), char.class));
      assertEquals(Character.valueOf('a'), Converter.convert(a, char.class));
      assertEquals(Character.valueOf('a'), Converter.convert(JsonEntry.from(a), char.class));
   }

   @Test(expected = TypeConversionException.class)
   public void badString() throws TypeConversionException {
      Converter.convert("ABC", char.class);
   }

   @Test(expected = TypeConversionException.class)
   public void badObject() throws TypeConversionException {
      Converter.convert(arrayOf(1, 2, 3), char.class);
   }
}