/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.apollo.ml.observation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.string.StringMatcher;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <p>
 * A {@link Variable} with defined prefix, suffix, and value. <b>Categorical</b> variables as a combination of category
 * name (prefix) and category value (suffix) and have feature value of 1.0. <b>Real</b> valued variables are represented
 * using a namespace (prefix) and the variable name (suffix). The namespace to distinguish between different variables
 * with overlapping names. For example given a text input we can define a TFIDF feature for the previous and current
 * word as follows:
 * </p>
 * <p>
 * <pre>
 * {@code
 *    previous_word: prefix = "WORD-1", suffix = word, value= TFIDF
 *    current_word: prefix = "WORD-0", suffix = word, value= TFIDF
 *
 *    Given previous_word = "the" and current_word = "house", we generate:
 *    previous_word = Feature(prefix="WORD-1", suffix="the", value=?)
 *    current_word = Feature(prefix="WORD-0", suffix="house", value=?)
 * }*
 * </pre>
 * </p>
 * <p>
 * The equals sign, "=", is a reserved character used to join the prefix and suffix, e.g. given the prefix "WORD" and
 * suffix "house" the two would be combined as "WORD=house". Additionally, suffixes "[", "]", "&lt;", "&gt;" are
 * reserved characters for suffixes. The bracket characters ("[" and "]") are used to denote position in contextualized
 * features, e.g. "WORD[-1]=test" to denote "WORD" feature of the previous observation. The greater, "&gt;", and less
 * than, "&lt;", signs are used to denote input/output name, e.g. "&lt;pos&gt;=NN" to denote the input/output named
 * "pos" with no prefix and suffix of "NN".
 * </p>
 */
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonTypeName("v")
public class Variable implements Observation, Serializable {
   private static final Pattern POSITION_PATTERN = Pattern.compile("\\[([+-]?\\d+)]");
   private static final Pattern DATUM_SOURCE_PATTERN = Pattern.compile("<([^>]+)>");
   private static final StringMatcher RESERVED = StringMatcher.contains("=[]<>");
   private static final long serialVersionUID = 1L;
   /**
    * Constant defining the separator between the prefix and suffix in the Variable name.
    */
   public static final String PREFIX_SUFFIX_SEPARATOR = "=";
   @Getter
   @JsonProperty("p")
   private String prefix;
   @Getter
   @JsonProperty("s")
   private String suffix;
   @JsonProperty("v")
   private float value;

   /**
    * Creates a binary {@link Variable} with the given feature prefix and suffix
    *
    * @param prefix the feature prefix (category name)
    * @param suffix the feature suffix (category value)
    * @return the feature
    */
   public static Variable binary(String prefix, @NonNull String suffix) {
      return new Variable(prefix, suffix, 1.0);
   }

   /**
    * Creates a binary {@link Variable} with the given feature suffix
    *
    * @param suffix the feature suffix (category value)
    * @return the feature
    */
   public static Variable binary(@NonNull String suffix) {
      return new Variable(Strings.EMPTY, suffix, 1.0);
   }

   /**
    * <p>
    * Creates a real-valued {@link Variable} with the given feature suffix. The feature prefix for a real valued feature
    * represents a namespace to distinguish between different variables with overlapping names. For example given a text
    * input we can define a TFIDF feature for the previous and current word as follows:
    * </p>
    * <pre>
    * {@code
    *    previous_word: prefix = "WORD-1", suffix = word, value= TFIDF
    *    current_word: prefix = "WORD-0", suffix = word, value= TFIDF
    *
    *    Given previous_word = "the" and current_word = "house", we generate:
    *    previous_word = Feature(prefix="WORD-1", suffix="the", value=?)
    *    current_word = Feature(prefix="WORD-0", suffix="house", value=?)
    * }*
    * </pre>
    *
    * @param suffix the feature suffix
    * @param value  the feature value
    * @return the feature
    */
   public static Variable real(@NonNull String suffix, double value) {
      return new Variable(Strings.EMPTY, suffix, value);
   }

   /**
    * <p>
    * Creates a real-valued {@link Variable} with the given feature prefix and suffix. The feature prefix for a real
    * valued feature represents a namespace to distinguish between different variables with overlapping names. For
    * example given a text input we can define a TFIDF feature for the previous and current word as follows:
    * </p>
    * <pre>
    * {@code
    *    previous_word: prefix = "WORD-1", suffix = word, value= TFIDF
    *    current_word: prefix = "WORD-0", suffix = word, value= TFIDF
    *
    *    Given previous_word = "the" and current_word = "house", we generate:
    *    previous_word = Feature(prefix="WORD-1", suffix="the", value=?)
    *    current_word = Feature(prefix="WORD-0", suffix="house", value=?)
    * }*
    * </pre>
    *
    * @param prefix the feature prefix
    * @param suffix the feature suffix
    * @param value  the feature value
    * @return the feature
    */
   public static Variable real(@NonNull String prefix, @NonNull String suffix, double value) {
      return new Variable(prefix, suffix, value);
   }

   /**
    * Instantiates a new Feature.
    *
    * @param suffix the suffix
    * @param value  the value
    */
   public Variable(@NonNull String suffix, double value) {
      this(Strings.EMPTY, suffix, value);
   }

   /**
    * Instantiates a new Feature.
    *
    * @param prefix the prefix
    * @param suffix the suffix
    * @param value  the value
    */
   @JsonCreator
   public Variable(@JsonProperty("p") String prefix,
                   @JsonProperty("s") @NonNull String suffix,
                   @JsonProperty("v") double value) {
      setPrefix(prefix);
      setSuffix(suffix);
      this.value = (float) value;
   }

   /**
    * Prepends the give source name to the front of the Variable's prefix.
    *
    * @param sourceName the source name
    */
   public void addSourceName(@NonNull String sourceName) {
      this.prefix = "<" + sourceName + ">" + prefix;
   }

   @Override
   public NDArray asNDArray() {
      return NDArrayFactory.ND.scalar(value);
   }

   @Override
   public Variable asVariable() {
      return this;
   }

   @Override
   public Variable copy() {
      return new Variable(prefix, suffix, value);
   }

   /**
    * Gets the name of the datum input or output this feature came from, if defined or else an empty string.
    *
    * @return the input or output name of the datum this feature was generated from or an empty string if not defined.
    */
   public String getDatumSourceName() {
      return Strings.firstMatch(DATUM_SOURCE_PATTERN, prefix, 1);
   }

   /**
    * Gets the name of the variable, which is represented as <code>PREFIX=SUFFIX</code> when a prefix is defined and
    * <code>SUFFIX</code> when no prefix is defined.
    *
    * @return the Variable name
    */
   public String getName() {
      String name = prefix;
      if(name.length() > 0) {
         name += PREFIX_SUFFIX_SEPARATOR;
      }
      return name + suffix;
   }

   /**
    * Gets the position defined in the prefix or 0 if none.
    *
    * @return the position when defined and 0 if none is defined
    */
   public int getPosition() {
      String match = Strings.firstMatch(POSITION_PATTERN, prefix, 1);
      if(Strings.isNotNullOrBlank(match)) {
         return Integer.parseInt(match);
      }
      return 0;
   }

   public double getValue() {
      return value;
   }

   @Override
   public Stream<Variable> getVariableSpace() {
      return Stream.of(this);
   }

   @Override
   public boolean isVariable() {
      return true;
   }

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      throw new UnsupportedOperationException("Variable does support mapping.");
   }

   @Override
   public void removeVariables(@NonNull Predicate<Variable> filter) {
      throw new UnsupportedOperationException("Variable does support filtering.");
   }

   /**
    * Sets the prefix of the Variable
    *
    * @param newPrefix the new prefix
    */
   public void setPrefix(String newPrefix) {
      this.prefix = Strings.nullToEmpty(newPrefix);
      Validation.checkArgument(prefix == null || !RESERVED.test(prefix),
                               "The equals sign '=' is reserved, but found in the prefix: '" + prefix + "'");
   }

   /**
    * Sets the suffix of the Variable
    *
    * @param newSuffix the new suffix of the Variable
    */
   public void setSuffix(String newSuffix) {
      this.suffix = Strings.nullToEmpty(newSuffix);
   }

   public void setValue(double value) {
      this.value = (float) value;
   }

   @Override
   public String toString() {
      return "(" + getName() + ", " + getValue() + ")";
   }

   @Override
   public void updateVariables(@NonNull Consumer<Variable> updater) {
      updater.accept(this);
   }

   /**
    * Updates the variable name by appending "&lt;name&gt;" to the front if the name is not null or blank. The name
    * represents the name of the input/out from the datum object.
    *
    * @param name the name of the input/output from the datum object
    * @return the variable
    */
   public Variable updateWithDatumSourceName(String name) {
      if(Strings.isNotNullOrBlank(name)) {
         prefix = "<" + name + ">" + Strings.nullToEmpty(prefix);
      }
      return this;
   }
}//END OF Variable
