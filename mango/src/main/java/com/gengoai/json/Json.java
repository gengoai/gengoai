package com.gengoai.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.reflection.TypeUtils;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * <p>Convenience methods for serializing and deserializing objects to and from json and creating json reader and
 * writers.</p>
 *
 * @author David B. Bracewell
 */
public final class Json {

   public static final ObjectMapper MAPPER = new ObjectMapper()
         .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

   static {
      MAPPER.registerModule(new Jdk8Module());
      MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      MAPPER.addMixIn(Type.class, Mixins.TypeMixin.class);
      MAPPER.addMixIn(ParameterizedType.class, Mixins.TypeMixin.class);
   }

   public static JsonEntry asJsonEntry(Object o) {
      return JsonEntry.from(o);
   }

   /**
    * Creates a <code>JsonReader</code> to read from the given resource.
    *
    * @param resource the resource to read
    * @return the json reader
    * @throws IOException Something went wrong creating the reader
    */
   public static JsonReader createReader(@NonNull Resource resource) throws IOException {
      return null;// new JsonReader(resource);
   }

   /**
    * Creates a <code>JsonWriter</code> to write to the given resource.
    *
    * @param resource the resource
    * @return the json writer
    * @throws IOException something went wrong creating the writer
    */
   public static JsonWriter createWriter(@NonNull Resource resource) throws IOException {
      return null;// new JsonWriter(resource);
   }

   /**
    * Dumps the given object to the given output location in json format.
    *
    * @param object   the object to dump
    * @param resource the resource to write the dumped object in json format to.
    * @return the resource
    * @throws IOException Something went wrong writing to the given resource
    */
   public static Resource dump(Object object, @NonNull Resource resource) throws IOException {
      try(Writer writer = resource.writer()) {
         if(object instanceof JsonEntry) {
            MAPPER.writeValue(writer, ((JsonEntry) object).getElement());
         } else {
            MAPPER.writeValue(writer, object);
         }
      } catch(JsonProcessingException e) {
         throw new RuntimeException(e);
      }
      return resource;
   }

   /**
    * Dumps the given object to the given output location in json format.
    *
    * @param object   the object to dump
    * @param resource the resource to write the dumped object in json format to.
    * @return the resource
    * @throws IOException Something went wrong writing to the given resource
    */
   public static Resource dumpPretty(Object object, @NonNull Resource resource) throws IOException {
      try(Writer writer = resource.writer()) {
         if(object instanceof JsonEntry) {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, ((JsonEntry) object).getElement());
         } else {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, object);
         }
      } catch(JsonProcessingException e) {
         throw new RuntimeException(e);
      }
      return resource;
   }

   /**
    * Dumps the given object to a string in json format.
    *
    * @param object the object to dump
    * @return the object as a json string
    */
   public static String dumps(Object object) {
      try {
         if(object instanceof JsonEntry) {
            return MAPPER.writeValueAsString(((JsonEntry) object).getElement());
         }
         return MAPPER.writeValueAsString(object);
      } catch(JsonProcessingException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Dumps the given object to a string in json format.
    *
    * @param object the object to dump
    * @return the object as a json string
    */
   public static String dumpsPretty(Object object) {
      try {
         if(object instanceof JsonEntry) {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(((JsonEntry) object).getElement());
         }
         return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
      } catch(JsonProcessingException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Parses the json string.
    *
    * @param json the json string to parse
    * @return the parsed string as a json entry
    * @throws IOException something went wrong parsing the json.
    */
   public static JsonEntry parse(String json) throws IOException {
      return parse(Resources.fromString(json));
   }

   /**
    * Parses the json in the given resource creating an object of the given class type.
    *
    * @param <T>  the class type parameter
    * @param json the json to parse
    * @param type the class information for the object to deserialized
    * @return the deserialized object
    * @throws IOException something went wrong reading the json
    */
   public static <T> T parse(@NonNull String json, @NonNull Type type) throws IOException {
      try {
         if(type instanceof ParameterizedType) {
            ParameterizedType pt = Cast.as(type);
            Class<?> baseClass = TypeUtils.asClass(pt.getRawType());
            Class<?>[] params = new Class[pt.getActualTypeArguments().length];
            for(int i = 0; i < params.length; i++) {
               params[i] = TypeUtils.asClass(pt.getActualTypeArguments()[i]);
            }
            return MAPPER.readValue(json, TypeFactory.defaultInstance()
                                                     .constructParametricType(baseClass, params));
         }
         return MAPPER.readValue(json, TypeUtils.asClass(type));
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Parses the given resource as json entry
    *
    * @param json the resource to read from
    * @return the parsed resource as a json entry
    * @throws IOException Something went wrong parsing the resource
    */
   public static JsonEntry parse(@NonNull Resource json) throws IOException {
      try(Reader reader = json.reader()) {
         return new JsonEntry(MAPPER.readTree(reader));
      }
   }

   /**
    * Parses the json in the given resource creating an object of the given class type.
    *
    * @param <T>      the class type parameter
    * @param resource the resource to read from
    * @param type     the class information for the object to deserialized
    * @return the deserialized object
    * @throws IOException something went wrong reading the json
    */
   public static <T> T parse(@NonNull Resource resource, @NonNull Type type) throws IOException {
      return parse(resource.readToString(), type);
   }

   /**
    * Loads an array of objects from the given resource in json format.
    *
    * @param resource the resource to read from
    * @return the list of objects read in from the resource
    * @throws IOException Something went wrong reading from the resource
    */
   public static List<JsonEntry> parseArray(@NonNull Resource resource) throws IOException {
      return parse(resource).asArray();
   }

   /**
    * Parses a json string into a list of {@link JsonEntry}
    *
    * @param json the json string to load the array from
    * @return the list of objects parsed from the string
    * @throws IOException Something went wrong parsing the json string
    */
   public static List<JsonEntry> parseArray(@NonNull String json) throws IOException {
      return parse(json).asArray();
   }

   /**
    * Quicker method for parsing a json string into a <code>Map</code> of <code>String</code> keys and
    * <code>Object</code> values.
    *
    * @param json the json to load from
    * @return the map of representing the json object in the string
    * @throws IOException Something went wrong parsing the json in the resource
    */
   public static Map<String, JsonEntry> parseObject(Resource json) throws IOException {
      return parse(json).asMap();
   }

   /**
    * Quicker method for parsing a json string into a <code>Map</code> of <code>String</code> keys and
    * <code>Object</code> values.
    *
    * @param json the json to load from
    * @return the map of representing the json object in the string
    * @throws IOException Something went wrong parsing the json string
    */
   public static Map<String, JsonEntry> parseObject(String json) throws IOException {
      return parseObject(Resources.fromString(json));
   }

   public static JavaType typeToJavaType(Type type) {
      if(type instanceof ParameterizedType) {
         ParameterizedType pt = Cast.as(type);
         Class<?> baseClass = TypeUtils.asClass(pt.getRawType());
         Class<?>[] params = new Class[pt.getActualTypeArguments().length];
         for(int i = 0; i < params.length; i++) {
            params[i] = TypeUtils.asClass(pt.getActualTypeArguments()[i]);
         }
         return TypeFactory.defaultInstance().constructParametricType(baseClass, params);
      }
      return TypeFactory.defaultInstance().constructType(type);
   }

   private Json() {
      throw new IllegalAccessError();
   }

}//END OF Json
