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

package com.gengoai.stream.local;

import com.gengoai.conversion.Cast;
import com.gengoai.function.Unchecked;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.*;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gengoai.stream.Streams.reusableDoubleStream;

/**
 * Represents a local streaming context using Java's built-in streams
 *
 * @author David B. Bracewell
 */
public final class LocalStreamingContext extends StreamingContext {
   private static final long serialVersionUID = 1L;
   /**
    * The singleton instance of the context
    */
   public static final LocalStreamingContext INSTANCE = new LocalStreamingContext();

   @Override
   public void close() {

   }

   @Override
   public <E> MCounterAccumulator<E> counterAccumulator(String name) {
      return new LocalMCounterAccumulator<>(name);
   }

   @Override
   public MDoubleAccumulator doubleAccumulator(double initialValue, String name) {
      return new LocalMDoubleAccumulator(initialValue, name);
   }


   @Override
   public MDoubleStream doubleStream(DoubleStream doubleStream) {
      return new LocalMDoubleStream(Objects.requireNonNullElseGet(doubleStream, Streams::reusableDoubleStream));
   }

   @Override
   public MDoubleStream doubleStream(double... values) {
      if(values == null || values.length == 0) {
         return emptyDouble();
      }
      return new LocalMDoubleStream(reusableDoubleStream(values));
   }


   @Override
   public <T> MStream<T> empty() {
      return new LocalReusableMStream<>(Stream::empty);
   }

   @Override
   public <E> MAccumulator<E, List<E>> listAccumulator(String name) {
      return new LocalMListAccumulator<>(name);
   }

   @Override
   public MLongAccumulator longAccumulator(long initialValue, String name) {
      return new LocalMLongAccumulator(initialValue, name);
   }

   @Override
   public <K, V> MMapAccumulator<K, V> mapAccumulator(String name) {
      return new LocalMMapAccumulator<>(name);
   }

   @Override
   public <K1, K2> MMultiCounterAccumulator<K1, K2> multiCounterAccumulator(String name) {
      return new LocalMMultiCounterAccumulator<>(name);
   }

   @Override
   public <K, V> MPairStream<K, V> pairStream(@NonNull Map<? extends K, ? extends V> map) {
      if(map == null) {
         return new LocalDefaultMPairStream<>(empty());
      }
      return new LocalDefaultMPairStream<>(Cast.as(stream(map.entrySet())));
   }

   @Override
   public <K, V> MPairStream<K, V> pairStream(@NonNull Collection<Map.Entry<? extends K, ? extends V>> tuples) {
      return new LocalDefaultMPairStream<>(Cast.as(stream(tuples)));
   }

   @Override
   public MStream<Integer> range(int startInclusive, int endExclusive) {
      return new LocalReusableMStream<>(() -> IntStream.range(startInclusive, endExclusive)
                                                       .boxed()
                                                       .parallel());
   }

   @Override
   public <E> MAccumulator<E, Set<E>> setAccumulator(String name) {
      return new LocalMSetAccumulator<>(name);
   }

   @Override
   public MStatisticsAccumulator statisticsAccumulator(String name) {
      return new LocalMStatisticsAccumulator(name);
   }

   @Override
   public <T> MStream<T> stream(Stream<T> stream) {
      if(stream == null) {
         return empty();
      }
      if(stream instanceof ReusableJavaStream) {
         return new LocalReusableMStream<T>(Cast.<ReusableJavaStream<T>>as(stream));
      }
      return new AbstractLocalMStream<T>() {
         @Override
         public Stream<T> javaStream() {
            return stream;
         }
      };
   }

   @Override
   public <T> MStream<T> stream(Iterable<? extends T> iterable) {
      if(iterable == null) {
         return empty();
      }
      else if(iterable instanceof Collection) {
         return new LocalInMemoryMStream<>(Cast.as(iterable));
      }
      return new LocalReusableMStream<T>(() -> Streams.asStream(iterable));
   }


   @Override
   public MStream<String> textFile(String location) {
      if(Strings.isNullOrBlank(location)) {
         return empty();
      }
      return textFile(Resources.from(location));

   }

   @Override
   public MStream<String> textFile(Resource resource) {
      if(resource == null) {
         return empty();
      }
      if(resource.isDirectory()) {
         return new LocalReusableMStream<>(() -> resource.getChildren(true).stream()
                                                         .filter(r -> !r.isDirectory())
                                                         .flatMap(Unchecked.function(r -> r.lines().javaStream())));
      }
      try {
         return resource.lines();
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public MStream<String> textFile(Resource location, boolean wholeFile) {

      if(!wholeFile) {
         return textFile(location);
      }
      if(location == null) {
         return empty();
      }
      if(location.isDirectory()) {
         return new LocalReusableMStream<>(() -> location.getChildren(true).stream()
                                                         .filter(r -> !r.isDirectory())
                                                         .map(Unchecked.function(Resource::readToString)));
      }
      try {
         return new LocalInMemoryMStream<>(Collections.singleton(location.readToString()));
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }


}//END OF LocalStreamingContext
