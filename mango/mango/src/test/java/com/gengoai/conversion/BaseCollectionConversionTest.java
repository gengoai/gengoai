package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOfInt;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public abstract class BaseCollectionConversionTest {

   private final Class<? extends Collection> aClass;

   protected BaseCollectionConversionTest(Class<? extends Collection> aClass) {
      this.aClass = aClass;
   }


   protected abstract Collection<?> create(Object... items);


   @Test
   public void success() throws TypeConversionException {
      assertArrayEquals(create("A", "B", "C", "A").toArray(),
                        Converter.<Collection<?>>convert("A,B,C,A", aClass, String.class).toArray());
      assertArrayEquals(create("A", "B", "C", "A").toArray(),
                        Converter.<Collection<?>>convert("[A,B,C,A]", aClass, String.class).toArray());
      assertArrayEquals(create("A", "B", "C", "A").toArray(),
                        Converter.<Collection<?>>convert("[\"A\",\"B\",\"C\",\"A\"]", aClass, String.class).toArray());
      assertArrayEquals(create(1, 2, 4, 1, 2, 5).toArray(),
                        Converter.<Collection<?>>convert(arrayOfInt(1, 2, 4, 1, 2, 5), aClass,
                                                         Integer.class).toArray());
      assertArrayEquals(create(1).toArray(),
                        Converter.<Collection<?>>convert(1, aClass, Integer.class).toArray());
      assertArrayEquals(create("ALOHA").toArray(),
                        Converter.<Collection<?>>convert("ALOHA", aClass, String.class).toArray());

      assertArrayEquals(create(1, 2, 4, 1, 2, 5).toArray(),
                        Converter.<Collection<?>>convert(JsonEntry.array(1, 2, 4, 1, 2, 5), aClass,
                                                         Integer.class).toArray());
      assertArrayEquals(create("ALOHA").toArray(),
                        Converter.<Collection<?>>convert(JsonEntry.from("ALOHA"), aClass, String.class).toArray());
      assertArrayEquals(create($("A", 1.0)).toArray(),
                        Converter.<Collection<?>>convert(JsonEntry.object().addProperty("A", 1.0), aClass,
                                                         parameterizedType(Map.Entry.class, String.class, Double.class))
                           .toArray());
   }


   @Test(expected = TypeConversionException.class)
   public void fail() throws TypeConversionException {
      Converter.<Collection<?>>convert("ALOHA", aClass, Integer.class);
   }

   @Test(expected = TypeConversionException.class)
   public void failJson() throws TypeConversionException {
      Converter.<Collection<?>>convert(JsonEntry.object()
                                                .addProperty("A", 1.0), aClass, Double.class);
   }

}//END OF BaseCollectionConversionTest
