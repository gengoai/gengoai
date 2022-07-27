package com.gengoai.stream;

import com.gengoai.io.ResourceMonitor;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.gengoai.stream.Streams.*;

/**
 * @author David B. Bracewell
 */
final class ReusableJavaDoubleStream implements DoubleStream {
   final Supplier<DoubleStream> streamSupplier;

   ReusableJavaDoubleStream(Supplier<DoubleStream> streamSupplier) {
      this.streamSupplier = streamSupplier;
   }

   private static DoubleStream n(Supplier<DoubleStream> streamSupplier) {
      return new ReusableJavaDoubleStream(streamSupplier);
   }

   @Override
   public boolean allMatch(DoublePredicate predicate) {
      try(DoubleStream s = streamSupplier.get()) {
         return s.allMatch(predicate);
      }
   }

   @Override
   public boolean anyMatch(DoublePredicate predicate) {
      try(DoubleStream s = streamSupplier.get()) {
         return s.anyMatch(predicate);
      }
   }

   @Override
   public OptionalDouble average() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.average();
      }
   }

   @Override
   public Stream<Double> boxed() {
      return reusableStream(() -> streamSupplier.get().boxed());
   }

   @Override
   public void close() {
   }

   @Override
   public <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
      try(DoubleStream s = streamSupplier.get()) {
         return s.collect(supplier, accumulator, combiner);
      }
   }

   @Override
   public long count() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.count();
      }
   }

   @Override
   public DoubleStream distinct() {
      return n(() -> streamSupplier.get().distinct());
   }

   @Override
   public DoubleStream filter(DoublePredicate predicate) {
      return n(() -> streamSupplier.get().filter(predicate));
   }

   @Override
   public OptionalDouble findAny() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.findAny();
      }
   }

   @Override
   public OptionalDouble findFirst() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.findFirst();
      }
   }

   @Override
   public DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
      return n(() -> streamSupplier.get().flatMap(mapper));
   }

   @Override
   public void forEach(DoubleConsumer action) {
      try(DoubleStream s = streamSupplier.get()) {
         s.forEach(action);
      }
   }

   @Override
   public void forEachOrdered(DoubleConsumer action) {
      try(DoubleStream s = streamSupplier.get()) {
         s.forEachOrdered(action);
      }
   }

   @Override
   public boolean isParallel() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.isParallel();
      }
   }

   @Override
   public PrimitiveIterator.OfDouble iterator() {
      return ResourceMonitor.monitor(streamSupplier.get()).iterator();
   }

   @Override
   public DoubleStream limit(long maxSize) {
      return n(() -> streamSupplier.get().limit(maxSize));
   }

   @Override
   public DoubleStream map(DoubleUnaryOperator mapper) {
      return n(() -> streamSupplier.get().map(mapper));
   }

   @Override
   public IntStream mapToInt(DoubleToIntFunction mapper) {
      return reusableIntStream(() -> streamSupplier.get().mapToInt(mapper));
   }

   @Override
   public LongStream mapToLong(DoubleToLongFunction mapper) {
      return reusableLongStream(() -> streamSupplier.get().mapToLong(mapper));
   }

   @Override
   public <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
      return reusableStream(() -> streamSupplier.get().mapToObj(mapper));
   }

   @Override
   public OptionalDouble max() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.max();
      }
   }

   @Override
   public OptionalDouble min() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.min();
      }
   }

   @Override
   public boolean noneMatch(DoublePredicate predicate) {
      try(DoubleStream s = streamSupplier.get()) {
         return s.noneMatch(predicate);
      }
   }

   @Override
   public DoubleStream onClose(Runnable closeHandler) {
      return n(() -> streamSupplier.get().onClose(closeHandler));
   }

   @Override
   public DoubleStream parallel() {
      return n(() -> streamSupplier.get().parallel());
   }

   @Override
   public DoubleStream peek(DoubleConsumer action) {
      return n(() -> streamSupplier.get().peek(action));
   }

   @Override
   public double reduce(double identity, DoubleBinaryOperator op) {
      try(DoubleStream s = streamSupplier.get()) {
         return s.reduce(identity, op);
      }
   }

   @Override
   public OptionalDouble reduce(DoubleBinaryOperator op) {
      try(DoubleStream s = streamSupplier.get()) {
         return s.reduce(op);
      }
   }

   @Override
   public DoubleStream sequential() {
      return n(() -> streamSupplier.get().sequential());
   }

   @Override
   public DoubleStream skip(long n) {
      return n(() -> streamSupplier.get().skip(n));
   }

   @Override
   public DoubleStream sorted() {
      return n(() -> streamSupplier.get().sorted());
   }

   @Override
   public Spliterator.OfDouble spliterator() {
      return ResourceMonitor.monitor(streamSupplier.get()).spliterator();
   }

   @Override
   public double sum() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.sum();
      }
   }

   @Override
   public DoubleSummaryStatistics summaryStatistics() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.summaryStatistics();
      }
   }

   @Override
   public double[] toArray() {
      try(DoubleStream s = streamSupplier.get()) {
         return s.toArray();
      }
   }

   @Override
   public DoubleStream unordered() {
      return n(() -> streamSupplier.get().unordered());
   }

}//END OF ReusableJavaDoubleStream
