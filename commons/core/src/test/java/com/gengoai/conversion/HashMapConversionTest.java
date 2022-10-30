package com.gengoai.conversion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class HashMapConversionTest extends BaseMapConversionTest {
   public HashMapConversionTest() {
      super(HashMap.class);
   }

   @Override
   protected Map<Object, Object> newMap() {
      return new HashMap<>();
   }
}//END OF HashMapConversionTest
