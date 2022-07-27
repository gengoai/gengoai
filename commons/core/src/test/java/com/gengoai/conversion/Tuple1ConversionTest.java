package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import com.gengoai.tuple.Tuple1;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class Tuple1ConversionTest {

   @Test
   public void success() throws TypeConversionException {
      assertEquals(Tuple1.of("A"),
                   Converter.convert("A", Tuple1.class, String.class));
      assertEquals(Tuple1.of("A"),
                   Converter.convert(JsonEntry.array("A"), Tuple1.class, String.class));
      assertEquals(Tuple1.of("A"),
                   Converter.convert(Tuple1.of("A"), Tuple1.class, String.class));
      assertArrayEquals(Tuple1.of(arrayOf("A", "B")).v1,
                        Converter.<Tuple1<Object[]>>convert("[A,B]", Tuple1.class, String[].class).v1);
   }

   @Test(expected = TypeConversionException.class)
   public void failTooLong() throws TypeConversionException {
      Converter.convert("[A,B]", Tuple1.class, String.class);
   }

}//END OF Tuple1ConversionTest
