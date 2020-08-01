package com.gengoai.json;

import java.io.Closeable;
import java.io.IOException;

/**
 * The type Json reader.
 *
 * @author David B. Bracewell
 */
public class JsonReader implements AutoCloseable, Closeable {
   @Override
   public void close() throws IOException {

   }
   //   private final JsonReade reader;
//   private boolean isArray = false;
//
//   /**
//    * Instantiates a new Json reader.
//    *
//    * @param resource the resource
//    * @throws IOException the io exception
//    */
//   public JsonReader(Resource resource) throws IOException {
//      this.reader = new com.google.gson.stream.JsonReader(resource.reader());
//   }
//
//   public JsonReader(com.google.gson.stream.JsonReader resource) throws IOException {
//      this.reader = resource;
//   }
//
//
//   /**
//    * Has next boolean.
//    *
//    * @return the boolean
//    * @throws IOException the io exception
//    */
//   public boolean hasNext() throws IOException {
//      return reader.hasNext() && peek() != JsonToken.END_DOCUMENT;
//   }
//
//   /**
//    * Begin object json reader.
//    *
//    * @return the json reader
//    * @throws IOException the io exception
//    */
//   public JsonReader beginObject() throws IOException {
//      reader.beginObject();
//      return this;
//   }
//
//   /**
//    * Begin object json reader.
//    *
//    * @param name the name
//    * @return the json reader
//    * @throws IOException the io exception
//    */
//   public JsonReader beginObject(String name) throws IOException {
//      String nextName = nextName();
//      checkState(nextName.equals(name), "Next Object is named (" +
//         nextName + "), but was expecting (" + name + ")");
//      reader.beginObject();
//      return this;
//   }
//
//   /**
//    * Begin array json reader.
//    *
//    * @return the json reader
//    * @throws IOException the io exception
//    */
//   public JsonReader beginArray() throws IOException {
//      reader.beginArray();
//      return this;
//   }
//
//   /**
//    * Begin array json reader.
//    *
//    * @param name the name
//    * @return the json reader
//    * @throws IOException the io exception
//    */
//   public JsonReader beginArray(String name) throws IOException {
//      String nextName = nextName();
//      checkState(nextName.equals(name), "Next Array is named (" +
//         nextName + "), but was expecting (" + name + ")");
//      reader.beginArray();
//      return this;
//   }
//
//   /**
//    * Begin document json token.
//    *
//    * @return the json token
//    * @throws IOException the io exception
//    */
//   public JsonToken beginDocument() throws IOException {
//      if (peek() == JsonToken.BEGIN_OBJECT) {
//         reader.beginObject();
//         return JsonToken.BEGIN_OBJECT;
//      } else if (peek() == JsonToken.BEGIN_ARRAY) {
//         reader.beginArray();
//         isArray = true;
//         return JsonToken.BEGIN_ARRAY;
//      }
//      throw new IOException("Unable to start document");
//   }
//
//   public void endArray() throws IOException {
//      reader.endArray();
//   }
//
//   public void endObject() throws IOException {
//      reader.endObject();
//   }
//
//   /**
//    * End document.
//    *
//    * @throws IOException the io exception
//    */
//   public void endDocument() throws IOException {
//      if (peek() != JsonToken.END_DOCUMENT && (isArray && peek() != JsonToken.END_ARRAY)) {
//         throw new IOException("Unable to end document " + peek());
//      }
//   }
//
//   /**
//    * Next map map.
//    *
//    * @return the map
//    * @throws IOException the io exception
//    */
//   public Map<String, JsonEntry> nextMap() throws IOException {
//      return nextMap(null);
//   }
//
//   /**
//    * Next map map.
//    *
//    * @param name the name
//    * @return the map
//    * @throws IOException the io exception
//    */
//   public Map<String, JsonEntry> nextMap(String name) throws IOException {
//      if (Strings.isNotNullOrBlank(name) || peek() == JsonToken.NAME) {
//         String nextName = nextName();
//         checkState(Strings.isNullOrBlank(name) || nextName.equals(name), "Next Map is named (" +
//            nextName + "), but was expecting (" + name + ")");
//      }
//      return nextElement().getAsMap();
//   }
//
//
//   /**
//    * Next array list.
//    *
//    * @return the list
//    * @throws IOException the io exception
//    */
//   public List<JsonEntry> nextArray() throws IOException {
//      return nextArray(null);
//   }
//
//   /**
//    * Next array list.
//    *
//    * @param name the name
//    * @return the list
//    * @throws IOException the io exception
//    */
//   public List<JsonEntry> nextArray(String name) throws IOException {
//      if (Strings.isNotNullOrBlank(name) || peek() == JsonToken.NAME) {
//         String nextName = nextName();
//         checkState(Strings.isNullOrBlank(name) || nextName.equals(name), "Next Collection is named (" +
//            nextName + "), but was expecting (" + name + ")");
//      }
//      return nextElement().getAsArray();
//   }
//
//   /**
//    * Next property tuple 2.
//    *
//    * @return the tuple 2
//    * @throws IOException the io exception
//    */
//   public Tuple2<String, Val> nextProperty() throws IOException {
//      return $(nextName(), nextValue());
//   }
//
//   public <T> Tuple2<String, T> nextProperty(Type clazz) throws IOException {
//      return $(nextName(), nextValue(clazz));
//   }
//
//   /**
//    * Next boolean property tuple 2.
//    *
//    * @return the tuple 2
//    * @throws IOException the io exception
//    */
//   public Tuple2<String, Boolean> nextBooleanProperty() throws IOException {
//      return $(nextName(), nextBoolean());
//   }
//
//   /**
//    * Next string property tuple 2.
//    *
//    * @return the tuple 2
//    * @throws IOException the io exception
//    */
//   public Tuple2<String, String> nextStringProperty() throws IOException {
//      return $(nextName(), nextString());
//   }
//
//   /**
//    * Next double property tuple 2.
//    *
//    * @return the tuple 2
//    * @throws IOException the io exception
//    */
//   public Tuple2<String, Double> nextDoubleProperty() throws IOException {
//      return $(nextName(), nextDouble());
//   }
//
//   /**
//    * Next long property tuple 2.
//    *
//    * @return the tuple 2
//    * @throws IOException the io exception
//    */
//   public Tuple2<String, Long> nextLongProperty() throws IOException {
//      return $(nextName(), nextLong());
//   }
//
//   /**
//    * Next element property tuple 2.
//    *
//    * @return the tuple 2
//    * @throws IOException the io exception
//    */
//   public Tuple2<String, JsonEntry> nextElementProperty() throws IOException {
//      return $(nextName(), nextElement());
//   }
//
//
//   public <T> T nextValue(Type clazz) throws IOException {
//      return nextElement().as(clazz);
//   }
//
//   /**
//    * Next value val.
//    *
//    * @return the val
//    * @throws IOException the io exception
//    */
//   public Val nextValue() throws IOException {
//      return nextElement().getAsVal();
//   }
//
//   /**
//    * Peek json token.
//    *
//    * @return the json token
//    * @throws IOException the io exception
//    */
//   public JsonToken peek() throws IOException {
//      return reader.peek();
//   }
//
//   /**
//    * Next boolean boolean.
//    *
//    * @return the boolean
//    * @throws IOException the io exception
//    */
//   public boolean nextBoolean() throws IOException {
//      return reader.nextBoolean();
//   }
//
//   /**
//    * Next double double.
//    *
//    * @return the double
//    * @throws IOException the io exception
//    */
//   public double nextDouble() throws IOException {
//      return reader.nextDouble();
//   }
//
//   /**
//    * Next long long.
//    *
//    * @return the long
//    * @throws IOException the io exception
//    */
//   public long nextLong() throws IOException {
//      return reader.nextLong();
//   }
//
//   /**
//    * Next name string.
//    *
//    * @return the string
//    * @throws IOException the io exception
//    */
//   public String nextName() throws IOException {
//      return reader.nextName();
//   }
//
//   /**
//    * Next string string.
//    *
//    * @return the string
//    * @throws IOException the io exception
//    */
//   public String nextString() throws IOException {
//      return reader.nextString();
//   }
//
//   /**
//    * Next element json element.
//    *
//    * @return the json element
//    * @throws IOException the io exception
//    */
//   public JsonEntry nextElement() throws IOException {
//      JsonToken nextToken = reader.peek();
//      switch (nextToken) {
//         case NUMBER:
//            String number = reader.nextString();
//            return JsonEntry.from(new JsonPrimitive(new LazilyParsedNumber(number)));
//         case BOOLEAN:
//            return JsonEntry.from(new JsonPrimitive(reader.nextBoolean()));
//         case STRING:
//            return JsonEntry.from(new JsonPrimitive(reader.nextString()));
//         case NULL:
//            reader.nextNull();
//            return JsonEntry.nullValue();
//         case BEGIN_ARRAY:
//            JsonEntry array = JsonEntry.array();
//            reader.beginArray();
//
//            while (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
//               array.addValue(nextElement());
//            }
//
//            reader.endArray();
//            return array;
//         case BEGIN_OBJECT:
//            JsonEntry object = JsonEntry.object();
//            reader.beginObject();
//
//            while (reader.hasNext() && reader.peek() != JsonToken.END_OBJECT) {
//               object.addProperty(reader.nextName(), nextElement());
//            }
//            reader.endObject();
//            return object;
//         case NAME:
//            return JsonEntry.object().addProperty(reader.nextName(), nextElement());
//         case END_OBJECT:
//         case END_ARRAY:
//         case END_DOCUMENT:
//         default:
//            throw new IllegalArgumentException(nextToken + " is invalid");
//      }
//   }
//
//   @Override
//   public void close() throws IOException {
//      reader.close();
//   }
}//END OF JsonReader2
