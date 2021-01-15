package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class ShortValueConversionTest extends BaseNumberConversionTest {

   public ShortValueConversionTest() {
      super(short.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.shortValue();
   }

}//END OF ShortValueConversionTest
