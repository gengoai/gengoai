package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * SortedSet and TreeSet Converter.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class TreeSetTypeConverter extends CollectionTypeConverter {

   @Override
   public Class[] getConversionType() {
      return arrayOf(SortedSet.class, TreeSet.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new TreeSet<>();
   }
}
