package com.gengoai.conversion;

import java.util.Collection;
import java.util.Stack;

/**
 * @author David B. Bracewell
 */
public class StackConversionTest extends BaseCollectionConversionTest {

   public StackConversionTest() {
      super(Stack.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      Stack<Object> stack = new Stack<>();
      for (Object item : items) {
         stack.push(item);
      }
      return stack;
   }
}
