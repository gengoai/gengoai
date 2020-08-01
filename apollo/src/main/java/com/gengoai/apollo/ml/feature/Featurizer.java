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

package com.gengoai.apollo.ml.feature;

import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableList;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Featurizers define a mapping from input objects to a list of {@link Variable}. Additionally, a featurizer acts as a
 * {@link FeatureExtractor} allowing input objects to be converted directly to {@link Observation}s.
 * </p>
 *
 * @param <I> the type of the object extracting features from
 * @author David B. Bracewell
 */
public abstract class Featurizer<I> implements FeatureExtractor<I>, Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Creates a boolean feature extractor that uses the given function to convert an object into a collection of string
    * representing the feature names in form <code>PREFIX=NAME</code>.
    *
    * @param <I>      the type of the object extracting features from
    * @param function the function to convert the object into feature names
    * @return the featurizer
    */
   public static <I> Featurizer<I> booleanFeaturizer(SerializableFunction<? super I, ? extends Collection<String>> function) {
      return new BooleanExtractor<>(function);
   }

   /**
    * Chains multiple featurizers together into a single featurizer.
    *
    * @param <I>         the type parameter
    * @param featurizers the featurizers
    * @return the featurizer
    */
   @SafeVarargs
   public static <I> Featurizer<I> chain(Featurizer<? super I>... featurizers) {
      return new ChainFeaturizer<I>(Arrays.asList(featurizers));
   }

   public static <I> Featurizer<I> chain(List<Featurizer<? super I>> featurizers) {
      return new ChainFeaturizer<>(featurizers);
   }

   /**
    * Featurizer that counts the strings in an Iterable prepending the given feature prefix to the string.
    *
    * @param featurePrefix the feature prefix
    * @param normalize     True - normalize the counts by dividing by the sum.
    * @return the featurizer
    */
   public static Featurizer<Iterable<String>> countFeaturizer(String featurePrefix, boolean normalize) {
      return new RealExtractor<>(itreable -> {
         Counter<String> cntr = Counters.newCounter();
         for(String s : itreable) {
            cntr.increment(Variable.binary(featurePrefix, s).getName());
         }
         if(normalize) {
            return cntr.divideBySum();
         }
         return cntr;
      });
   }

   /**
    * Creates a feature extractor that returns multiple features . If the function returns a null value no feature is
    * generated.
    *
    * @param <I>           the type parameter
    * @param featurePrefix the feature prefix
    * @param function      the function
    * @return the featurizer
    */
   public static <I> Featurizer<I> multiValueFeaturizer(String featurePrefix,
                                                        SerializableFunction<? super I, Iterable<String>> function) {
      return new Featurizer<I>() {
         @Override
         public List<Variable> applyAsFeatures(I input) {
            Iterable<String> iterable = function.apply(input);
            return iterable == null
                   ? Collections.emptyList()
                   : Streams.asStream(iterable)
                            .map(s -> Variable.binary(featurePrefix, s))
                            .collect(Collectors.toList());
         }
      };
   }

   /**
    * Creates a feature extractor that creates a true boolean feature with a given name when the given predicate
    * evaluates to true.
    *
    * @param <I>         the type of the object extracting features from
    * @param featureName the name of the feature to create (in the form of <code>PREFIX=NAME</code>)
    * @param predicate   the predicate to test the input object.
    * @return the featurizer
    */
   public static <I> Featurizer<I> predicateFeaturizer(String featureName, SerializablePredicate<? super I> predicate) {
      return new PredicateExtractor<>(featureName, predicate);
   }

   /**
    * Creates a real feature extractor that uses the given function to convert an object into a Counter of string
    * representing the feature names in form <code>PREFIX=NAME</code> with associated real-values.
    *
    * @param <I>      the type of the object extracting features from
    * @param function the function to convert the object into a counter of feature names and values.
    * @return the featurizer
    */
   public static <I> Featurizer<I> realFeaturizer(SerializableFunction<? super I, ? extends Counter<String>> function) {
      return new RealExtractor<>(function);
   }

   /**
    * Creates a feature extractor that returns a single feature of the form <code>featurePrefix=function.apply(input)</code>.
    * If the function returns a null value no feature is generated.
    *
    * @param <I>           the type parameter
    * @param featurePrefix the feature prefix
    * @param function      the function
    * @return the featurizer
    */
   public static <I> Featurizer<I> valueFeaturizer(@NonNull String featurePrefix,
                                                   @NonNull SerializableFunction<? super I, String> function) {
      return new ValueExtractor<>(featurePrefix, function);
   }

   public static <I> Featurizer<I> valueFeaturizer(@NonNull SerializableFunction<? super I, String> function) {
      return new ValueExtractor<>(null, function);
   }

   /**
    * Applies the featurizer to the given input producing a list of {@link Variable}
    *
    * @param input the input object to extract for features from
    * @return the list of extracted {@link Variable}
    */
   public abstract List<Variable> applyAsFeatures(I input);

   @Override
   public VariableList extractObservation(I input) {
      return new VariableList(applyAsFeatures(input));
   }

   /**
    * Creates a new feature extractor that includes contextual features.
    *
    * @param patterns the contextual feature patterns
    * @return the feature extractor
    */
   public final FeatureExtractor<I> withContext(String... patterns) {
      return withContext(Arrays.asList(patterns));
   }

   /**
    * Creates a new feature extractor that includes contextual features.
    *
    * @param patterns the contextual feature patterns
    * @return the feature extractor
    */
   public final FeatureExtractor<I> withContext(List<String> patterns) {
      if(patterns == null || patterns.size() == 0) {
         return this;
      }
      List<ContextFeaturizer<? super I>> contextFeaturizers = new ArrayList<>();
      for(String pattern : patterns) {
         contextFeaturizers.addAll(ContextPatternParser.parse(pattern));
      }
      return new FeatureExtractorImpl<>(this, ContextFeaturizer.chain(contextFeaturizers));
   }

   private static class BooleanExtractor<I> extends Featurizer<I> {
      private static final long serialVersionUID = 1L;
      private final SerializableFunction<? super I, ? extends Collection<String>> function;

      private BooleanExtractor(SerializableFunction<? super I, ? extends Collection<String>> function) {
         this.function = function;
      }

      @Override
      public List<Variable> applyAsFeatures(I input) {
         return function.apply(input)
                        .stream()
                        .map(Variable::binary)
                        .collect(Collectors.toList());
      }
   }

   private static class ChainFeaturizer<I> extends Featurizer<I> {
      private static final long serialVersionUID = 1L;
      private final List<Featurizer<? super I>> featurizers;

      private ChainFeaturizer(List<Featurizer<? super I>> featurizers) {
         this.featurizers = featurizers;
      }

      @Override
      public List<Variable> applyAsFeatures(I input) {
         List<Variable> features = new ArrayList<>();
         for(Featurizer<? super I> featurizer : featurizers) {
            features.addAll(featurizer.applyAsFeatures(input));
         }
         return features;
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder("Features\n");
         for(Featurizer<? super I> featurizer : featurizers) {
            builder.append("\t").append(featurizer).append("\n");
         }
         return builder.toString();
      }
   }

   private static class PredicateExtractor<I> extends Featurizer<I> {
      private static final long serialVersionUID = 1L;
      private final Variable feature;
      private final SerializablePredicate<? super I> predicate;

      private PredicateExtractor(String featureName, SerializablePredicate<? super I> predicate) {
         this.predicate = predicate;
         this.feature = Variable.binary(featureName);
      }

      @Override
      public List<Variable> applyAsFeatures(I input) {
         if(predicate.test(input)) {
            return Collections.singletonList(feature);
         }
         return Collections.emptyList();
      }
   }

   private static class RealExtractor<I> extends Featurizer<I> {
      private static final long serialVersionUID = 1L;
      private final SerializableFunction<? super I, ? extends Counter<String>> function;

      private RealExtractor(SerializableFunction<? super I, ? extends Counter<String>> function) {
         this.function = function;
      }

      @Override
      public List<Variable> applyAsFeatures(I input) {
         Counter<String> counter = function.apply(input);
         List<Variable> features = new ArrayList<>();
         counter.forEach((k, v) -> features.add(Variable.real(k, v)));
         return features;
      }
   }

   private static class ValueExtractor<I> extends Featurizer<I> {
      private static final long serialVersionUID = 1L;
      private final String featurePrefix;
      private final SerializableFunction<? super I, String> function;

      private ValueExtractor(String featurePrefix, SerializableFunction<? super I, String> function) {
         this.function = function;
         this.featurePrefix = featurePrefix;
      }

      @Override
      public List<Variable> applyAsFeatures(I input) {
         String value = function.apply(input);
         if(value == null) {
            return Collections.emptyList();
         }
         return Collections.singletonList(Variable.binary(featurePrefix, value));
      }
   }

}//END OF Featurizer
