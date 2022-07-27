package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Converts object into Long (see {@link BaseNumberTypeConverter} for details on conversion.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LongTypeConverter extends BaseNumberTypeConverter {

   @Override
   protected Object convertNumber(Number number) {
      return number.longValue();
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Long.class, long.class);
   }
}//END OF FloatTypeConverter
