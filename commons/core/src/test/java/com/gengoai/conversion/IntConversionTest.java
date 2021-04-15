package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class IntConversionTest extends BaseNumberConversionTest {

   public IntConversionTest() {
      super(int.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.intValue();
   }

}//END OF IntConversionTest
