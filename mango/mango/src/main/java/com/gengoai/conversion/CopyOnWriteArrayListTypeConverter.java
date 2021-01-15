package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * CopyOnWriteArrayList Converter
 *
 * @author David Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class CopyOnWriteArrayListTypeConverter extends CollectionTypeConverter {

   @Override
   public Class[] getConversionType() {
      return arrayOf(CopyOnWriteArrayList.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new CopyOnWriteArrayList<>();
   }
}
