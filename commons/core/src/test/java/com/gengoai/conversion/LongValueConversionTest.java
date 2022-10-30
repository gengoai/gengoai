package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class LongValueConversionTest extends BaseNumberConversionTest {

   public LongValueConversionTest() {
      super(long.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.longValue();
   }

}//END OF LongValueConversionTest
