package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * LinkedBlockingDeque Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LinkedBlockingDequeTypeConverter extends CollectionTypeConverter {

   @Override
   public Class[] getConversionType() {
      return arrayOf(LinkedBlockingDeque.class, BlockingDeque.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new LinkedBlockingDeque<>();
   }
}
