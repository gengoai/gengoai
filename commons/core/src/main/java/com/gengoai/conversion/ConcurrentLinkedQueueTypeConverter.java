package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * ConcurrentLinkedQueue Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ConcurrentLinkedQueueTypeConverter extends CollectionTypeConverter {
   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(ConcurrentLinkedQueue.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new ConcurrentLinkedQueue<>();
   }
}//END OF ConcurrentLinkedQueueTypeConverter
