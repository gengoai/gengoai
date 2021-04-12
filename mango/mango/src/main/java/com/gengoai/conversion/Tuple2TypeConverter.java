package com.gengoai.conversion;

import com.gengoai.reflection.TypeUtils;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;

/**
 * Map Entry and Tuple2 Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class Tuple2TypeConverter implements TypeConverter {

   protected Object getValue(int index, List<?> list, Type[] parameters) throws TypeConversionException {
      if (list.size() <= index) {
         return null;
      }
      return Converter.convert(list.get(index), getOrObject(index, parameters));
   }

   protected List<?> createList(Object source, Type... parameters) throws TypeConversionException {
      if (TypeUtils.asClass(getOrObject(0, parameters)).isArray()) {
         return Converter.convert(source, List.class, Object[].class);
      }
      return Converter.convert(source, List.class);
   }

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Map.Entry) {
         Map.Entry<?, ?> m = Cast.as(source);
         return $(Converter.convert(m.getKey(), getOrObject(0, parameters)),
                  Converter.convert(m.getValue(), getOrObject(1, parameters)));
      }
      List<?> list = createList(source, parameters);
      if (list.size() <= 2) {
         return $(getValue(0, list, parameters),
                  getValue(1, list, parameters));
      }
      throw new TypeConversionException(source, parameterizedType(Map.Entry.class, parameters));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Map.Entry.class, Tuple2.class);
   }
}//END OF TupleTypeConverter
