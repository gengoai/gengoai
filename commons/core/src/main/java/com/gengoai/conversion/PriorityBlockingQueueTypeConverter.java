package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * PriorityBlockingQueue Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class PriorityBlockingQueueTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(PriorityBlockingQueue.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new PriorityBlockingQueue<>();
   }
}
