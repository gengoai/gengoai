package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * SortedMap and TreeMap Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class TreeMapTypeConverter extends MapTypeConverter {
   @Override
   public Map<?, ?> createMap() {
      return new TreeMap<>();
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(TreeMap.class, SortedMap.class);
   }
}//END OF TreeMapTypeConverter
