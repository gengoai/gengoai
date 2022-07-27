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
import com.gengoai.function.*;
import com.gengoai.EnhancedDoubleStatistics;
import com.gengoai.stream.MDoubleStream;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;

import static com.gengoai.stream.Streams.reusableDoubleStream;

public class LocalMDoubleStream implements MDoubleStream, Serializable {
   private static final long serialVersionUID = 1L;
   private final DoubleStream stream;

   public LocalMDoubleStream(@NonNull DoubleStream stream) {
      this.stream = stream;
   }

   @Override
   public boolean allMatch(@NonNull SerializableDoublePredicate predicate) {
      return stream.allMatch(predicate);
   }

   @Override
   public boolean anyMatch(@NonNull SerializableDoublePredicate predicate) {
      return stream.anyMatch(predicate);
   }

   @Override
   public MDoubleStream cache() {
      return new LocalMDoubleStream(reusableDoubleStream(stream.toArray()));
   }

   @Override
   public long count() {
      return stream.count();
   }

   @Override
   public MDoubleStream distinct() {
      return new LocalMDoubleStream(stream.distinct());
   }

   @Override
   public MDoubleStream filter(@NonNull SerializableDoublePredicate predicate) {
      return new LocalMDoubleStream(stream.filter(predicate));
   }

   @Override
   public OptionalDouble first() {
      return stream.findFirst();
   }

   @Override
   public MDoubleStream flatMap(@NonNull SerializableDoubleFunction<double[]> mapper) {
      return new LocalMDoubleStream(stream.flatMap(d -> Arrays.stream(mapper.apply(d))));
   }

   @Override
   public void forEach(@NonNull SerializableDoubleConsumer consumer) {
      stream.forEach(consumer);
   }

   @Override
   public StreamingContext getContext() {
      return LocalStreamingContext.INSTANCE;
   }

   @Override
   public boolean isEmpty() {
      return stream.count() == 0;
   }

   @Override
   public PrimitiveIterator.OfDouble iterator() {
      return stream.iterator();
   }

   @Override
   public MDoubleStream limit(int n) {
      return new LocalMDoubleStream(stream.limit(n));
   }

   @Override
   public MDoubleStream map(@NonNull SerializableDoubleUnaryOperator mapper) {
      return new LocalMDoubleStream(stream.map(mapper));
   }

   @Override
   public <T> MStream<T> mapToObj(@NonNull SerializableDoubleFunction<? extends T> function) {
      return getContext().stream(stream.mapToObj(function));
   }

   @Override
   public OptionalDouble max() {
      return stream.max();
   }

   @Override
   public double mean() {
      return stream.average().orElse(0d);
   }

   @Override
   public OptionalDouble min() {
      return stream.min();
   }

   @Override
   public boolean noneMatch(@NonNull SerializableDoublePredicate predicate) {
      return stream.noneMatch(predicate);
   }

   @Override
   public MDoubleStream onClose(@NonNull SerializableRunnable onCloseHandler) {
      return new LocalMDoubleStream(stream.onClose(onCloseHandler));
   }

   @Override
   public MDoubleStream parallel() {
      return new LocalMDoubleStream(stream.parallel());
   }

   @Override
   public OptionalDouble reduce(@NonNull SerializableDoubleBinaryOperator operator) {
      return stream.reduce(operator);
   }

   @Override
   public double reduce(double zeroValue, @NonNull SerializableDoubleBinaryOperator operator) {
      return stream.reduce(zeroValue, operator);
   }

   @Override
   public MDoubleStream repartition(int numberOfPartition) {
      return this;
   }

   @Override
   public MDoubleStream skip(int n) {
      return new LocalMDoubleStream(stream.skip(n));
   }

   @Override
   public MDoubleStream sorted(boolean ascending) {
      if(ascending) {
         return new LocalMDoubleStream(stream.sorted());
      }
      return new LocalMDoubleStream(stream.boxed()
                                          .sorted((d1, d2) -> -Double.compare(d1, d2))
                                          .mapToDouble(d -> d));
   }

   @Override
   public EnhancedDoubleStatistics statistics() {
      return stream.collect(EnhancedDoubleStatistics::new,
                            EnhancedDoubleStatistics::accept,
                            EnhancedDoubleStatistics::combine);
   }

   @Override
   public double stddev() {
      return statistics().getSampleStandardDeviation();
   }

   @Override
   public double sum() {
      return stream.sum();
   }

   @Override
   public double[] toArray() {
      return stream.toArray();
   }

   @Override
   public boolean isDistributed() {
      return false;
   }

   @Override
   public MDoubleStream union(@NonNull MDoubleStream other) {
      if(other.isDistributed()) {
         return other.union(this);
      }
      return new LocalMDoubleStream(reusableDoubleStream(() -> DoubleStream.concat(stream,
                                                                                   Cast.<LocalMDoubleStream>as(other).stream))
      );
   }

   @Override
   public void close() throws Exception {
      stream.close();
   }
}//END OF LocalMDoubleStream
