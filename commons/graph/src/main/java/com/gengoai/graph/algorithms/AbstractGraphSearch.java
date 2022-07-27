package com.gengoai.graph.algorithms;

import com.gengoai.collection.Lists;
import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;
import com.gengoai.tuple.Tuple2;

import java.io.Serializable;
import java.util.*;

import static com.gengoai.collection.Lists.arrayListOf;
import static com.gengoai.collection.Lists.asArrayList;
import static com.gengoai.collection.Sets.hashSetOf;
import static com.gengoai.tuple.Tuples.$;

abstract class AbstractGraphSearch<V> implements GraphSearch<V>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Graph<V> graph;

   /**
    * Instantiates a new Abstract graph search.
    *
    * @param graph the graph
    */
   public AbstractGraphSearch(Graph<V> graph) {
      this.graph = graph;
   }

   protected abstract void add(Deque<List<Tuple2<V, Edge<V>>>> pathes, List<Tuple2<V, Edge<V>>> path);

   protected abstract List<Tuple2<V, Edge<V>>> next(Deque<List<Tuple2<V, Edge<V>>>> pathes);

   @Override
   public final List<Edge<V>> search(V startingPoint, V endingPoint) {
      Deque<List<Tuple2<V, Edge<V>>>> pathes = new LinkedList<>();
      Set<V> visited = hashSetOf(startingPoint);
      graph.getEdges(startingPoint)
           .forEach(edge -> {
              Tuple2<V, Edge<V>> entry = $(startingPoint, edge);
              add(pathes, arrayListOf(entry));
           });

      while (pathes.size() > 0) {
         List<Tuple2<V, Edge<V>>> path = next(pathes);
         Tuple2<V, Edge<V>> last = path.get(path.size() - 1);
         V nextV = last.v2.getOppositeVertex(last.v1);
         if (nextV.equals(endingPoint)) {
            return Lists.transform(path, Tuple2::getV2);
         }
         if (visited.contains(nextV)) {
            continue;
         }
         visited.add(nextV);

         graph.getEdges(nextV)
              .stream()
              .filter(edge -> !visited.contains(edge.getOppositeVertex(nextV)))
              .forEachOrdered(edge -> {
                 List<Tuple2<V, Edge<V>>> newPath = asArrayList(path);
                 newPath.add($(nextV, edge));
                 add(pathes, newPath);
              });
      }


      return Collections.emptyList();
   }
}//END OF AbstractGraphSearch
