package com.gengoai.graph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A directed weighted edge implementation.
 *
 * @param <V> The type of vertex
 */
public class DirectedEdge<V> extends Edge<V> {
   private static final long serialVersionUID = 961303328216002925L;
   private double weight = 1d;

   @JsonCreator
   protected DirectedEdge(@JsonProperty("firstVertex") V vertex1,
                          @JsonProperty("secondVertex") V vertex2,
                          @JsonProperty("weight") double weight) {
      super(vertex1, vertex2);
      this.weight = weight;
   }

   @Override
   public boolean equals(Object obj) {
      if(obj == null) {
         return false;
      }
      if(obj == this) {
         return true;
      }
      if(obj instanceof DirectedEdge) {
         DirectedEdge otherEdge = (DirectedEdge) obj;
         return (Objects.equals(vertex1, otherEdge.vertex1) && Objects.equals(vertex2, otherEdge.vertex2));
      }
      return false;
   }

   @Override
   public double getWeight() {
      return weight;
   }

   @Override
   public int hashCode() {
      return 31 * (vertex1.hashCode()) + 37 * (vertex2.hashCode());
   }

   @Override
   public boolean isDirected() {
      return true;
   }

   @Override
   public boolean isWeighted() {
      return true;
   }

   @Override
   public void setWeight(double weight) {
      this.weight = weight;
   }

   @Override
   public String toString() {
      return "DirectedEdge{ " + vertex1 + " -> " + vertex2 + (isWeighted()
                                                              ? " : " + getWeight()
                                                              : "") + "}";
   }
}//END OF DirectedEdge
