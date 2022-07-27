package com.gengoai.conversion;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author David B. Bracewell
 */
public class WeakHashMapConversionTest extends BaseMapConversionTest {
   public WeakHashMapConversionTest() {
      super(WeakHashMap.class);
   }

   @Override
   protected Map<Object, Object> newMap() {
      return new WeakHashMap<>();
   }
}//END OF HashMapConversionTest
