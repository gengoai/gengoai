package com.gengoai.conversion;

import org.junit.Test;

import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.collection.Arrays2.arrayOfDouble;
import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author David B. Bracewell
 */
public class ArrayConversionTest {


   @Test
   public void testStrings() throws TypeConversionException {
      assertArrayEquals(arrayOf("A", "B", "C"),
                        Converter.convert("[A,B,C]", String[].class));
      assertArrayEquals(arrayOf("A", "B", "C"),
                        Converter.convert("A,B,C", String[].class));
      assertArrayEquals(arrayOf("A", "B", "C"),
                        Converter.convert(arrayOf("A", "B", "C"), String[].class));
      assertArrayEquals(arrayOf("A", "B", "C"),
                        Converter.convert(asList("A", "B", "C"), String[].class));
      assertArrayEquals(arrayOf(arrayOf("A"), arrayOf("B"), arrayOf("C")),
                        Converter.convert(asList(singletonList("A"), singletonList("B"), singletonList("C")),
                                          String[][].class));
      assertArrayEquals(arrayOf(hashMapOf($("A", 1.0))),
                        Converter.convert("{\"A\":1.0}", Map[].class, String.class, Double.class));
      assertArrayEquals(arrayOf(1.0), Converter.convert(1.0, Double[].class));
      assertArrayEquals(arrayOfDouble(1.0), Converter.convert(1.0, double[].class), 0d);
      assertArrayEquals(arrayOf($("A", "B")),
                        Converter.convert(hashMapOf($("A", "B")), Map.Entry[].class, String.class, String.class));
      assertArrayEquals(arrayOf("A B C"), Converter.convert("A B C", String[].class));
   }


}//END OF ArrayConversionTest
