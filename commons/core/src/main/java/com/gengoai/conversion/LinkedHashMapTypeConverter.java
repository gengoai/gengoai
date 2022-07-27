package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * LinkedHashMap Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LinkedHashMapTypeConverter extends MapTypeConverter {
   @Override
   public Map<?, ?> createMap() {
      return new LinkedHashMap<>();
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(LinkedHashMap.class);
   }
}//END OF LinkedHashMapTypeConverter
