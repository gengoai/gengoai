package com.gengoai.conversion;

import com.gengoai.tuple.Tuple1;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;

/**
 * Tuple1 Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class Tuple1TypeConverter extends Tuple2TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Tuple1) {
         Tuple1<?> m = Cast.as(source);
         return $(Converter.<Object>convert(m.v1, getOrObject(0, parameters)));
      }
      List<?> list = createList(source, parameters);
      if (list.size() <= 1) {
         return $(getValue(0, list, parameters));
      }
      throw new TypeConversionException(source, parameterizedType(Tuple1.class, parameters));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Tuple1.class);
   }
}//END OF TupleTypeConverter
