package com.gengoai.conversion;

import com.gengoai.collection.Iterators;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * Base Map type converter. Handles Maps, JsonEntries, CharSequences, and other objects that can be iterated over.
 *
 * @author David B. Bracewell
 */
public abstract class MapTypeConverter implements TypeConverter {


   private Map<?, ?> convertJson(JsonEntry json,
                                 Object source,
                                 Type keyType,
                                 Type valueType) throws TypeConversionException {
      Map<?, ?> map = createMap();
      if (!json.isObject()) {
         throw new TypeConversionException(source, parameterizedType(Map.class, keyType, valueType));
      }
      try {
         for (Iterator<Map.Entry<String, JsonEntry>> itr = json.propertyIterator(); itr.hasNext(); ) {
            Map.Entry<String, JsonEntry> entry = itr.next();
            map.put(Converter.convert(entry.getKey(), keyType),
                    entry.getValue().as(valueType));
         }
      } catch (Exception e) {
         throw new TypeConversionException(source, parameterizedType(Map.class, keyType, valueType), e.getCause());
      }
      return map;
   }

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      Type keyType = (parameters == null || parameters.length == 0) ? Object.class : parameters[0];
      Type valueType = (parameters == null || parameters.length < 2) ? Object.class : parameters[1];


      //In the case that the source object is a map, convert its keys and values
      if (source instanceof Map) {
         Map<?, ?> map = createMap();
         for (Map.Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
            map.put(Converter.convert(entry.getKey(), keyType),
                    Converter.convert(entry.getValue(), valueType));
         }
         return map;
      }


      if (source instanceof CharSequence) {
         String str = source.toString();
         //Empty String
         if (Strings.isNullOrBlank(str)) {
            return createMap();
         }

         //JSON
         if (str.startsWith("{")) {
            try {
               return convertJson(Json.parse(str), str, keyType, valueType);
            } catch (IOException e) {
               throw new TypeConversionException(str, parameterizedType(Map.class, keyType, valueType), e);
            }
         }

         //Alt. form Key=Value, Key=Value
         Map<?, ?> map = createMap();
         for (String entry : Strings.split(source.toString()
                                                 .replaceFirst("^\\[", "")
                                                 .replaceFirst("]$", ""),
                                           ',')) {
            List<String> keyValue = Strings.split(entry.trim(), '=');
            if (keyValue.size() != 2) {
               throw new TypeConversionException(source, Map.class);
            }
            map.put(Converter.convert(keyValue.get(0), keyType),
                    Converter.convert(keyValue.get(1), valueType));
         }
         return map;
      }

      //CharSequences should be JSON format, so process it with JsonEntry
      if (source instanceof JsonEntry) {
         return convertJson(Cast.as(source), source, keyType, valueType);
      }

      //Last chance is to try and convert the source into an iterable and process the times in the iterable as key value pairs.
      Map<?, ?> map = createMap();
      for (Iterator<?> iterator = Iterators.asIterator(source); iterator.hasNext(); ) {
         Object o = iterator.next();
         Object key;
         Object value;
         if (o instanceof Map.Entry) {
            key = Cast.<Map.Entry<?,?>>as(o).getKey();
            value = Cast.<Map.Entry<?,?>>as(o).getValue();
         } else {
            key = o;
            if (!iterator.hasNext()) {
               throw new TypeConversionException(source, parameterizedType(Map.class, keyType, valueType));
            }
            value = iterator.next();
         }
         map.put(Converter.convert(key, keyType), Converter.convert(value, valueType));
      }
      return map;
   }

   public abstract Map<?, ?> createMap();

}//END OF MapTypeConverter
