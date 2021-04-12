package com.gengoai.conversion;

import com.gengoai.tuple.Tuple4;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;

/**
 * Tuple4 Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class Tuple4TypeConverter extends Tuple2TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Tuple4) {
         Tuple4<?, ?, ?, ?> m = Cast.as(source);
         return $(Converter.convert(m.v1, getOrObject(0, parameters)),
                  Converter.convert(m.v2, getOrObject(1, parameters)),
                  Converter.convert(m.v3, getOrObject(2, parameters)),
                  Converter.convert(m.v4, getOrObject(3, parameters)));
      }
      List<?> list = createList(source, parameters);
      if (list.size() <= 4) {
         return $(getValue(0, list, parameters),
                  getValue(1, list, parameters),
                  getValue(2, list, parameters),
                  getValue(3, list, parameters));
      }
      throw new TypeConversionException(source, parameterizedType(Tuple4.class, parameters));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Tuple4.class);
   }
}//END OF TupleTypeConverter
