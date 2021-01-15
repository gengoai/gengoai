package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.HashMap;
import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Map and HashMap Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class HashMapTypeConverter extends MapTypeConverter {
   @Override
   public Map<?, ?> createMap() {
      return new HashMap<>();
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(HashMap.class, Map.class);
   }
}//END OF HashMapTypeConverter
