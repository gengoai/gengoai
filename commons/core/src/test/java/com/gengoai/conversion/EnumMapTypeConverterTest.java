package com.gengoai.conversion;

import com.gengoai.Language;
import org.junit.Test;

import java.util.EnumMap;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class EnumMapTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      EnumMap<Language, Double> emap = new EnumMap<>(Language.class);
      emap.put(Language.SPANISH, 20.2);
      emap.put(Language.ENGLISH, 20.3);
      assertEquals(emap,
                   Converter.convert("{\"SPANISH\": 20.2, \"ENGLISH\": 20.3}", EnumMap.class, Language.class,
                                     Double.class));
      assertEquals(emap,
                   Converter.convert(hashMapOf($("SPANISH", 20.2),
                                               $("ENGLISH", 20.3)),
                                     parameterizedType(EnumMap.class, Language.class, Double.class)));
   }
}