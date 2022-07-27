package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import com.gengoai.tuple.Tuple2;
import com.gengoai.tuple.Tuple3;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class Tuple3ConversionTest {

   @Test
   public void success() throws TypeConversionException {
      assertEquals(Tuple3.of("A", "B", "B"),
                   Converter.convert(arrayOf("A", "B", "B"), Tuple3.class, String.class, String.class, String.class));
      assertEquals(Tuple3.of("A", "B", "B"),
                   Converter.convert(JsonEntry.array("A", "B", "B"), Tuple3.class, String.class, String.class,
                                     String.class));
      assertEquals(Tuple3.of("A", "B", null),
                   Converter.convert(Tuple3.of("A", "B", null), Tuple3.class, String.class, String.class, String.class));
      assertEquals(Tuple3.of("A", null, null), Converter.convert("A", Tuple3.class, String.class, String.class,
                                                                 String.class));
   }

   @Test(expected = TypeConversionException.class)
   public void failTooLong() throws TypeConversionException {
      Converter.convert("[A,B,C, D]", Tuple2.class, String.class, String.class);
   }

}//END OF Tuple1ConversionTest
