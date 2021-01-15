package com.gengoai.conversion;

import com.gengoai.collection.Lists;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class IteratorTypeConverterTest {

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(Lists.arrayListOf("A", "B", "C"),
                   Lists.asArrayList(Converter.<Iterator<String>>convert("A,B,C", Iterator.class, String.class)));
   }
}