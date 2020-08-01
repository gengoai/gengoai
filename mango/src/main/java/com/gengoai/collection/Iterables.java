package com.gengoai.collection;

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.function.SerializableSupplier;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.Validation.notNull;
import static com.gengoai.stream.Streams.asStream;

/**
 * Methods for manipulating iterables
 *
 * @author David B. Bracewell
 */
public final class Iterables {

    private Iterables() {
        throw new IllegalAccessError();
    }

    /**
     * Wraps an <code>array</code> as an <code>Iterable</code>
     *
     * @param <T>   the component type of the array
     * @param array The array to wrap
     * @return An Iterable wrapping the iterator.
     */
    public static <T> Iterable<T> asIterable(final Object array) {
        if (array == null) {
            return Collections.emptyList();
        }
        if (!array.getClass().isArray()) {
            return Cast.as(Collections.singleton(array));
        }
        return new IteratorIterable<>(() -> new Iterator<T>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < Array.getLength(array);
            }

            @Override
            public T next() {
                Validation.checkElementIndex(pos, Array.getLength(array));
                return Cast.as(Array.get(array, pos++));
            }
        });
    }

    /**
     * Wraps an <code>Iterator</code> as an <code>Iterable</code>
     *
     * @param <T>      the type parameter
     * @param iterator The iterator to wrap
     * @return An Iterable wrapping the iterator.
     */
    public static <T> Iterable<T> asIterable(@NonNull final Iterator<? extends T> iterator) {
        return new IteratorIterable<>(() -> Cast.as(iterator));
    }

    /**
     * Concatenates iterables together
     *
     * @param <T>       the iterables element type parameter
     * @param iterables the iterables to concatenate
     * @return the concatenated iterable
     */
    @SafeVarargs
    public static <T> Iterable<T> concat(@NonNull Iterable<? extends T>... iterables) {
        return new IteratorIterable<>(() -> Iterators.concat(iterables));
    }

    /**
     * Filters elements from the given iterable when the given filter predicate evaluates to false
     *
     * @param <E>       the iterable type parameter
     * @param iterable  the iterable to filter
     * @param predicate the filter to apply items evaluating to false will be removed from the iterator
     * @return the filtered iterable
     */
    public static <E> Iterable<E> filter(@NonNull final Iterable<? extends E> iterable,
                                         @NonNull final SerializablePredicate<? super E> predicate) {
        return new IteratorIterable<>(() -> Iterators.filter(iterable.iterator(), predicate));
    }

    /**
     * Flattens an iterable of iterables into a single iterable view.
     *
     * @param <T>      the iterable element type parameter
     * @param iterable the iterable to flatten
     * @return the flattened iterable
     */
    public static <T> Iterable<T> flatten(@NonNull Iterable<? extends Iterable<? extends T>> iterable) {
        final SerializableSupplier<Iterator<T>> supplier = () -> Iterators.flatten(
                Iterators.transform(iterable.iterator(), Iterable::iterator));
        return new IteratorIterable<>(Cast.as(supplier));
    }

    /**
     * Gets the element of the iterable at the given index using either <code>get</code> if the iterable is a list or by
     * iterating <code>index</code> times.
     *
     * @param <T>      the iterable element type parameter
     * @param iterable the iterable
     * @param index    the index of the element to retrieve
     * @return Optional of the element or empty when the index is out of bounds
     */
    public static <T> Optional<T> get(@NonNull Iterable<? extends T> iterable, int index) {
        checkArgument(index >= 0, "index must be >= 0");
        if (iterable instanceof List) {
            List<T> list = Cast.as(iterable);
            if (index > 0 && index < list.size()) {
                return Optional.ofNullable(list.get(index));
            }
            return Optional.empty();
        }
        return Iterators.get(iterable.iterator(), index);
    }

    /**
     * Gets the element of the iterable at the given index using either <code>get</code> if the iterable is a list or by
     * iterating <code>index</code> times.
     *
     * @param <T>          the iterable element type parameter
     * @param iterable     the iterable
     * @param index        the index of the element to retrieve
     * @param defaultValue value to return when the element is null or index is out of bounds
     * @return element value or default value
     */
    public static <T> T get(Iterable<? extends T> iterable, int index, T defaultValue) {
        return get(iterable, index).orElse(Cast.as(defaultValue));
    }

    /**
     * <p>Returns the first item in an iterable.</p>
     *
     * @param <T>      the type of element in the iterable
     * @param iterable the iterable
     * @return An optional containing the first element in the iterable if available
     */
    public static <T> Optional<T> getFirst(@NonNull Iterable<? extends T> iterable) {
        return Iterators.next(notNull(iterable).iterator());
    }

    /**
     * Gets the first element of the iterable.
     *
     * @param <T>          the iterable element type parameter
     * @param iterable     the iterable
     * @param defaultValue the default value
     * @return the first element of the iterable or the default value
     */
    public static <T> T getFirst(@NonNull Iterable<T> iterable, T defaultValue) {
        return Iterators.next(notNull(iterable).iterator(), defaultValue);
    }

    /**
     * <p>Returns the last item in an iterable. </p>
     *
     * @param <T>      the type of element in the iterable
     * @param iterable the iterable
     * @return An optional containing the last element in the iterable or null if none
     */
    public static <T> Optional<T> getLast(@NonNull Iterable<? extends T> iterable) {
        if (iterable instanceof List) {
            List<T> list = Cast.as(iterable);
            return Optional.ofNullable(list.get(list.size() - 1));
        }
        return Iterators.last(iterable.iterator());
    }

    /**
     * <p>Returns the last item in an iterable. </p>
     *
     * @param <T>          the type of element in the iterable
     * @param iterable     the iterable
     * @param defaultValue default value if the list is empty
     * @return An optional containing the last element in the iterable or null if none
     */
    public static <T> T getLast(@NonNull Iterable<? extends T> iterable, T defaultValue) {
        return getLast(notNull(iterable)).orElse(Cast.as(defaultValue));
    }

    /**
     * Determines if the iterable is empty or not.
     *
     * @param iterable the iterable to check
     * @return True if the iterable is empty, False otherwise
     */
    public static boolean isEmpty(@NonNull Iterable<?> iterable) {
        return !iterable.iterator().hasNext();
    }

    /**
     * Gets the size of the iterable
     *
     * @param iterable the iterable
     * @return the number of items in the iterable
     */
    public static int size(@NonNull Iterable<?> iterable) {
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).size();
        }
        return Iterators.size(iterable.iterator());
    }

    /**
     * <p>Sorts the items of an iterable returning an array of the sorted items.</p>
     *
     * @param <E>      The component type of the iterable which implements the <code>Comparable</code> interface.
     * @param iterable The iterable instance to sort
     * @return A list of the items in the given iterable sorted using the items natural comparator.
     */
    public static <E extends Comparable<? super E>> List<E> sort(@NonNull Iterable<? extends E> iterable) {
        return asStream(iterable).sorted().collect(Collectors.toList());
    }

    /**
     * <p>Sorts the items of an iterable returning an array of the sorted items.</p>
     *
     * @param <E>        The component type of the iterable.
     * @param iterable   The iterable instance to sort
     * @param comparator The comparator to use for sorting
     * @return A list of the items in the given iterable sorted using the given comparator.
     */
    public static <E> List<E> sort(@NonNull Iterable<? extends E> iterable, @NonNull Comparator<? super E> comparator) {
        return asStream(iterable).sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Transforms the elements in the iterable
     *
     * @param <I>      the iterable element type parameter
     * @param <O>      the transformed element type parameter
     * @param iterable the iterable to transform
     * @param function the function to perform transform
     * @return the transformed iterable
     */
    public static <I, O> Iterable<O> transform(@NonNull final Iterable<? extends I> iterable,
                                               @NonNull final SerializableFunction<? super I, ? extends O> function) {
        return new IteratorIterable<>(() -> Iterators.transform(iterable.iterator(), function));
    }

    /**
     * <p>Zips (combines) two iterators together. For example, if iterable 1 contained [1,2,3] and iterable 2 contained
     * [4,5,6] the result would be [(1,4), (2,5), (3,6)]. Note that the length of the resulting stream will be the
     * minimum of the two iterables.</p>
     *
     * @param <T>       the component type of the first iterator
     * @param <U>       the component type of the second iterator
     * @param iterable1 the iterator making up the key in the resulting entries
     * @param iterable2 the iterator making up the value in the resulting entries
     * @return A stream of entries whose keys are taken from iterable1 and values are taken from iterable2
     */
    public static <T, U> Iterable<Map.Entry<T, U>> zip(@NonNull final Iterable<? extends T> iterable1,
                                                       @NonNull final Iterable<? extends U> iterable2) {
        return new IteratorIterable<>(() -> Iterators.zip(iterable1.iterator(), iterable2.iterator()));
    }

    /**
     * Creates pairs of entries from the given iterable and its index in the iterable (0 based)
     *
     * @param <T>      the iterator type parameter
     * @param iterable the iterator
     * @return the iterable with index values
     */
    public static <T> Iterable<Map.Entry<T, Integer>> zipWithIndex(@NonNull final Iterable<? extends T> iterable) {
        return new IteratorIterable<>(() -> Iterators.zipWithIndex(iterable.iterator()));
    }

    private static class IteratorIterable<T> implements Iterable<T> {
        private final Supplier<Iterator<? extends T>> supplier;

        private IteratorIterable(Supplier<Iterator<? extends T>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Iterable)) return false;
            Iterable<?> that = (Iterable<?>) o;
            return Objects.equals(supplier.get(), that);
        }

        @Override
        public Iterator<T> iterator() {
            return Cast.cast(supplier.get());
        }

        @Override
        public String toString() {
            return Streams.asStream(this)
                    .map(Object::toString)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

    }

}//END OF Iterables
