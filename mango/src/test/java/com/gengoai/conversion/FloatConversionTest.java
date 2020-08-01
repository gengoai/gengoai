package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class FloatConversionTest extends BaseNumberConversionTest {

   public FloatConversionTest() {
      super(Float.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.floatValue();
   }

}//END OF FloatConversionTest
