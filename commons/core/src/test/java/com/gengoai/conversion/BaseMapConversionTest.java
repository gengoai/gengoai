package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.junit.Test;

import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public abstract class BaseMapConversionTest {

   @SuppressWarnings("rawtypes")
   private final Class<? extends Map> aClass;

   @SuppressWarnings("rawtypes")
   protected BaseMapConversionTest(Class<? extends Map> aClass) {
      this.aClass = aClass;
   }


   private Map<?, ?> create(Object[] keys, Object[] values) {
      Map<Object, Object> m = newMap();
      for (int i = 0; i < keys.length; i++) {
         m.put(keys[i], values[i]);
      }
      return m;
   }

   protected abstract Map<Object, Object> newMap();


   @Test
   public void success() throws TypeConversionException {
      assertEquals(create(arrayOf("A", "B"), arrayOf(1.0, 2.0)),
                   Converter.convert("{\"A\":1.0, \"B\" : 2.0}", aClass, String.class, Double.class));
      assertEquals(create(arrayOf("A", "B"), arrayOf(1.0, 2.0)),
                   Converter.convert("A=1.0, B=2.0", aClass, String.class, Double.class));
      assertEquals(create(arrayOf("A", "B"), arrayOf(1.0, 2.0)),
                   Converter.convert(arrayOf("A", 1.0, "B", 2.0), aClass, String.class, Double.class));
      assertEquals(create(arrayOf("A", "B"), arrayOf(1.0, 2.0)),
                   Converter.convert(JsonEntry.object()
                                              .addProperty("A", 1.0)
                                              .addProperty("B", 2.0), aClass, String.class, Double.class));
      assertEquals(create(arrayOf("A", "B"),
                          arrayOf(1.0, 2.0)),
                   Converter.convert(create(arrayOf("A", "B"),
                                            arrayOf(1.0, 2.0)), aClass, String.class, Double.class));
   }


   @Test(expected = TypeConversionException.class)
   public void fail() throws TypeConversionException {
      Converter.convert("ALOHA", aClass, Integer.class, Integer.class);
   }

   @Test(expected = TypeConversionException.class)
   public void failJson() throws TypeConversionException {
      Converter.convert(JsonEntry.array("[ALOHA]"), aClass, Integer.class, Integer.class);
   }

}//END OF BaseCollectionConversionTest
