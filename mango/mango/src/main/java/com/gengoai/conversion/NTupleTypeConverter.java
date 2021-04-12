package com.gengoai.conversion;

import com.gengoai.tuple.NTuple;
import com.gengoai.tuple.Tuple;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;

/**
 * NTuple and Tuple Converter.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class NTupleTypeConverter extends Tuple2TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      List<?> list = createList(source, parameters);
      List<?> conv = new ArrayList<>();
      for (int i = 0; i < list.size(); i++) {
         conv.add(Converter.convert(list.get(i), getOrObject(i, parameters)));
      }
      return NTuple.of(conv);
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(NTuple.class, Tuple.class);
   }

}//END OF NTupleTypeConverter
