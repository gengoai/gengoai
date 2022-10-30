package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Map;
import java.util.WeakHashMap;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * WeakHashMap Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class WeakHashMapTypeConverter extends MapTypeConverter {
   @Override
   public Map<?, ?> createMap() {
      return new WeakHashMap<>();
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(WeakHashMap.class);
   }
}//END OF WeakHashMapTypeConverter
