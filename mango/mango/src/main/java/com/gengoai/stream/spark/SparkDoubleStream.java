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

package com.gengoai.stream.spark;

import com.gengoai.Primitives;
import com.gengoai.Validation;
import com.gengoai.config.Config;
import com.gengoai.config.Configurator;
import com.gengoai.conversion.Cast;
import com.gengoai.function.*;
import com.gengoai.math.EnhancedDoubleStatistics;
import com.gengoai.stream.MDoubleStream;
import com.gengoai.stream.MStatisticsAccumulator;
import com.gengoai.stream.MStream;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.stream.Collectors;

/**
 * <p>An implementation of a <code>MDoubleStream</code> backed by Spark DoubleRDD.</p>
 *
 * @author David B. Bracewell
 */
class SparkDoubleStream implements MDoubleStream, Serializable {
   private static final long serialVersionUID = 1L;
   private final JavaDoubleRDD doubleStream;
   private volatile Broadcast<Config> configBroadcast;
   private SerializableRunnable onClose;

   /**
    * Instantiates a new Spark double stream.
    *
    * @param doubleStream the double rdd to wrap
    */
   public SparkDoubleStream(JavaDoubleRDD doubleStream) {
      this.configBroadcast = SparkStreamingContext.INSTANCE.getConfigBroadcast();
      this.doubleStream = doubleStream;
   }

   @Override
   public boolean allMatch(SerializableDoublePredicate predicate) {
      return doubleStream.filter(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return predicate.test(d);
      }).count() == count();
   }

   @Override
   public boolean anyMatch(SerializableDoublePredicate predicate) {
      return doubleStream.filter(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return predicate.test(d);
      }).count() != 0;
   }

   @Override
   public MDoubleStream cache() {
      return new SparkDoubleStream(doubleStream.cache());
   }

   @Override
   public void close() throws Exception {
      this.doubleStream.unpersist();
      if(onClose != null) {
         onClose.run();
      }
   }

   @Override
   public long count() {
      return doubleStream.count();
   }

   @Override
   public MDoubleStream distinct() {
      return new SparkDoubleStream(doubleStream.distinct());
   }

   @Override
   public MDoubleStream filter(SerializableDoublePredicate predicate) {
      return new SparkDoubleStream(doubleStream.filter(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return predicate.test(d);
      }));
   }

   @Override
   public OptionalDouble first() {
      if(doubleStream.isEmpty()) {
         return OptionalDouble.empty();
      }
      return OptionalDouble.of(doubleStream.first());
   }

   @Override
   public MDoubleStream flatMap(SerializableDoubleFunction<double[]> mapper) {
      return new SparkDoubleStream(doubleStream.flatMapToDouble(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return Arrays.stream(mapper.apply(d)).iterator();
      }));
   }

   @Override
   public void forEach(SerializableDoubleConsumer consumer) {
      doubleStream.foreach(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         consumer.accept(d);
      });
   }

   @Override
   public SparkStreamingContext getContext() {
      return SparkStreamingContext.contextOf(this);
   }


   /**
    * Gets the wrapped rdd.
    *
    * @return the rdd
    */
   JavaDoubleRDD getRDD() {
      return doubleStream;
   }

   @Override
   public boolean isEmpty() {
      return doubleStream.isEmpty();
   }

   @Override
   public PrimitiveIterator.OfDouble iterator() {
      return new PrimitiveIterator.OfDouble() {

         Iterator<Double> itr = doubleStream.toLocalIterator();

         @Override
         public boolean hasNext() {
            return itr.hasNext();
         }

         @Override
         public double nextDouble() {
            return itr.next();
         }
      };
   }

   @Override
   public MDoubleStream limit(int n) {
      Validation.checkArgument(n >= 0, "Limit number must be non-negative.");
      return new SparkDoubleStream(doubleStream.zipWithIndex().filter(p -> p._2() < n).mapToDouble(Tuple2::_1));
   }

   @Override
   public MDoubleStream map(SerializableDoubleUnaryOperator mapper) {
      return new SparkDoubleStream(doubleStream.mapToDouble(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return mapper.applyAsDouble(d);
      }));
   }

   @Override
   public <T> MStream<T> mapToObj(SerializableDoubleFunction<? extends T> function) {
      return new SparkStream<>(doubleStream.map(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return function.apply(d);
      }));
   }

   @Override
   public OptionalDouble max() {
      if(doubleStream.isEmpty()) {
         return OptionalDouble.empty();
      }
      return OptionalDouble.of(doubleStream.max());
   }

   @Override
   public double mean() {
      return doubleStream.mean();
   }

   @Override
   public OptionalDouble min() {
      if(doubleStream.isEmpty()) {
         return OptionalDouble.empty();
      }
      return OptionalDouble.of(doubleStream.min());
   }

   @Override
   public boolean noneMatch(SerializableDoublePredicate predicate) {
      return doubleStream.filter(d -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return predicate.test(d);
      }).count() == 0;
   }

   @Override
   public MDoubleStream onClose(SerializableRunnable onCloseHandler) {
      this.onClose = onCloseHandler;
      return this;
   }

   @Override
   public boolean isDistributed() {
      return true;
   }

   @Override
   public MDoubleStream parallel() {
      return this;
   }

   @Override
   public OptionalDouble reduce(SerializableDoubleBinaryOperator operator) {
      if(doubleStream.isEmpty()) {
         return OptionalDouble.empty();
      }
      return OptionalDouble.of(doubleStream.reduce((d1, d2) -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return operator.applyAsDouble(d1, d2);
      }));
   }

   @Override
   public double reduce(double zeroValue, SerializableDoubleBinaryOperator operator) {
      if(doubleStream.isEmpty()) {
         return zeroValue;
      }
      return zeroValue + doubleStream.reduce((d1, d2) -> {
         Configurator.INSTANCE.configure(configBroadcast.value());
         return operator.applyAsDouble(d1, d2);
      });
   }

   @Override
   public MDoubleStream repartition(int numberOfPartition) {
      return new SparkDoubleStream(doubleStream.repartition(numberOfPartition));
   }

   @Override
   public MDoubleStream skip(int n) {
      if(n > count()) {
         return getContext().emptyDouble();
      }
      else if(n <= 0) {
         return this;
      }
      return new SparkDoubleStream(doubleStream.zipWithIndex().filter(p -> p._2() > n - 1).mapToDouble(Tuple2::_1));
   }

   @Override
   public MDoubleStream sorted(boolean ascending) {
      return new SparkDoubleStream(doubleStream.map(Double::valueOf)
                                               .sortBy(d -> d, ascending, doubleStream.partitions().size())
                                               .mapToDouble(d -> d));
   }

   @Override
   public EnhancedDoubleStatistics statistics() {
      MStatisticsAccumulator accumulator = getContext().statisticsAccumulator();
      forEach(accumulator::add);
      return accumulator.value();
   }

   @Override
   public double stddev() {
      return doubleStream.stdev();
   }

   @Override
   public double sum() {
      return doubleStream.sum();
   }

   @Override
   public double[] toArray() {
      return Primitives.toDoubleArray(doubleStream.collect());
   }

   @Override
   public MDoubleStream union(MDoubleStream other) {
      if(other == null) {
         return this;
      }
      else if(other instanceof SparkDoubleStream) {
         return new SparkDoubleStream(doubleStream.union(Cast.<SparkDoubleStream>as(other).getRDD()));
      }
      SparkStreamingContext sc = getContext();
      return new SparkDoubleStream(doubleStream.union(sc.sparkContext()
                                                        .parallelizeDoubles(Arrays.stream(other.toArray())
                                                                                  .boxed()
                                                                                  .collect(Collectors.toList())
                                                                           )));
   }

   @Override
   public void updateConfig() {
      SparkStreamingContext.INSTANCE.updateConfig();
      this.configBroadcast = SparkStreamingContext.INSTANCE.getConfigBroadcast();
   }

}//END OF SparkDoubleStream
