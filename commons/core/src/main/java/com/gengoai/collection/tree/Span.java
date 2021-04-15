package com.gengoai.collection.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

/**
 * <p>A Span represents a continuous segment with a start and end.</p>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = SimpleSpan.class)
public interface Span extends Serializable, Comparable<Span> {

   /**
    * Of span.
    *
    * @param start the start
    * @param end   the end
    * @return the span
    */
   static Span of(int start, int end) {
      return new SimpleSpan(start, end);
   }

   @Override
   default int compareTo(Span o) {
      if(o == null) {
         return -1;
      }
      int cmp = Integer.compare(start(), o.start());
      if(cmp == 0) {
         cmp = Integer.compare(end(), o.end());
      }
      return cmp;
   }

   /**
    * Returns true if the bounds of the other text do not extend outside the bounds of this text.
    *
    * @param other The other text to check if this one encloses
    * @return True if the two texts are in the same document and this text encloses the other, False otherwise
    */
   default boolean encloses(Span other) {
      return other != null && other.start() >= this.start() && other.end() <= this.end();
   }

   /**
    * The ending offset
    *
    * @return The ending offset (exclusive).
    */
   int end();

   /**
    * Checks if the span is empty (<code>start == end</code>)
    *
    * @return True if the span is empty, False if not
    */
   @JsonIgnore
   default boolean isEmpty() {
      return length() == 0 || start() < 0 || end() < 0;
   }

   /**
    * The length of the span
    *
    * @return The length of the span
    */
   int length();

   /**
    * Returns true if the bounds of other text are connected with the bounds of this text.
    *
    * @param other The other text to check if this one overlaps
    * @return True if the two texts are in the same document and overlap, False otherwise
    */
   default boolean overlaps(Span other) {
      return other != null && this.start() < other.end() && this.end() > other.start();
   }

   /**
    * The starting offset
    *
    * @return The start offset (inclusive).
    */
   int start();
}//END OF Span
