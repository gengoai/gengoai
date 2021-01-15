package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.LinkedHashSet;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * LinkedHashSet Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LinkedHashSetTypeConverter extends CollectionTypeConverter {

   @Override
   public Class[] getConversionType() {
      return arrayOf(LinkedHashSet.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new LinkedHashSet<>();
   }
}
