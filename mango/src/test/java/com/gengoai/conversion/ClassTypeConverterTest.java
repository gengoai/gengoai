package com.gengoai.conversion;

import com.gengoai.Language;
import org.junit.Test;

import java.util.List;

import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class ClassTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(String.class,
                   Converter.convert("String", Class.class));
      assertEquals(List.class,
                   Converter.convert("List<String>", Class.class));
      assertEquals(Language.class,
                   Converter.convert(Language.class, Class.class));
      assertEquals(List.class,
                   Converter.convert(parameterizedType(List.class, String.class), Class.class));
      assertEquals(String.class,
                   Converter.convert("ABC", Class.class));
   }

}