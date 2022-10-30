package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Converts object into Integer (see {@link BaseNumberTypeConverter} for details on conversion.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class IntegerTypeConverter extends BaseNumberTypeConverter {

   @Override
   protected Object convertNumber(Number number) {
      return number.intValue();
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Integer.class, int.class);
   }
}//END OF IntegerTypeConverter
