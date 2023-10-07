package com.gengoai.collection.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.Objects;

/**
 * The type Simple span.
 */
@ToString
public class SimpleSpan implements Span {
    private static final long serialVersionUID = 1L;
    @JsonProperty("start")
    private int start;
    @JsonProperty("end")
    private int end;

    /**
     * Instantiates a new Simple span.
     *
     * @param start the start
     * @param end   the end
     */
    @JsonCreator
    public SimpleSpan(@JsonProperty("start") int start, @JsonProperty("end") int end) {
        if (start >= end) {
            this.end = start;
        } else {
            this.end = end;
        }
        this.start = start;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Span)) return false;
        Span that = (Span) o;
        return end == that.end() && start == that.start();
    }

    @Override
    public int hashCode() {
        return Objects.hash(end, start);
    }

    @Override
    public int length() {
        return end - start;
    }

    /**
     * Sets end.
     *
     * @param end the end
     */
    protected void setEnd(int end) {
        this.end = end;
    }

    /**
     * Sets start.
     *
     * @param start the start
     */
    protected void setStart(int start) {
        this.start = start;
    }

    @Override
    public int start() {
        return start;
    }

}//END OF SimpleSpan
