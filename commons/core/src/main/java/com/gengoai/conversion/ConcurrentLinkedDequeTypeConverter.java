package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * ConcurrentLinkedDeque Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ConcurrentLinkedDequeTypeConverter extends CollectionTypeConverter {
   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(ConcurrentLinkedDeque.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new ConcurrentLinkedDeque<>();
   }
}//END OF ConcurrentLinkedDequeTypeConverter
