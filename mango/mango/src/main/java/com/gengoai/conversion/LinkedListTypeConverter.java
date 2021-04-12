package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * LinkedList Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LinkedListTypeConverter extends CollectionTypeConverter {

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(LinkedList.class, Deque.class, Queue.class);
   }

   @Override
   protected Collection<?> newCollection() {
      return new LinkedList<>();
   }
}
