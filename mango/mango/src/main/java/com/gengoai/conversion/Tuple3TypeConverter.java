package com.gengoai.conversion;

import com.gengoai.tuple.Tuple3;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;

/**
 * Tuple3 Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class Tuple3TypeConverter extends Tuple2TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Tuple3) {
         Tuple3<?, ?, ?> m = Cast.as(source);
         return $(Converter.convert(m.v1, getOrObject(0, parameters)),
                  Converter.convert(m.v2, getOrObject(1, parameters)),
                  Converter.convert(m.v3, getOrObject(2, parameters)));
      }
      List<?> list = createList(source, parameters);
      if (list.size() <= 3) {
         return $(getValue(0, list, parameters),
                  getValue(1, list, parameters),
                  getValue(2, list, parameters));
      }
      throw new TypeConversionException(source, parameterizedType(Tuple3.class, parameters));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Tuple3.class);
   }
}//END OF TupleTypeConverter
