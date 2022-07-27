package com.gengoai.conversion;

import java.math.BigInteger;

/**
 * @author David B. Bracewell
 */
public class BigIntegerConversionTest extends BaseNumberConversionTest {

   public BigIntegerConversionTest() {
      super(BigInteger.class);
   }

   @Override
   protected Number convert(Number in) {
      return new BigInteger(Long.toString(in.longValue()));
   }

}//END OF FloatConversionTest
