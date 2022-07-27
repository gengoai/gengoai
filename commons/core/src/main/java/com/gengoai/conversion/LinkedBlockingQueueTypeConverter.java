package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * LinkedBlockingQueue Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LinkedBlockingQueueTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(LinkedBlockingQueue.class, BlockingQueue.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new LinkedBlockingQueue<>();
   }
}
