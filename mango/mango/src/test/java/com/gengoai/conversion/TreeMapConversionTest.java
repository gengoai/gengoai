package com.gengoai.conversion;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author David B. Bracewell
 */
public class TreeMapConversionTest extends BaseMapConversionTest {
   public TreeMapConversionTest() {
      super(TreeMap.class);
   }

   @Override
   protected Map<Object, Object> newMap() {
      return new TreeMap<>();
   }
}//END OF HashMapConversionTest
