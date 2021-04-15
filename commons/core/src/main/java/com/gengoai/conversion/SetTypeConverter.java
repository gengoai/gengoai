package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Set and HashSet Converter.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class SetTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Set.class, HashSet.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new HashSet<>();
   }
}
