package com.gengoai.conversion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class LinkedHashMapConversionTest extends BaseMapConversionTest {
   public LinkedHashMapConversionTest() {
      super(LinkedHashMap.class);
   }

   @Override
   protected Map<Object, Object> newMap() {
      return new LinkedHashMap<>();
   }
}//END OF HashMapConversionTest
