package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import com.gengoai.tuple.Tuple2;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class Tuple2ConversionTest {

   @Test
   public void success() throws TypeConversionException {
      assertEquals(Tuple2.of("A", "B"),
                   Converter.convert(arrayOf("A", "B"), Tuple2.class, String.class, String.class));
      assertEquals(Tuple2.of("A", "B"),
                   Converter.convert(JsonEntry.array("A", "B"), Tuple2.class, String.class, String.class));
      assertEquals(Tuple2.of("A", "B"),
                   Converter.convert(Tuple2.of("A", "B"), Tuple2.class, String.class, String.class));
      assertEquals(Tuple2.of("A", null), Converter.convert("A", Tuple2.class, String.class, String.class));
   }

   @Test(expected = TypeConversionException.class)
   public void failTooLong() throws TypeConversionException {
      Converter.convert("[A,B,C]", Tuple2.class, String.class, String.class);
   }

}//END OF Tuple1ConversionTest
