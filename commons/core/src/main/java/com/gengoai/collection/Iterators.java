package com.gengoai.collection;

import com.gengoai.Validation;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.stream.Streams;

import java.util.*;
import java.util.stream.IntStream;

import static com.gengoai.Validation.*;
import static com.gengoai.tuple.Tuples.$;

/**
 * Methods for manipulating iterators
 *
 * @author David B. Bracewell
 */
public final class Iterators {
   private Iterators() {
      throw new IllegalAccessError();
   }

   /**
    * As iterator iterator.
    *
    * @param object the object
    * @return the iterator
    */
   public static Iterator<?> asIterator(Object object) {
      if(object instanceof Iterable) {
         return Cast.<Iterable>as(object).iterator();
      } else if(object instanceof Iterator) {
         return Cast.as(object);
      } else if(object.getClass().isArray()) {
         return Iterables.asIterable(object).iterator();
      } else if(object instanceof Map) {
         return Cast.<Map<?, ?>>as(object).entrySet().iterator();
      } else if(object instanceof Counter) {
         return Cast.<Counter<?>>as(object).entries().iterator();
      } else if(object instanceof MultiCounter) {
         return Cast.<MultiCounter<?, ?>>as(object).entries().iterator();
      }
      return Collections.singleton(object).iterator();
   }

   /**
    * Concatenates iterators together
    *
    * @param <T>       the iterator element type parameter
    * @param iterators the iterators to concatenate
    * @return the concatenated iterator
    */
   @SafeVarargs
   public static <T> Iterator<T> concat(final Iterator<? extends T>... iterators) {
      return new ConcatIterator<>(Arrays.asList(iterators).iterator());
   }

   /**
    * Concatenates iterables together
    *
    * @param <T>       the iterables element type parameter
    * @param iterables the iterables to concatenate
    * @return the concatenated iterator
    */
   @SafeVarargs
   public static <T> Iterator<T> concat(final Iterable<? extends T>... iterables) {
      return new ConcatIterableIterator<>(Arrays.asList(iterables).iterator());
   }

   /**
    * Filters elements from the given iterator when the given filter predicate evaluates to false
    *
    * @param <E>      the iterator element parameter
    * @param iterator the iterator to filter
    * @param filter   the filter to apply items evaluating to false will be removed from the iterator
    * @return the filtered iterator
    */
   public static <E> Iterator<E> filter(final Iterator<? extends E> iterator,
                                        final SerializablePredicate<? super E> filter
                                       ) {
      return new FilteredIterator<>(notNull(iterator), notNull(filter));
   }

   /**
    * Gets the first element of the iterator that would be returned by calling <code>next</code> if it exists  or the
    * default value.
    *
    * @param <T>          the iterator element type parameter
    * @param iterator     the iterator
    * @param defaultValue the default value
    * @return the first element in the iterator or the default value
    */
   public static <T> T first(Iterator<? extends T> iterator, T defaultValue) {
      return first(iterator).orElse(Cast.as(defaultValue));
   }

   /**
    * Gets the first element of the iterator that would be returned by calling <code>next</code> if it exists.
    *
    * @param <T>      the iterator element type parameter
    * @param iterator the iterator
    * @return the first element in the iterator
    */
   public static <T> Optional<T> first(Iterator<? extends T> iterator) {
      notNull(iterator);
      if(iterator.hasNext()) {
         return Optional.ofNullable(iterator.next());
      }
      return Optional.empty();
   }

   /**
    * Flattens an iterator of iterators.
    *
    * @param <T>      the iterator element type parameter
    * @param iterator the iterator
    * @return the flattened iterator
    */
   public static <T> Iterator<T> flatten(final Iterator<? extends Iterator<? extends T>> iterator) {
      return new ConcatIterator<>(iterator);
   }

   /**
    * Gets the element of the iterator after iterator <code>index</code> times if it exists
    *
    * @param <T>      the iterator element type parameter
    * @param iterator the iterator
    * @param n        how many times to iterate
    * @return Optional of the element of the iterator after iterating n times if it exists
    */
   public static <T> Optional<T> get(Iterator<? extends T> iterator, int n) {
      int i = iterateTo(notNull(iterator), n);
      if(i == n && iterator.hasNext()) {
         return Optional.ofNullable(iterator.next());
      }
      return Optional.empty();
   }

   /**
    * Gets the element of the iterator after iterator <code>index</code> times if it exists and if it does not exist,
    * returns the default value
    *
    * @param <T>          the iterator element type parameter
    * @param iterator     the iterator
    * @param n            how many times to iterate
    * @param defaultValue the default value to return if nothing is at the given index
    * @return the element of the iterator after iterating n times or default value if cannot iterator n times
    */
   public static <T> T get(Iterator<? extends T> iterator, int n, T defaultValue) {
      return get(iterator, n).orElse(Cast.as(defaultValue));
   }

   private static int iterateTo(Iterator<?> iterator, int index) {
      int i = 0;
      while(i < index && iterator.hasNext()) {
         i++;
         iterator.next();
      }
      return i;
   }

   /**
    * Gets the last element of the iterator that would be returned by calling <code>next</code> until
    * <code>hasNext</code> returns false if it exists  or the default value.
    *
    * @param <T>          the iterator element type parameter
    * @param iterator     the iterator
    * @param defaultValue the default value
    * @return the last element in the iterator or the default value
    */
   public static <T> T last(Iterator<? extends T> iterator, T defaultValue) {
      return last(iterator).orElse(Cast.as(defaultValue));
   }

   /**
    * Gets the last element of the iterator that would be returned by calling <code>next</code> until
    * <code>hasNext</code> returns false if it exists.
    *
    * @param <T>      the iterator element type parameter
    * @param iterator the iterator
    * @return the last element in the iterator
    */
   public static <T> Optional<T> last(Iterator<? extends T> iterator) {
      notNull(iterator);
      T value = null;
      while(iterator.hasNext()) {
         value = iterator.next();
      }
      return Optional.ofNullable(value);
   }

   /**
    * Gets the next element of the iterator that would be returned by calling <code>next</code> if it exists.
    *
    * @param <T>      the iterator element type parameter
    * @param iterator the iterator
    * @return the next element in the iterator
    */
   public static <T> Optional<T> next(Iterator<? extends T> iterator) {
      if(notNull(iterator).hasNext()) {
         return Optional.ofNullable(iterator.next());
      }
      return Optional.empty();
   }

   /**
    * Gets the next element of the iterator that would be returned by calling <code>next</code> if it exists or the
    * default value.
    *
    * @param <T>          the iterator element type parameter
    * @param iterator     the iterator
    * @param defaultValue the default value
    * @return the next element in the iterator or the default value
    */
   public static <T> T next(Iterator<? extends T> iterator, T defaultValue) {
      return next(iterator).orElse(Cast.as(defaultValue));
   }

   /**
    * Partitions the elements in the iterator into a number of lists equal to partition size except for the last
    * partition, which may have less elements.
    *
    * @param <T>           the iterator element type parameter
    * @param iterator      the iterator to partition
    * @param partitionSize the partition size
    * @return the partitioned iterator
    */
   public static <T> Iterator<List<T>> partition(final Iterator<T> iterator,
                                                 final int partitionSize
                                                ) {
      return new PartitionedIterator<>(notNull(iterator),
                                       validateArg(partitionSize,
                                                   size -> size > 0,
                                                   "Partition size must be greater than zero.",
                                                   false),
                                       false);
   }

   /**
    * Partitions the elements in the iterator into a number of lists equal to partition size except for the last
    * partition, which may have less elements if pad is false but will be filled with nulls if true.
    *
    * @param <T>           the iterator element type parameter
    * @param iterator      the iterator to partition
    * @param partitionSize the partition size
    * @param pad           true add nulls to ensure list size is equal to partition size
    * @return the partitioned iterator
    */
   public static <T> Iterator<List<T>> partition(final Iterator<? extends T> iterator,
                                                 int partitionSize,
                                                 boolean pad
                                                ) {
      return new PartitionedIterator<>(notNull(iterator),
                                       validateArg(partitionSize,
                                                   size -> size > 0,
                                                   "Partition size must be greater than zero.",
                                                   false),
                                       pad);
   }

   public static <T> Iterator<T> singletonIterator(T object) {
      return new Iterator<T>() {
         boolean available = true;

         @Override
         public boolean hasNext() {
            return available;
         }

         @Override
         public T next() {
            if(!available) {
               throw new NoSuchElementException();
            }
            available = false;
            return object;
         }
      };
   }

   /**
    * Gets the number of items in the iterator
    *
    * @param iterator the iterator
    * @return the size or number of items in the iterator
    */
   public static int size(final Iterator<?> iterator) {
      return (int) Streams.asStream(notNull(iterator)).count();
   }

   /**
    * Creates an iterator that transforms the elements of the iterator
    *
    * @param <I>      the type parameter of the item in given iterator
    * @param <O>      the type parameter of the output of the transform function
    * @param iterator the iterator to transform
    * @param function the transform function
    * @return the transformed iterator
    */
   public static <I, O> Iterator<O> transform(final Iterator<? extends I> iterator,
                                              final SerializableFunction<? super I, ? extends O> function
                                             ) {
      return new TransformedIterator<>(notNull(iterator), notNull(function));
   }

   /**
    * Wraps an iterator allowing items to not be removed
    *
    * @param <T>      the iterator element type parameter
    * @param iterator the iterator to make unmodifiable
    * @return the unmodifiable iterator
    */
   public static <T> Iterator<T> unmodifiableIterator(final Iterator<? extends T> iterator) {
      return new UnmodifiableIterator<>(notNull(iterator));
   }

   /**
    * <p>Zips (combines) two iterators together. For example, if iterator 1 contained [1,2,3] and iterator 2 contained
    * [4,5,6] the result would be [(1,4), (2,5), (3,6)]. Note that the length of the resulting stream will be the
    * minimum of the two iterators.</p>
    *
    * @param <T>       the component type of the first iterator
    * @param <U>       the component type of the second iterator
    * @param iterator1 the iterator making up the key in the resulting entries
    * @param iterator2 the iterator making up the value in the resulting entries
    * @return A stream of entries whose keys are taken from iterator1 and values are taken from iterator2
    */
   public static <T, U> Iterator<Map.Entry<T, U>> zip(final Iterator<? extends T> iterator1,
                                                      final Iterator<? extends U> iterator2
                                                     ) {
      return new ZippedIterator<>(notNull(iterator1), notNull(iterator2));
   }

   /**
    * Creates pairs of entries from the given iterator and its index in the iterator (0 based)
    *
    * @param <T>      the iterator type parameter
    * @param iterator the iterator
    * @return the iterator with index values
    */
   public static <T> Iterator<Map.Entry<T, Integer>> zipWithIndex(final Iterator<? extends T> iterator) {
      return new ZippedIterator<>(notNull(iterator),
                                  IntStream.iterate(0, i -> i + 1).boxed().iterator());
   }

   private static class PartitionedIterator<E> implements Iterator<List<E>> {
      private final Iterator<? extends E> backing;
      private final int partitionSize;
      private final boolean pad;

      private PartitionedIterator(Iterator<? extends E> backing, int partitionSize, boolean pad) {
         this.backing = backing;
         this.partitionSize = partitionSize;
         this.pad = pad;
      }

      @Override
      public boolean hasNext() {
         return backing.hasNext();
      }

      @Override
      public List<E> next() {
         Validation.checkState(backing.hasNext());
         List<E> nextList = new ArrayList<>();
         while(backing.hasNext() && nextList.size() < partitionSize) {
            nextList.add(backing.next());
         }
         if(pad) {
            while(nextList.size() < partitionSize) {
               nextList.add(null);
            }
         }
         return Collections.unmodifiableList(nextList);
      }
   }

   private static class ZippedIterator<T, U> implements Iterator<Map.Entry<T, U>> {
      private final Iterator<? extends T> iterator1;
      private final Iterator<? extends U> iterator2;

      private ZippedIterator(Iterator<? extends T> iterator1, Iterator<? extends U> iterator2) {
         this.iterator1 = iterator1;
         this.iterator2 = iterator2;
      }

      @Override
      public boolean hasNext() {
         return iterator1.hasNext() && iterator2.hasNext();
      }

      @Override
      public Map.Entry<T, U> next() {
         return $(iterator1.next(), iterator2.next());
      }

      @Override
      public void remove() {
         iterator1.remove();
         iterator2.remove();
      }
   }

   private static class FilteredIterator<E> implements Iterator<E> {
      private final Iterator<? extends E> backing;
      private final SerializablePredicate<? super E> filter;
      private E next = null;
      private boolean canRemove = false;

      private FilteredIterator(Iterator<? extends E> backing, SerializablePredicate<? super E> filter) {
         this.backing = backing;
         this.filter = filter;
      }

      private boolean advance() {
         while(next == null && backing.hasNext()) {
            next = backing.next();
            canRemove = false;
            if(!filter.test(next)) {
               next = null;
            }
         }
         return next != null;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public E next() {
         E toReturn = validate(advance(), NoSuchElementException::new, next);
         next = null;
         canRemove = true;
         return toReturn;
      }

      @Override
      public void remove() {
         validate(canRemove, IllegalStateException::new);
         backing.remove();
      }
   }

   private static class TransformedIterator<I, O> implements Iterator<O> {
      private final Iterator<? extends I> backing;
      private final SerializableFunction<? super I, ? extends O> transform;

      private TransformedIterator(Iterator<? extends I> backing,
                                  SerializableFunction<? super I, ? extends O> transform) {
         this.backing = backing;
         this.transform = transform;
      }

      @Override
      public boolean hasNext() {
         return backing.hasNext();
      }

      @Override
      public O next() {
         return transform.apply(backing.next());
      }

      @Override
      public void remove() {
         backing.remove();
      }
   }

   private static class ConcatIterableIterator<E> implements Iterator<E> {
      private final Iterator<Iterable<? extends E>> iterables;
      private Iterator<? extends E> current = null;

      private ConcatIterableIterator(Iterator<Iterable<? extends E>> iterables) {
         this.iterables = iterables;
      }

      private boolean advance() {
         if(current != null && current.hasNext()) {
            return true;
         }
         while(iterables.hasNext()) {
            current = iterables.next().iterator();
            if(current.hasNext()) {
               return true;
            }
         }
         return false;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public E next() {
         validate(advance(), NoSuchElementException::new);
         return current.next();
      }

      @Override
      public void remove() {
         current.remove();
      }
   }

   private static class ConcatIterator<E> implements Iterator<E> {
      private final Iterator<? extends Iterator<? extends E>> iterators;
      private Iterator<? extends E> current = null;

      private ConcatIterator(Iterator<? extends Iterator<? extends E>> iterators) {
         this.iterators = iterators;
      }

      private boolean advance() {
         if(current != null && current.hasNext()) {
            return true;
         }
         while(iterators.hasNext()) {
            current = iterators.next();
            if(current.hasNext()) {
               return true;
            }
         }
         return false;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public E next() {
         validate(advance(), NoSuchElementException::new);
         return current.next();
      }

      @Override
      public void remove() {
         current.remove();
      }
   }

   private static class UnmodifiableIterator<E> implements Iterator<E> {
      /**
       * The Backing iterator.
       */
      final Iterator<? extends E> backingIterator;

      private UnmodifiableIterator(Iterator<? extends E> backingIterator) {
         this.backingIterator = backingIterator;
      }

      @Override
      public boolean hasNext() {
         return backingIterator.hasNext();
      }

      @Override
      public E next() {
         return backingIterator.next();
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

}//END OF Iterators
