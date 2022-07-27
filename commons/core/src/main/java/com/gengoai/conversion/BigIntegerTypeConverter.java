package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.math.BigInteger;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Converts object into BigInteger (see {@link BaseNumberTypeConverter} for details on conversion.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class BigIntegerTypeConverter extends BaseNumberTypeConverter {

   @Override
   protected Object convertNumber(Number number) {
      if(number instanceof BigInteger) {
         return number;
      }
      return BigInteger.valueOf(number.longValue());
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(BigInteger.class);
   }
}//END OF BigIntegerTypeConverter
