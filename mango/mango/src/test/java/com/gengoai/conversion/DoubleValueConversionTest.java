package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class DoubleValueConversionTest extends BaseNumberConversionTest {

   public DoubleValueConversionTest() {
      super(double.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.doubleValue();
   }

}//END OF DoubleValueConversionTest
