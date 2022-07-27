package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class ShortConversionTest extends BaseNumberConversionTest {

   public ShortConversionTest() {
      super(Short.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.shortValue();
   }

}//END OF ShortConversionTest
