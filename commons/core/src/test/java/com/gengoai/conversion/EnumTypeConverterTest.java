package com.gengoai.conversion;

import com.gengoai.Language;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class EnumTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(Language.CHINESE,
                   Converter.convert("CHINESE", Language.class));
      assertEquals(Language.CHINESE,
                   Converter.convert("com.gengoai.Language.CHINESE", Enum.class));
      assertEquals(Language.ENGLISH,
                   Converter.convert("com.gengoai.Language$1", Enum.class));
      assertEquals(Language.CHINESE,
                   Converter.convert(Language.CHINESE.getClass(), Enum.class));
   }

   @Test(expected = TypeConversionException.class)
   public void badItem() throws TypeConversionException {
      Converter.convert("KLINGON", Language.class);
   }
}