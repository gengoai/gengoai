package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.math.BigDecimal;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Converts object into BigDecimals (see {@link BaseNumberTypeConverter} for details on conversion.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class BigDecimalTypeConverter extends BaseNumberTypeConverter {

   @Override
   protected Object convertNumber(Number number) {
      if (number instanceof BigDecimal) {
         return number;
      }
      return new BigDecimal(number.doubleValue());
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(BigDecimal.class);
   }
}//END OF BigDecimalTypeConverter
