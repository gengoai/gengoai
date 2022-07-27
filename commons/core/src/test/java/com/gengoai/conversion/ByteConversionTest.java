package com.gengoai.conversion;

/**
 * @author David B. Bracewell
 */
public class ByteConversionTest extends BaseNumberConversionTest {

   public ByteConversionTest() {
      super(Byte.class);
   }

   @Override
   protected Number convert(Number in) {
      return in.byteValue();
   }

}//END OF ByteConversionTest
