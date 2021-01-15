package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class FloatValueConversionTest extends BaseNumberConversionTest {

   public FloatValueConversionTest() {
      super(float.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.floatValue();
   }

}//END OF FloatValueConversionTest
