package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * ConcurrentSkipListSet Converter
 *
 * @author David Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ConcurrentSkipListSetTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(ConcurrentSkipListSet.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new ConcurrentSkipListSet<>();
   }
}//END OF ConcurrentSkipListSetTypeConverter
