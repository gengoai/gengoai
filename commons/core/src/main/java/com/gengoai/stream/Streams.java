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

package com.gengoai.stream;

import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableSupplier;
import com.gengoai.io.CharsetDetectingReader;
import com.gengoai.io.QuietIO;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

import static com.gengoai.Validation.notNull;
import static com.gengoai.tuple.Tuples.$;


/**
 * Convenience methods for creating and manipulating Java streams
 *
 * @author David B. Bracewell
 */
public final class Streams {

    private Streams() {
        throw new IllegalAccessError();
    }


    public static <T> Stream<T> reusableStream(@NonNull Collection<T> collection) {
        return new ReusableJavaStream<>(collection::stream);
    }

    public static <T> Stream<T> reusableStream(@NonNull SerializableSupplier<Stream<T>> streamSupplier) {
        return new ReusableJavaStream<>(streamSupplier);
    }


    public static <T> Stream<T> reusableParallelStream(@NonNull Collection<T> collection) {
        return new ReusableJavaStream<>(collection::parallelStream);
    }


    @SafeVarargs
    public static <T> Stream<T> reusableStream(@NonNull T... objects) {
        if (objects == null) {
            return new ReusableJavaStream<>(Stream::empty);
        }
        return new ReusableJavaStream<>(() -> Stream.of(objects));
    }

    public static IntStream reusableIntStream(int... objects) {
        if (objects == null || objects.length == 0) {
            return new ReusableJavaIntStream(IntStream::empty);
        }
        return new ReusableJavaIntStream(() -> IntStream.of(objects));
    }

    public static IntStream reusableIntStream(@NonNull SerializableSupplier<IntStream> streamSupplier) {
        return new ReusableJavaIntStream(streamSupplier);
    }

    public static LongStream reusableLongStream(long... objects) {
        if (objects == null) {
            return new ReusableJavaLongStream(LongStream::empty);
        }
        return new ReusableJavaLongStream(() -> LongStream.of(objects));
    }

    public static LongStream reusableLongStream(@NonNull SerializableSupplier<LongStream> streamSupplier) {
        return new ReusableJavaLongStream(streamSupplier);
    }

    public static DoubleStream reusableDoubleStream(@NonNull double... objects) {
        if (objects == null) {
            return new ReusableJavaDoubleStream(DoubleStream::empty);
        }
        return new ReusableJavaDoubleStream(() -> DoubleStream.of(objects));
    }

    public static DoubleStream reusableDoubleStream(@NonNull SerializableSupplier<DoubleStream> streamSupplier) {
        return new ReusableJavaDoubleStream(streamSupplier);
    }

    /**
     * Creates a <code>Stream</code> of <code>String</code> lines from an <code>InputStream</code> using a {@link
     * CharsetDetectingReader} to read from the input stream.
     *
     * @param stream the stream to read from
     * @return the stream of strings from the input stream
     */
    public static Stream<String> asStream(InputStream stream) {
        return asStream(new CharsetDetectingReader(notNull(stream)));
    }

    /**
     * Creates a <code>Stream</code> of <code>String</code> lines from an <code>Reader</code> by converting the reader
     * into a
     * <code>BufferedReader</code> and calling the lines method
     *
     * @param reader the reader to read from
     * @return the stream of strings from the input stream
     */
    public static Stream<String> asStream(Reader reader) {
        notNull(reader);
        return ((reader instanceof BufferedReader)
                ? Cast.<BufferedReader>as(reader)
                : new BufferedReader(reader))
                .lines()
                .onClose(() -> QuietIO.closeQuietly(reader));
    }


    /**
     * Creates a parallel stream from a given iterator
     *
     * @param <T>      the element type of the iterator parameter
     * @param iterator the iterator
     * @return the stream
     */
    public static <T> Stream<T> asParallelStream(Iterator<? extends T> iterator) {
        return asStream(iterator, true);
    }

    /**
     * Creates a parallel stream for a given iterable
     *
     * @param <T>      the element type of the iterable parameter
     * @param iterable the iterable
     * @return the stream
     */
    public static <T> Stream<T> asParallelStream(Iterable<? extends T> iterable) {
        return asStream(iterable, true);
    }

    /**
     * Creates a <code>Stream</code>  from a number of arguments returning an empty string if nothing is passed
     *
     * @param <T>    the value type parameter
     * @param values the values
     * @return the stream
     */
    @SafeVarargs
    public static <T> Stream<T> asStream(T... values) {
        if (values == null) {
            return Stream.empty();
        }
        return Stream.of(values);
    }

    /**
     * Creates a stream from a given iterator
     *
     * @param <T>      the element type of the iterator parameter
     * @param iterator the iterator
     * @return the stream
     */
    public static <T> Stream<T> asStream(Iterator<? extends T> iterator) {
        return asStream(iterator, false);
    }

    /**
     * Creates a parallel stream from a given iterator
     *
     * @param <T>      the element type of the iterator parameter
     * @param iterator the iterator
     * @param parallel True - create a parallel stream, False create a sequential stream
     * @return the stream
     */
    public static <T> Stream<T> asStream(Iterator<? extends T> iterator, boolean parallel) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                parallel);
    }

    /**
     * Convenience method for creating a stream from an iterable
     *
     * @param <T>      the iterable element type parameter
     * @param iterable the iterable
     * @return the stream
     */
    public static <T> Stream<T> asStream(Iterable<? extends T> iterable) {
        return asStream(iterable, false);
    }

    /**
     * Convenience method for creating a stream from an iterable
     *
     * @param <T>      the iterable element type parameter
     * @param iterable the iterable
     * @param parallel True create a parallel stream, False create sequential stream
     * @return the stream
     */
    public static <T> Stream<T> asStream(Iterable<? extends T> iterable, boolean parallel) {
        return StreamSupport.stream(Cast.as(notNull(iterable).spliterator()), parallel);
    }

    /**
     * <p>Zips (combines) items from both streams as tuples. Length of the new stream will the minimum length of the
     * two
     * streams.</p>
     *
     * @param <T>     the first stream's element type parameter
     * @param <U>     the second stream's element type parameter
     * @param stream1 the first stream * @param stream2 the second stream
     * @return the zipped stream
     */
    public static <T, U> Stream<Map.Entry<T, U>> zip(final Stream<? extends T> stream1,
                                                     final Stream<? extends U> stream2
    ) {
        final Spliterator<? extends T> s1 = stream1.spliterator();
        final Spliterator<? extends U> s2 = stream2.spliterator();
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Map.Entry<T, U>>(Long.min(s1.estimateSize(), s2.estimateSize()),
                        s1.characteristics() & s2.characteristics()) {

                    @Override
                    public boolean tryAdvance(Consumer<? super Map.Entry<T, U>> consumer) {
                        return s1.tryAdvance(t -> s2.tryAdvance(u -> consumer.accept($(t, u))));
                    }
                }, false);
    }

    /**
     * <p>Zips (combines) items from a stream with an integer in order of access. </p>
     *
     * @param <T>    the stream's type parameter
     * @param stream the stream
     * @return the stream
     */
    public static <T> Stream<Map.Entry<T, Long>> zipWithIndex(Stream<T> stream) {
        final Spliterator<T> backing = stream.spliterator();
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Map.Entry<T, Long>>(backing.estimateSize(),
                        backing.characteristics()) {
                    final Spliterator<Long> indexer = LongStream.range(0, Long.MAX_VALUE).spliterator();

                    @Override
                    public boolean tryAdvance(Consumer<? super Map.Entry<T, Long>> consumer) {
                        return backing.tryAdvance(t -> indexer.tryAdvance(l -> consumer.accept($(t, l))));
                    }
                }, false);
    }

    /**
     * Flattens an iterable of iterable into a single stream
     *
     * @param <T>       the type parameter
     * @param iterables the iterables
     * @return the stream
     */
    public static <T> Stream<T> flatten(Iterable<? extends Iterable<? extends T>> iterables) {
        return asStream(notNull(iterables)).flatMap(Streams::asStream);
    }

    /**
     * Concatenates two collections into a single stream
     *
     * @param <T> the type parameter
     * @param c1  the first collection
     * @param c2  the second collection
     * @return the stream
     */
    public static <T> Stream<T> union(Collection<? extends T> c1, Collection<? extends T> c2) {
        return Stream.concat(notNull(c1).stream(), notNull(c2).stream());
    }

    /**
     * Creates a new stream that is the intersection of the two collections
     *
     * @param <T> the type parameter
     * @param c1  the first collection
     * @param c2  the second collection
     * @return the stream
     */
    public static <T> Stream<T> intersection(Collection<? extends T> c1, Collection<? extends T> c2) {
        notNull(c2);
        return Cast.as(notNull(c1).stream().filter(c2::contains));
    }

    /**
     * Creates a new stream that has all elements of the first collection that are not in the second collection
     *
     * @param <T> the type parameter
     * @param c1  the first collection
     * @param c2  the second collection
     * @return the stream
     */
    public static <T> Stream<T> difference(Collection<? extends T> c1, Collection<? extends T> c2) {
        notNull(c2);
        return Cast.as(notNull(c1).stream().filter(v -> !c2.contains(v)));
    }


    public static <T> Stream<Stream<T>> partition(Stream<T> stream, long partitionSize) {
        final Spliterator<T> backing = stream.spliterator();
        final long expSize = backing.estimateSize() == Long.MAX_VALUE
                ? Long.MAX_VALUE
                : backing.estimateSize() / partitionSize;
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<Stream<T>>(expSize, backing.characteristics()) {
            private Stream.Builder<T> builder = Stream.builder();
            private boolean firstPartition = true;

            @Override
            public boolean tryAdvance(Consumer<? super Stream<T>> consumer) {
                boolean hasMore = true;
                if (firstPartition) {
                    hasMore = backing.tryAdvance(builder::add);
                    firstPartition = false;
                }
                if (!hasMore) {
                    consumer.accept(builder.build());
                    return false;
                }

                for (long i = 1; i < partitionSize && hasMore; i++) {
                    hasMore = backing.tryAdvance(builder::add);
                }
                consumer.accept(builder.build());

                if (hasMore) {
                    builder = Stream.builder();
                    hasMore = backing.tryAdvance(builder::add);
                }

                return hasMore;
            }
        }, false);
    }

}//END OF Streams
