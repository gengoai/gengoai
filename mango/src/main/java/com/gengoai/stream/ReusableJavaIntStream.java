package com.gengoai.stream;

import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.gengoai.stream.Streams.*;

/**
 * @author David B. Bracewell
 */
final class ReusableJavaIntStream implements IntStream {
   final Supplier<IntStream> streamSupplier;

   ReusableJavaIntStream(Supplier<IntStream> streamSupplier) {
      this.streamSupplier = streamSupplier;
   }

   private static IntStream n(Supplier<IntStream> streamSupplier) {
      return new ReusableJavaIntStream(streamSupplier);
   }

   @Override
   public boolean allMatch(@NonNull IntPredicate predicate) {
      try(IntStream s = streamSupplier.get()) {
         return s.allMatch(predicate);
      }
   }

   @Override
   public boolean anyMatch(@NonNull IntPredicate predicate) {
      try(IntStream s = streamSupplier.get()) {
         return s.anyMatch(predicate);
      }
   }

   @Override
   public DoubleStream asDoubleStream() {
      return reusableDoubleStream(() -> streamSupplier.get().asDoubleStream());
   }

   @Override
   public LongStream asLongStream() {
      return reusableLongStream(() -> streamSupplier.get().asLongStream());
   }

   @Override
   public OptionalDouble average() {
      try(IntStream s = streamSupplier.get()) {
         return s.average();
      }
   }

   @Override
   public Stream<Integer> boxed() {
      return reusableStream(() -> streamSupplier.get().boxed());
   }

   @Override
   public void close() {
   }

   @Override
   public <R> R collect(@NonNull Supplier<R> supplier,
                        @NonNull ObjIntConsumer<R> accumulator,
                        @NonNull BiConsumer<R, R> combiner) {
      try(IntStream s = streamSupplier.get()) {
         return s.collect(supplier, accumulator, combiner);
      }
   }

   @Override
   public long count() {
      try(IntStream s = streamSupplier.get()) {
         return s.count();
      }
   }

   @Override
   public IntStream distinct() {
      return n(() -> streamSupplier.get().distinct());
   }

   @Override
   public IntStream filter(@NonNull IntPredicate predicate) {
      return n(() -> streamSupplier.get().filter(predicate));
   }

   @Override
   public OptionalInt findAny() {
      try(IntStream s = streamSupplier.get()) {
         return s.findAny();
      }
   }

   @Override
   public OptionalInt findFirst() {
      try(IntStream s = streamSupplier.get()) {
         return s.findFirst();
      }
   }

   @Override
   public IntStream flatMap(@NonNull IntFunction<? extends IntStream> mapper) {
      return n(() -> streamSupplier.get().flatMap(mapper));
   }

   @Override
   public void forEach(@NonNull IntConsumer action) {
      try(IntStream s = streamSupplier.get()) {
         s.forEach(action);
      }
   }

   @Override
   public void forEachOrdered(@NonNull IntConsumer action) {
      try(IntStream s = streamSupplier.get()) {
         s.forEachOrdered(action);
      }
   }

   @Override
   public boolean isParallel() {
      try(IntStream s = streamSupplier.get()) {
         return s.isParallel();
      }
   }

   @Override
   public PrimitiveIterator.OfInt iterator() {
      return ResourceMonitor.monitor(streamSupplier.get()).iterator();
   }

   @Override
   public IntStream limit(long maxSize) {
      return n(() -> streamSupplier.get().limit(maxSize));
   }

   @Override
   public IntStream map(@NonNull IntUnaryOperator mapper) {
      return n(() -> streamSupplier.get().map(mapper));
   }

   @Override
   public DoubleStream mapToDouble(@NonNull IntToDoubleFunction mapper) {
      return reusableDoubleStream(() -> streamSupplier.get().mapToDouble(mapper));
   }

   @Override
   public LongStream mapToLong(@NonNull IntToLongFunction mapper) {
      return reusableLongStream(() -> streamSupplier.get().mapToLong(mapper));
   }

   @Override
   public <U> Stream<U> mapToObj(@NonNull IntFunction<? extends U> mapper) {
      return reusableStream(() -> streamSupplier.get().mapToObj(mapper));
   }

   @Override
   public OptionalInt max() {
      try(IntStream s = streamSupplier.get()) {
         return s.max();
      }
   }

   @Override
   public OptionalInt min() {
      try(IntStream s = streamSupplier.get()) {
         return s.min();
      }
   }

   @Override
   public boolean noneMatch(@NonNull IntPredicate predicate) {
      try(IntStream s = streamSupplier.get()) {
         return s.noneMatch(predicate);
      }
   }

   @Override
   public IntStream onClose(@NonNull Runnable closeHandler) {
      return n(() -> streamSupplier.get().onClose(closeHandler));
   }

   @Override
   public IntStream parallel() {
      return n(() -> streamSupplier.get().parallel());
   }

   @Override
   public IntStream peek(IntConsumer action) {
      return n(() -> streamSupplier.get().peek(action));
   }

   @Override
   public int reduce(int identity, @NonNull IntBinaryOperator op) {
      try(IntStream s = streamSupplier.get()) {
         return s.reduce(identity, op);
      }
   }

   @Override
   public OptionalInt reduce(@NonNull IntBinaryOperator op) {
      try(IntStream s = streamSupplier.get()) {
         return s.reduce(op);
      }
   }

   @Override
   public IntStream sequential() {
      return n(() -> streamSupplier.get().sequential());
   }

   @Override
   public IntStream skip(long n) {
      return n(() -> streamSupplier.get().skip(n));
   }

   @Override
   public IntStream sorted() {
      return n(() -> streamSupplier.get().sorted());
   }

   @Override
   public Spliterator.OfInt spliterator() {
      return ResourceMonitor.monitor(streamSupplier.get()).spliterator();
   }

   @Override
   public int sum() {
      try(IntStream s = streamSupplier.get()) {
         return s.sum();
      }
   }

   @Override
   public IntSummaryStatistics summaryStatistics() {
      try(IntStream s = streamSupplier.get()) {
         return s.summaryStatistics();
      }
   }

   @Override
   public int[] toArray() {
      try(IntStream s = streamSupplier.get()) {
         return s.toArray();
      }
   }

   @Override
   public IntStream unordered() {
      return n(() -> streamSupplier.get().unordered());
   }

}//END OF ReusableJavaIntStream
