package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class ByteValueConversionTest extends BaseNumberConversionTest {

   public ByteValueConversionTest() {
      super(byte.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.byteValue();
   }

}//END OF ByteValueConversionTest
