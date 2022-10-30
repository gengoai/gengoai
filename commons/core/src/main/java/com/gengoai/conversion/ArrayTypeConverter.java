package com.gengoai.conversion;

import com.gengoai.reflection.TypeUtils;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;

/**
 * Type Converter to convert objects into Arrays. Uses {@link CollectionTypeConverter} to convert the object into a
 * collection and then constructs the required array. Is capable of handling primitive arrays as well as object arrays.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ArrayTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      Type componentType = getOrObject(0, parameters);
      //Convert the source to a list and create an array from it.
      List<?> list = Converter.convert(source, List.class, parameters);
      Object array = Array.newInstance(TypeUtils.asClass(componentType), list.size());
      for (int i = 0; i < list.size(); i++) {
         Array.set(array, i, list.get(i));
      }
      return array;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Object[].class);
   }
}//END OF ArrayTypeConverter
