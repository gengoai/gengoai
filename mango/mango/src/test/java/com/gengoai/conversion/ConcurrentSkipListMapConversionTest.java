package com.gengoai.conversion;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author David B. Bracewell
 */
public class ConcurrentSkipListMapConversionTest extends BaseMapConversionTest {
   public ConcurrentSkipListMapConversionTest() {
      super(ConcurrentSkipListMap.class);
   }

   @Override
   protected Map<Object, Object> newMap() {
      return new ConcurrentSkipListMap<>();
   }
}//END OF HashMapConversionTest
