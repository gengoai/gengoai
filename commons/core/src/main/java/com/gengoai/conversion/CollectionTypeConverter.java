package com.gengoai.conversion;

import com.gengoai.collection.Collect;
import com.gengoai.collection.Iterators;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static com.gengoai.reflection.TypeUtils.*;
import static java.util.Collections.singletonList;

/**
 * Base class for converting object into Collections. Conversion is possible for the following types:
 * <ul>
 * <li>Default: A singleton collection is created for the object passed in being converted to the collection element
 * type if possible.</li>
 * <li>{@link JsonEntry}: arrays as collection of desired type, objects as either collection map entries, or
 * attempts to create an object from the map, primitive - singleton collection</li>
 * <li>CharSequence: Try to parse as json, if that fails treat as csv with optional []</li>
 * </ul>
 *
 * @author David B. Bracewell
 */
public abstract class CollectionTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      Type elementType = (parameters == null || parameters.length == 0)
                         ? Object.class
                         : parameters[0];

      if(source instanceof JsonEntry) {
         return fromJson(source, Cast.as(source), elementType);
      }

      if(source instanceof CharSequence) {
         String str = source.toString();

         //Try Json
         try {
            return fromJson(source, Json.parse(source.toString()), elementType);
         } catch(IOException e) {
            //Ignore and try csv style conversion
         }

         //Not Json, so try CSV
         str = str.replaceFirst("^\\[", "").replaceFirst("]$", "").trim();
         List<String> strList = new ArrayList<>();
         if(isArray(elementType) || isAssignable(Collection.class, elementType)) {
            for(String s : str.split("]")) {
               s = s.replaceFirst("^\\[", "")
                    .replaceFirst("^,", "").trim();
               strList.add(s);
            }
         } else {
            strList.addAll(Strings.split(str, ','));
         }

         Collection<?> newCollection = newCollection();
         for(String s : strList) {
            newCollection.add(Converter.convert(s, elementType));
         }
         return newCollection;
      }

      Collection<?> collection = newCollection();
      for(Iterator<?> iterator = Iterators.asIterator(source); iterator.hasNext(); ) {
         collection.add(Converter.convert(iterator.next(), elementType));
      }
      return collection;
   }

   private Collection<?> fromJson(Object source, JsonEntry je, Type elementType) throws TypeConversionException {
      if(je.isArray()) {
         Collection<?> c = newCollection();
         for(Iterator<JsonEntry> itr = je.elementIterator(); itr.hasNext(); ) {
            c.add(Converter.convert(itr.next(), elementType));
         }
         return c;
      } else if(je.isObject() && isAssignable(Map.Entry.class, elementType)) {
         Collection<?> c = newCollection();
         for(Iterator<Map.Entry<String, JsonEntry>> itr = je.propertyIterator(); itr.hasNext(); ) {
            c.add(Converter.convert(itr.next(), elementType));
         }
         return c;
      } else if(je.isObject()) {
         return Collect.addAll(newCollection(), singletonList(Converter.convert(je.asMap(), elementType)));
      } else if(je.isPrimitive()) {
         return Collect.addAll(newCollection(), singletonList(je.as(elementType)));
      }
      throw new TypeConversionException(source, parameterizedType(Collection.class, elementType));
   }

   /**
    * New collection collection.
    *
    * @return the collection
    */
   protected abstract Collection<?> newCollection();

}//END OF CollectionTypeConverter
