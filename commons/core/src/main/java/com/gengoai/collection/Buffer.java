package com.gengoai.collection;

import com.gengoai.Validation;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.function.Consumer;

public class Buffer<T> {
    private final LinkedList<T> buffer = new LinkedList<>();
    private final Consumer<Iterable<T>> onDrain;
    private final int maxSize;

    /**
     * Constructs a new Buffer object with the given onDrain callback.
     *
     * @param onDrain the callback to be executed when the buffer is drained
     */
    public Buffer(int maxSize, @NonNull Consumer<Iterable<T>> onDrain) {
        Validation.checkArgument(maxSize > 0, "maxSize must be > 0");
        this.onDrain = onDrain;
        this.maxSize = maxSize;
    }

    /**
     * Adds an item to the buffer and drains if the buffer is full.
     *
     * @param item the item to add
     */
    public void add(T item) {
        synchronized (buffer) {
            buffer.add(item);
            if (buffer.size() >= maxSize) {
                onDrain.accept(buffer);
                buffer.clear();
            }
        }
    }

    /**
     * Drains the buffer
     */
    public void drain() {
        synchronized (buffer) {
            if (!buffer.isEmpty()) {
                onDrain.accept(buffer);
                buffer.clear();
            }
        }
    }


}//END Buffer
