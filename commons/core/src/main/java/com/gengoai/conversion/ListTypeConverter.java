package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * List, ArrayList, Iterable, and Collection Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ListTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(List.class, ArrayList.class, Iterable.class, Collection.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new ArrayList<>();
   }
}
