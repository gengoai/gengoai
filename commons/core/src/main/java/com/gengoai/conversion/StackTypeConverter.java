package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.Stack;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Stack Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class StackTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Stack.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new Stack<>();
   }
}
