package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class IntegerConversionTest extends BaseNumberConversionTest {

   public IntegerConversionTest() {
      super(Integer.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.intValue();
   }

}//END OF IntegerConversionTest
