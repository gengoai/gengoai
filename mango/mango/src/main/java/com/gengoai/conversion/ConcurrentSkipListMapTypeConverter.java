package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * ConcurrentSkipListMap Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ConcurrentSkipListMapTypeConverter extends MapTypeConverter {
   @Override
   public Map<?, ?> createMap() {
      return new ConcurrentSkipListMap<>();
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(ConcurrentSkipListMap.class);
   }
}//END OF ConcurrentSkipListMapTypeConverter
