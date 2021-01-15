package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * CopyOnWriteArraySet Converter
 *
 * @author David Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class CopyOnWriteArraySetTypeConverter extends CollectionTypeConverter {

   @Override
   public Class[] getConversionType() {
      return arrayOf(CopyOnWriteArraySet.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new CopyOnWriteArraySet<>();
   }
}
