package com.gengoai.stream;

import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author David B. Bracewell
 */
public final class ReusableJavaStream<T> implements Stream<T> {
   final Supplier<Stream<T>> streamSupplier;

   ReusableJavaStream(@NonNull Supplier<Stream<T>> streamSupplier) {
      this.streamSupplier = streamSupplier;
   }

   @Override
   public boolean allMatch(Predicate<? super T> predicate) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.allMatch(predicate);
      }
   }

   @Override
   public boolean anyMatch(Predicate<? super T> predicate) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.anyMatch(predicate);
      }
   }

   @Override
   public void close() {
   }

   @Override
   public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
      return streamSupplier.get().collect(supplier, accumulator, combiner);
   }

   @Override
   public <R, A> R collect(Collector<? super T, A, R> collector) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.collect(collector);
      }
   }

   @Override
   public long count() {
      try(Stream<T> s = streamSupplier.get()) {
         return s.count();
      }
   }

   @Override
   public Stream<T> distinct() {
      return new ReusableJavaStream<>(() -> streamSupplier.get().distinct());
   }

   @Override
   public Stream<T> filter(Predicate<? super T> predicate) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().filter(predicate));
   }

   @Override
   public Optional<T> findAny() {
      try(Stream<T> s = streamSupplier.get()) {
         return s.findAny();
      }
   }

   @Override
   public Optional<T> findFirst() {
      try(Stream<T> s = streamSupplier.get()) {
         return s.findFirst();
      }
   }

   @Override
   public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().flatMap(mapper));
   }

   @Override
   public ReusableJavaDoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
      return new ReusableJavaDoubleStream(() -> streamSupplier.get().flatMapToDouble(mapper));
   }

   @Override
   public ReusableJavaIntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
      return new ReusableJavaIntStream(() -> streamSupplier.get().flatMapToInt(mapper));
   }

   @Override
   public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
      return new ReusableJavaLongStream(() -> streamSupplier.get().flatMapToLong(mapper));
   }

   @Override
   public void forEach(Consumer<? super T> action) {
      try(Stream<T> s = streamSupplier.get()) {
         s.forEach(action);
      }
   }

   @Override
   public void forEachOrdered(Consumer<? super T> action) {
      try(Stream<T> s = streamSupplier.get()) {
         s.forEachOrdered(action);
      }
   }

   @Override
   public boolean isParallel() {
      try(Stream<T> s = streamSupplier.get()) {
         return s.isParallel();
      }
   }

   @Override
   public Iterator<T> iterator() {
      return new Iterator<T>() {
         Stream<T> stream = ResourceMonitor.monitor(streamSupplier.get());
         Iterator<T> iterator = stream.iterator();

         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         @Override
         public T next() {
            return iterator.next();
         }
      };
   }

   @Override
   public Stream<T> limit(long maxSize) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().limit(maxSize));
   }

   @Override
   public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().map(mapper));
   }

   @Override
   public ReusableJavaDoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
      return new ReusableJavaDoubleStream(() -> streamSupplier.get().mapToDouble(mapper));
   }

   @Override
   public ReusableJavaIntStream mapToInt(ToIntFunction<? super T> mapper) {
      return new ReusableJavaIntStream(() -> streamSupplier.get().mapToInt(mapper));
   }

   @Override
   public LongStream mapToLong(ToLongFunction<? super T> mapper) {
      return new ReusableJavaLongStream(() -> streamSupplier.get().mapToLong(mapper));
   }

   @Override
   public Optional<T> max(Comparator<? super T> comparator) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.max(comparator);
      }
   }

   @Override
   public Optional<T> min(Comparator<? super T> comparator) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.min(comparator);
      }
   }

   @Override
   public boolean noneMatch(Predicate<? super T> predicate) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.noneMatch(predicate);
      }
   }

   @Override
   public Stream<T> onClose(Runnable closeHandler) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().onClose(closeHandler));
   }

   @Override
   public Stream<T> parallel() {
      return new ReusableJavaStream<>(() -> streamSupplier.get().parallel());
   }

   @Override
   public Stream<T> peek(Consumer<? super T> action) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().peek(action));
   }

   @Override
   public T reduce(T identity, BinaryOperator<T> accumulator) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.reduce(identity, accumulator);
      }
   }

   @Override
   public Optional<T> reduce(BinaryOperator<T> accumulator) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.reduce(accumulator);
      }
   }

   @Override
   public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.reduce(identity, accumulator, combiner);
      }
   }

   @Override
   public Stream<T> sequential() {
      return new ReusableJavaStream<>(() -> streamSupplier.get().sequential());
   }

   @Override
   public Stream<T> skip(long n) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().skip(n));
   }

   @Override
   public Stream<T> sorted() {
      return new ReusableJavaStream<>(() -> streamSupplier.get().sorted());
   }

   @Override
   public Stream<T> sorted(Comparator<? super T> comparator) {
      return new ReusableJavaStream<>(() -> streamSupplier.get().sorted(comparator));
   }

   @Override
   public Spliterator<T> spliterator() {
      return streamSupplier.get().spliterator();
   }

   @Override
   public Object[] toArray() {
      try(Stream<T> s = streamSupplier.get()) {
         return s.toArray();
      }
   }

   @Override
   public <A> A[] toArray(IntFunction<A[]> generator) {
      try(Stream<T> s = streamSupplier.get()) {
         return s.toArray(generator);
      }
   }

   @Override
   public Stream<T> unordered() {
      return new ReusableJavaStream<>(() -> streamSupplier.get().unordered());
   }
}//END OF ReusableJavaStream
