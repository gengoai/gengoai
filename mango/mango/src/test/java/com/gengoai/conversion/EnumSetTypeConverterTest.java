package com.gengoai.conversion;

import com.gengoai.Language;
import org.junit.Test;

import java.util.EnumSet;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class EnumSetTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertArrayEquals(arrayOf(Language.ENGLISH,
                                Language.CHINESE),
                        Converter.<EnumSet<Language>>
                                     convert("[\"ENGLISH\",\"CHINESE\"]", EnumSet.class, Language.class).toArray());

   }
}