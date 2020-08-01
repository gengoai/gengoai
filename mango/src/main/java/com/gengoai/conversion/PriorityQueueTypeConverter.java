package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.PriorityQueue;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * PriorityQueue Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class PriorityQueueTypeConverter extends CollectionTypeConverter {

   @Override
   public Class[] getConversionType() {
      return arrayOf(PriorityQueue.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new PriorityQueue<>();
   }
}
