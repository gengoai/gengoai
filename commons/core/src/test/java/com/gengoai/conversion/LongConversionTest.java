package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class LongConversionTest extends BaseNumberConversionTest {

   public LongConversionTest() {
      super(Long.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.longValue();
   }

}//END OF LongConversionTest
