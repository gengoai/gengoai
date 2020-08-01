package com.gengoai.graph.algorithms;

import java.util.Iterator;

/**
 * Defines an iterator from a starting vertex to traverse the graph.
 *
 * @param <V> the vertex type parameter
 * @author David B. Bracewell
 */
public interface GraphTraversal<V> {

   /**
    * An iterator to traverse the graph starting at the given vertex
    *
    * @param startingPoint the starting point
    * @return the iterator
    */
   Iterator<V> iterator(V startingPoint);

}//END OF Visitor
