package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class DoubleConversionTest extends BaseNumberConversionTest {

   public DoubleConversionTest() {
      super(Double.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.doubleValue();
   }

}//END OF DoubleConversionTest
