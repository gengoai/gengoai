package com.gengoai.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gengoai.collection.IteratorSet;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Lists;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.stream.Streams;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gengoai.json.Json.MAPPER;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>A convenience wrapper around <code>JsonElement</code> allowing a single interface for objects, arrays, and
 * primitives</p>
 */
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class JsonEntry implements Serializable {
   @JsonValue
   private JsonNode element;

   /**
    * Array json entry.
    *
    * @param items the items
    * @return the json entry
    */
   public static JsonEntry array(Iterable<?> items) {
      JsonEntry entry = new JsonEntry(JsonNodeFactory.instance.arrayNode());
      items.forEach(entry::addValue);
      return entry;
   }

   /**
    * Creates a new array
    *
    * @param items Items to add to the array
    * @return the json entry
    */
   public static JsonEntry array(Object... items) {
      JsonEntry entry = new JsonEntry(JsonNodeFactory.instance.arrayNode());
      if(items != null) {
         for(Object item : items) {
            entry.addValue(item);
         }
      }
      return entry;
   }

   /**
    * Creates an entry from the given object.
    *
    * @param v the value to create the entry from.
    * @return the json entry
    */
   public static JsonEntry from(Object v) {
      return new JsonEntry(toElement(v));
   }

   /**
    * Creates a null valued entry
    *
    * @return the null valued entry
    */
   public static JsonEntry nullValue() {
      return new JsonEntry(JsonNodeFactory.instance.nullNode());
   }

   /**
    * Creates a new empty object
    *
    * @return the json entry
    */
   public static JsonEntry object() {
      return new JsonEntry(JsonNodeFactory.instance.objectNode());
   }

   private static JsonNode toElement(Object v) {
      if(v == null) {
         return JsonNodeFactory.instance.nullNode();
      }
      return MAPPER.valueToTree(v);
   }

   @JsonCreator
   public JsonEntry(@JsonProperty @NonNull JsonNode element) {
      this.element = element;
   }

   /**
    * Adds a property to the entry checking that it is a json object
    *
    * @param name  the property name
    * @param value the property value
    * @return this json entry
    * @throws IllegalStateException if the entry's element is not a json object
    */
   public JsonEntry addProperty(@NonNull String name, Object value) {
      ObjectNode n = asObjectNode();
      if(value == null) {
         n.set(name, JsonNodeFactory.instance.nullNode());
      } else if(value instanceof String) {
         n.put(name, (String) value);
      } else if(value instanceof Integer) {
         n.put(name, (Integer) value);
      } else if(value instanceof Double) {
         n.put(name, (Double) value);
      } else if(value instanceof Long) {
         n.put(name, (Long) value);
      } else if(value instanceof Float) {
         n.put(name, (Float) value);
      } else if(value instanceof Short) {
         n.put(name, (Short) value);
      } else if(value instanceof Byte) {
         n.put(name, (Byte) value);
      } else if(value instanceof byte[]) {
         n.put(name, (byte[]) value);
      } else if(value instanceof Boolean) {
         n.put(name, (Boolean) value);
      } else if(value instanceof BigDecimal) {
         n.put(name, (BigDecimal) value);
      } else if(value instanceof BigInteger) {
         n.put(name, (BigInteger) value);
      } else if(value.getClass().isArray()) {
         ArrayNode array = n.putArray(name);
         int length = Array.getLength(value);
         for(int i = 0; i < length; i++) {
            addValue(array, Array.get(value, i));
         }
      } else if(value instanceof JsonEntry) {
         n.set(name, Cast.<JsonEntry>as(value).element);
      } else if(value instanceof JsonNode) {
         n.set(name, (JsonNode) value);
      } else {
         n.putPOJO(name, value);
      }
      return this;
   }

   private JsonEntry addValue(ArrayNode n, Object o) {
      if(o == null) {
         n.add(JsonNodeFactory.instance.nullNode());
      } else if(o instanceof String) {
         n.add((String) o);
      } else if(o instanceof Integer) {
         n.add((Integer) o);
      } else if(o instanceof Double) {
         n.add((Double) o);
      } else if(o instanceof Long) {
         n.add((Long) o);
      } else if(o instanceof Float) {
         n.add((Float) o);
      } else if(o instanceof Short) {
         n.add((Short) o);
      } else if(o instanceof Byte) {
         n.add((Byte) o);
      } else if(o instanceof byte[]) {
         n.add((byte[]) o);
      } else if(o instanceof Boolean) {
         n.add((Boolean) o);
      } else if(o instanceof BigDecimal) {
         n.add((BigDecimal) o);
      } else if(o instanceof BigInteger) {
         n.add((BigInteger) o);
      } else if(o.getClass().isArray()) {
         int length = Array.getLength(o);
//         ArrayNode array = JsonNodeFactory.instance.arrayNode(length);
         for(int i = 0; i < length; i++) {
            addValue(n, Array.get(o, i));
         }
      } else if(o instanceof JsonEntry) {
         n.add(Cast.<JsonEntry>as(o).element);
      } else if(o instanceof JsonNode) {
         n.add((JsonNode) o);
      } else {
         n.addPOJO(o);
      }
      return this;
   }

   /**
    * Adds a value to the entry checking that it is a json array
    *
    * @param value the value
    * @return this json entry
    * @throws IllegalStateException if the entry's element is not a json array
    */
   public JsonEntry addValue(Object value) {
      addValue(asArrayNode(), value);
      return this;
   }

   /**
    * Gets the value of this entry as the given class.
    *
    * @param <T>  the type parameter
    * @param type the type information for the type to be generated
    * @return the value
    */
   @SneakyThrows
   public <T> T as(@NonNull Class<T> type) {
      return Json.parse(element.toString(), type);
   }

   /**
    * Gets the value of this entry as the given class.
    *
    * @param <T>  the type parameter
    * @param type the type information for the type to be generated
    * @return the value
    */
   @SneakyThrows
   public <T> T as(@NonNull Type type) {
      return Json.parse(toString(), type);
   }

   /**
    * Converts the entry into a list of elements checking if the underlying entry is a json array.
    *
    * @return the list
    * @throws IllegalStateException if the entry's element is not a json array
    */
   public List<JsonEntry> asArray() {
      return new ElementList(asArrayNode());
   }

   /**
    * Converts the entry into a list of elements checking if the underlying entry is a json array.
    *
    * @param <T>   the type parameter
    * @param clazz the clazz
    * @return the list
    * @throws IllegalStateException if the entry's element is not a json array
    */
   public <T> List<T> asArray(@NonNull Class<? extends T> clazz) {
      return Lists.transform(asArray(), entry -> entry.as(clazz));
   }

   /**
    * Converts the entry into a list of elements checking if the underlying entry is a json array.
    *
    * @param <T>   the type parameter
    * @param clazz the clazz
    * @return the list
    * @throws IllegalStateException if the entry's element is not a json array
    */
   public <T> List<T> asArray(@NonNull Type clazz) {
      return Lists.transform(asArray(), entry -> entry.as(clazz));
   }

   public <T, E extends Collection<T>> E asArray(@NonNull Class<? extends T> clazz, @NonNull E collection) {
      elementIterator().forEachRemaining(je -> collection.add(je.as(clazz)));
      return collection;
   }

   private ArrayNode asArrayNode() {
      return Cast.as(element, ArrayNode.class);
   }

   /**
    * Gets this entry as a boolean value.
    *
    * @return the as boolean value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public boolean asBoolean() {
      return element.asBoolean();
   }

   public boolean[] asBooleanArray() {
      return as(boolean[].class);
   }

   /**
    * Gets this entry as a byte value.
    *
    * @return the as byte value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public byte asByte() {
      return (byte) element.asInt();
   }

   /**
    * Gets this entry as a Character value.
    *
    * @return the as Character value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public Character asCharacter() {
      return element.asText().charAt(0);
   }

   /**
    * Gets this entry as a double value.
    *
    * @return the as double value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public double asDouble() {
      return element.doubleValue();
   }

   public double[] asDoubleArray() {
      return as(double[].class);
   }

   /**
    * Gets this entry as a float value.
    *
    * @return the as float value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public float asFloat() {
      return (float) element.doubleValue();
   }

   /**
    * Gets this entry as a int value.
    *
    * @return the as int value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public int asInt() {
      return element.intValue();
   }

   public int[] asIntArray() {
      return as(int[].class);
   }

   /**
    * Gets this entry as a long value.
    *
    * @return the as long value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public long asLong() {
      return element.longValue();
   }

   /**
    * Converts the entry into a map of string keys and entry elements checking if the underlying entry is a json
    * object.
    *
    * @return the map
    * @throws IllegalStateException if the entry's element is not a json object
    */
   public Map<String, JsonEntry> asMap() {
      return new ElementMap(asObjectNode());
   }

   /**
    * Converts the entry into a map of string keys and entry elements checking if the underlying entry is a json
    * object.
    *
    * @param <T>   the type parameter
    * @param clazz the clazz
    * @return the map
    * @throws IllegalStateException if the entry's element is not a json object
    */
   public <T> Map<String, T> asMap(@NonNull Class<? extends T> clazz) {
      Map<String, T> map = new HashMap<>();
      propertyIterator().forEachRemaining(e -> map.put(e.getKey(), e.getValue().as(clazz)));
      return map;
   }

   /**
    * Gets this entry as a Number value.
    *
    * @return the as Number value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public Number asNumber() {
      return element.asDouble();
   }

   private ObjectNode asObjectNode() {
      return Cast.as(element, ObjectNode.class);
   }

   /**
    * Gets this entry as a short value.
    *
    * @return the as short value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public short asShort() {
      return (short) element.asInt();
   }

   /**
    * Gets this entry as a String value.
    *
    * @return the as String value
    * @throws IllegalStateException if the entry's element is not a json primitive
    */
   public String asString() {
      if(element.isNull()) {
         return null;
      }
      return element.textValue();
   }

   /**
    * Gets this entry as a Val value.
    *
    * @return the as Val value
    */
   public Val asVal() {
      if(element.isNull()) {
         return Val.NULL;
      } else if(element.isTextual()) {
         return Val.of(element.textValue());
      } else if(element.isInt() || element.isShort()) {
         return Val.of(element.intValue());
      } else if(element.isLong()) {
         return Val.of(element.longValue());
      } else if(element.isNumber()) {
         return Val.of(element.doubleValue());
      } else if(element.isBoolean()) {
         return Val.of(element.booleanValue());
      } else if(element.isArray()) {
         return Val.of(asArray());
      }
      return Val.of(asMap());
   }

   /**
    * Gets an iterator over the elements in this element checking if the underlying entry is a json array.
    *
    * @return the iterator
    * @throws IllegalStateException if the entry's element is not a json array
    */
   public Iterator<JsonEntry> elementIterator() {
      return Iterators.transform(element.iterator(), JsonEntry::new);
   }

   public Stream<JsonEntry> elementStream() {
      return Streams.asStream(elementIterator());
   }

   @Override
   public boolean equals(Object obj) {
      if(this == obj) {
         return true;
      }
      if(obj == null || getClass() != obj.getClass()) {
         return false;
      }
      final JsonEntry other = (JsonEntry) obj;
      return Objects.equals(this.element, other.element);
   }

   /**
    * Performs the given action for entry in this array.
    *
    * @param consumer the action to perform
    * @throws IllegalStateException if the entry's element is not a json array
    */
   public void forEachElement(Consumer<JsonEntry> consumer) {
      elementIterator().forEachRemaining(consumer);
   }

   /**
    * Performs the given action for property name and value in this object.
    *
    * @param consumer the action to perform
    * @throws IllegalStateException if the entry's element is not a json object
    */
   public void forEachProperty(BiConsumer<String, JsonEntry> consumer) {
      propertyIterator().forEachRemaining(e -> consumer.accept(e.getKey(), e.getValue()));
   }

   public Object get() {
      if(isString()) {
         return asString();
      }
      if(isNumber()) {
         return asNumber();
      }
      if(isBoolean()) {
         return asBoolean();
      }
      if(isNull()) {
         return asNumber();
      }
      if(isObject()) {
         return asMap();
      }
      if(isArray()) {
         return asArray();
      }
      return as(Object.class);
   }

   /**
    * Gets the value of the given property name as a boolean
    *
    * @param propertyName the property name
    * @return the boolean property
    * @throws IllegalStateException if the entry is not a json object
    */
   public boolean getBooleanProperty(String propertyName) {
      return getProperty(propertyName).element.asBoolean();
   }

   /**
    * Gets the value of the given property name as a boolean
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the boolean property
    * @throws IllegalStateException if the entry is not a json object
    */
   public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asBoolean();
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a Character
    *
    * @param propertyName the property name
    * @return the Character property
    * @throws IllegalStateException if the entry is not a json object
    */
   public Character getCharacterProperty(String propertyName) {
      return getProperty(propertyName).asCharacter();
   }

   /**
    * Gets the value of the given property name as a Character
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the Character property
    * @throws IllegalStateException if the entry is not a json object
    */
   public Character getCharacterProperty(String propertyName, Character defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asCharacter();
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a double
    *
    * @param propertyName the property name
    * @return the double property
    * @throws IllegalStateException if the entry is not a json object
    */
   public double getDoubleProperty(String propertyName) {
      return getProperty(propertyName).asDouble();
   }

   /**
    * Gets the value of the given property name as a double
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the double property
    * @throws IllegalStateException if the entry is not a json object
    */
   public double getDoubleProperty(String propertyName, double defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asDouble();
      }
      return defaultValue;
   }

   /**
    * Gets the underlying JsonElement.
    *
    * @return the element
    */
   public JsonNode getElement() {
      return element;
   }

   /**
    * Gets the value of the given property name as a float
    *
    * @param propertyName the property name
    * @return the float property
    * @throws IllegalStateException if the entry is not a json object
    */
   public float getFloatProperty(String propertyName) {
      return getProperty(propertyName).asFloat();
   }

   /**
    * Gets the value of the given property name as a float
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the float property
    * @throws IllegalStateException if the entry is not a json object
    */
   public float getFloatProperty(String propertyName, float defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asFloat();
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a int
    *
    * @param propertyName the property name
    * @return the int property
    * @throws IllegalStateException if the entry is not a json object
    */
   public int getIntProperty(String propertyName) {
      return getProperty(propertyName).asInt();
   }

   /**
    * Gets the value of the given property name as a int
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the int property
    * @throws IllegalStateException if the entry is not a json object
    */
   public int getIntProperty(String propertyName, int defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asInt();
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a long
    *
    * @param propertyName the property name
    * @return the long property
    * @throws IllegalStateException if the entry is not a json object
    */
   public long getLongProperty(String propertyName) {
      return getProperty(propertyName).asLong();
   }

   /**
    * Gets the value of the given property name as a long
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the long property
    * @throws IllegalStateException if the entry is not a json object
    */
   public long getLongProperty(String propertyName, long defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asLong();
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a Number
    *
    * @param propertyName the property name
    * @return the Number property
    * @throws IllegalStateException if the entry is not a json object
    */
   public Number getNumberProperty(String propertyName) {
      return getProperty(propertyName).asNumber();
   }

   /**
    * Gets the value of the given property name as a Number
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the Number property
    * @throws IllegalStateException if the entry is not a json object
    */
   public Number getNumberProperty(String propertyName, Number defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asNumber();
      }
      return defaultValue;
   }

   public Optional<JsonEntry> getOptionalProperty(String propertyName) {
      if(hasProperty(propertyName)) {
         JsonEntry e = getProperty(propertyName);
         if(!e.isNull()) {
            return Optional.of(e);
         }
      }
      return Optional.empty();
   }

   /**
    * Gets the value of the given property name as a JsonEntry
    *
    * @param propertyName the property name
    * @return the JsonEntry property
    * @throws IllegalStateException if the entry is not a json object
    */
   public JsonEntry getProperty(String propertyName) {
      if(element.isObject()) {
         return new JsonEntry(element.get(propertyName));
      }
      throw new IllegalArgumentException("Trying to get '" + propertyName + "' from an array");
   }

   /**
    * Gets the value of the given property name as a JsonEntry
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the JsonEntry property
    * @throws IllegalStateException if the entry is not a json object
    */
   public JsonEntry getProperty(String propertyName, Object defaultValue) {
      if(element.has(propertyName)) {
         return new JsonEntry(element.get(propertyName));
      }
      return from(defaultValue);
   }

   /**
    * Gets the value of the given property name as the type of the given class
    *
    * @param <T>          the type parameter
    * @param propertyName the property name
    * @param clazz        Class information for the desired type
    * @return the property value
    * @throws IllegalStateException if the entry is not a json object
    */
   public <T> T getProperty(@NonNull String propertyName, @NonNull Class<T> clazz) {
      return getProperty(propertyName).as(clazz);
   }

   /**
    * Gets the value of the given property name as the type of the given class
    *
    * @param <T>          the type parameter
    * @param propertyName the property name
    * @param clazz        Class information for the desired type
    * @param defaultValue the default value if the property does not exist
    * @return the property value
    * @throws IllegalStateException if the entry is not a json object
    */
   public <T> T getProperty(String propertyName, Class<T> clazz, T defaultValue) {
      if(element.has(propertyName)) {
         return new JsonEntry(element.get(propertyName)).as(clazz);
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a String
    *
    * @param propertyName the property name
    * @return the String property
    * @throws IllegalStateException if the entry is not a json object
    */
   public String getStringProperty(String propertyName) {
      return getProperty(propertyName).asString();
   }

   /**
    * Gets the value of the given property name as a String
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the String property
    * @throws IllegalStateException if the entry is not a json object
    */
   public String getStringProperty(String propertyName, String defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asString();
      }
      return defaultValue;
   }

   /**
    * Gets the value of the given property name as a Val
    *
    * @param propertyName the property name
    * @return the Val property
    * @throws IllegalStateException if the entry is not a json object
    */
   public Val getValProperty(String propertyName) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asVal();
      }
      return Val.NULL;
   }

   /**
    * Gets the value of the given property name as a Val
    *
    * @param propertyName the property name
    * @param defaultValue the default value if the property does not exist
    * @return the Val property
    * @throws IllegalStateException if the entry is not a json object
    */
   public Val getValProperty(String propertyName, Object defaultValue) {
      if(element.has(propertyName)) {
         return getProperty(propertyName).asVal();
      }
      return Val.of(defaultValue);
   }

   /**
    * Checks if this entry has the given property
    *
    * @param propertyName the property name to check
    * @return true if this is an object and has the property otherwise false
    */
   public boolean hasProperty(String propertyName) {
      return element.has(propertyName);
   }

   @Override
   public int hashCode() {
      return Objects.hash(element);
   }

   /**
    * Checks if this entry is a json array
    *
    * @return true if a json array, false otherwise
    */
   public boolean isArray() {
      return element.isArray();
   }

   /**
    * Is boolean boolean.
    *
    * @return the boolean
    */
   public boolean isBoolean() {
      return element.isBoolean();
   }

   /**
    * Checks if this entry is a json null value
    *
    * @return true if a json null value, false otherwise
    */
   public boolean isNull() {
      return element.isNull();
   }

   /**
    * Is number boolean.
    *
    * @return the boolean
    */
   public boolean isNumber() {
      return element.isNumber();
   }

   /**
    * Checks if this entry is a json object
    *
    * @return true if a json object, false otherwise
    */
   public boolean isObject() {
      return element.isObject();
   }

   public boolean isPrimitive() {
      return isNumber() || isBoolean() || isString();
   }

   /**
    * Is string boolean.
    *
    * @return the boolean
    */
   public boolean isString() {
      return element.isTextual();
   }

   /**
    * Gets the keys (property names).
    *
    * @return the set of property names (keys) or empty set if not an object
    */
   public Set<String> keySet() {
      return new IteratorSet<>(() -> element.fieldNames());
   }

   public JsonEntry mergeObject(JsonEntry entry) {
      if(entry.isObject()) {
         entry.propertyIterator()
              .forEachRemaining(e -> this.addProperty(e.getKey(), e.getValue()));
         return this;
      }
      throw new IllegalArgumentException("Object expected");
   }

   //   public String pprint() {
   //      return pprint(3);
   //   }

   //   public String pprint(int indent) {
   //      StringWriter sw = new StringWriter();
   //      try(JsonWriter jw = MAPPER.newJsonWriter(sw)) {
   //         jw.setIndent(Strings.repeat(' ', indent));
   //         MAPPER.toJson(element, jw);
   //      } catch(Exception e) {
   //         throw new RuntimeException(e);
   //      }
   //      try {
   //         sw.close();
   //         return sw.getBuffer().toString();
   //      } catch(IOException e) {
   //         throw new RuntimeException(e);
   //      }
   //   }

   /**
    * Gets an iterator over the elements in this element.
    *
    * @return the iterator of properties if an object, empty iterator otherwise
    */
   public Iterator<Map.Entry<String, JsonEntry>> propertyIterator() {
      return Iterators.transform(element.fieldNames(), n -> $(n, getProperty(n)));
   }

   private void readObject(ObjectInputStream ois) throws IOException {
      element = Json.parse(ois.readUTF()).element;
   }

   public int size() {
      return element.size();
   }

   @Override
   @SneakyThrows
   public String toString() {
      return MAPPER.writeValueAsString(element);
   }

   private void writeObject(ObjectOutputStream oos) throws IOException {
      oos.writeUTF(element.toString());
   }

   private static class ElementList extends AbstractList<JsonEntry> implements Serializable {
      private static final long serialVersionUID = 1L;
      private final ArrayNode array;

      private ElementList(ArrayNode array) {
         this.array = array;
      }

      @Override
      public boolean add(JsonEntry entry) {
         array.add(entry.element);
         return true;
      }

      @Override
      public boolean equals(Object obj) {
         if(this == obj) {
            return true;
         }
         if(obj == null || getClass() != obj.getClass()) {
            return false;
         }
         if(!super.equals(obj)) {
            return false;
         }
         final ElementList other = (ElementList) obj;
         return Objects.equals(this.array, other.array);
      }

      @Override
      public JsonEntry get(int index) {
         return new JsonEntry(array.get(index));
      }

      @Override
      public int hashCode() {
         return 31 * super.hashCode() + Objects.hash(array);
      }

      @Override
      public JsonEntry remove(int index) {
         return new JsonEntry(array.remove(index));
      }

      @Override
      public boolean remove(Object o) {
         return false;
      }

      @Override
      public int size() {
         return array.size();
      }

      @Override
      public String toString() {
         return array.toString();
      }
   }

   private static class ElementMap extends AbstractMap<String, JsonEntry> implements Serializable {
      private static final long serialVersionUID = 1L;
      private final ObjectNode object;

      /**
       * Instantiates a new Element map.
       *
       * @param object the object
       */
      ElementMap(ObjectNode object) {
         this.object = object;
      }

      @Override
      public boolean containsKey(Object key) {
         return object.has(key.toString());
      }

      @Override
      public Set<Entry<String, JsonEntry>> entrySet() {
         return new IteratorSet<>(() -> Iterators.transform(object.fieldNames(),
                                                            n -> $(n, new JsonEntry(object.get(n)))));
      }

      @Override
      public boolean equals(Object obj) {
         if(this == obj) {
            return true;
         }
         if(obj == null || getClass() != obj.getClass()) {
            return false;
         }
         if(!super.equals(obj)) {
            return false;
         }
         final ElementMap other = (ElementMap) obj;
         return Objects.equals(this.object, other.object);
      }

      @Override
      public JsonEntry get(Object key) {
         return getOrDefault(key, null);
      }

      @Override
      public JsonEntry getOrDefault(Object key, JsonEntry defaultValue) {
         if(containsKey(key)) {
            return new JsonEntry(object.get(key.toString()));
         }
         return defaultValue;
      }

      @Override
      public int hashCode() {
         return 31 * super.hashCode() + Objects.hash(object);
      }

      @Override
      public Set<String> keySet() {
         return new IteratorSet<>(object::fieldNames);
      }

      @Override
      public JsonEntry put(String key, JsonEntry value) {
         JsonEntry toReturn = get(key);
         object.set(key, value.element);
         return toReturn;
      }

      @Override
      public void putAll(Map<? extends String, ? extends JsonEntry> m) {
         super.putAll(m);
      }

      @Override
      public JsonEntry remove(Object key) {
         return new JsonEntry(object.remove(key.toString()));
      }

      @Override
      public int size() {
         return object.size();
      }

      @Override
      public String toString() {
         return object.toString();
      }
   }

}//END OF JElement
