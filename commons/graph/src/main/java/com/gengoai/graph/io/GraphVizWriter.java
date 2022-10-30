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

package com.gengoai.graph.io;

import com.gengoai.Validation;
import com.gengoai.collection.Index;
import com.gengoai.collection.Indexes;
import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;
import com.gengoai.graph.Vertex;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphVizWriter<V> implements Serializable {
   private final Map<String, String> graphProperties = new HashMap<>();
   private final Map<String, String> nodeProperties = new HashMap<>();
   @Getter
   @Setter
   @NonNull
   private VertexEncoder<V> vertexEncoder = DefaultEncodersDecoders.defaultVertexEncoder();
   @Getter
   @Setter
   @NonNull
   private EdgeEncoder<V> edgeEncoder = DefaultEncodersDecoders.defaultEdgeEncoder();

   public static void main(String[] args) throws Exception {
      Graph<String> g = Graph.directed();
      g.addVertices(List.of("A", "B", "C", "D", "E"));
      g.addEdges(List.of(Edge.directedEdge("A", "B"),
                         Edge.directedEdge("A", "C"),
                         Edge.directedEdge("C", "B"),
                         Edge.directedEdge("E", "B"),
                         Edge.directedEdge("A", "D"),
                         Edge.directedEdge("C", "E")));

      GraphVizWriter<String> writer = new GraphVizWriter<>();
      writer.setGraphSize(10, 10, true);
      writer.setGraphProperty("bgcolor", "green");
      writer.setRankDir("BT");
      writer.setNodeProperty("shape", "record");
      writer.setVertexEncoder(s -> {
         return Vertex.builder()
                      .label("<font color='\"'blue\">" + s + "</font> \\n <font color=\"red\"" + s.length() + "</font>")
                      .build();
      });
      writer.write(g, Resources.from("/home/ik/test.dot"));
   }

   private String escape(String input) {
      if(input == null || input.length() == 0) {
         return "\"" + Strings.EMPTY + "\"";
      }
      if(input.length() == 1) {
         return "\"" + input + "\"";
      }
      if(input.charAt(0) == '"' && input.charAt(input.length() - 1) == '"') {
         return input;
      }
      return "\"" + input + "\"";
   }

   public void setGraphProperty(String property, String value) {
      graphProperties.put(property, value);
   }

   public void setGraphSize(double width, double height, boolean force) {
      Validation.checkArgument(width > 0);
      Validation.checkArgument(height > 0);
      if(force) {
         graphProperties.put("size", width + "," + height + "!");
      } else {
         graphProperties.put("size", width + "," + height);
      }
   }

   public void setNodeProperty(String property, String value) {
      nodeProperties.put(property, value);
   }

   public void setNodeShape(String shape) {
      nodeProperties.put("shape", shape);
   }

   public void setRankDir(String rankidr) {
      graphProperties.put("rankdir", rankidr);
   }

   public void write(@NonNull Graph<V> graph,
                     @NonNull Resource location) throws IOException {
      location.setCharset(StandardCharsets.UTF_8);
      try(BufferedWriter writer = new BufferedWriter(location.writer())) {
         //Write the header
         if(graph.isDirected()) {
            writer.write("digraph G {");
         } else {
            writer.write("graph G {");
         }
         writer.newLine();

         if(graphProperties.size() > 0) {
            writer.write("graph [");
            for(Map.Entry<String, String> entry : graphProperties.entrySet()) {
               writer.write(entry.getKey());
               writer.write("=");
               writer.write(escape(entry.getValue()));
               writer.write(";");
            }
            writer.write("];");
            writer.newLine();
         }

         if(nodeProperties.size() > 0) {
            writer.write("node [");
            int index = 0;
            for(Map.Entry<String, String> entry : nodeProperties.entrySet()) {
               if(index > 0) {
                  writer.write(", ");
               }
               index++;
               writer.write(entry.getKey());
               writer.write("=");
               writer.write(escape(entry.getValue()));
            }
            writer.write("];");
            writer.newLine();
         }

         Index<V> vertexIndex = Indexes.indexOf(graph.vertices());

         for(V vertex : graph.vertices()) {
            Vertex vertexProps = vertexEncoder.encode(vertex);
            writer.write(Integer.toString(vertexIndex.getId(vertex)));
            writer.write(" [");
            writer.write("label=" + escape(vertexProps.getLabel()) + " ");
            for(Map.Entry<String, String> entry : vertexProps.getProperties().entrySet()) {
               writer.write(entry.getKey() + "=" + escape(entry.getValue()) + " ");
            }
            writer.write("];");
            writer.newLine();
         }

         for(Edge<V> edge : graph.edges()) {
            writer.write(Integer.toString(vertexIndex.getId(edge.getFirstVertex())));
            if(graph.isDirected()) {
               writer.write(" -> ");
            } else {
               writer.write(" -- ");
            }
            writer.write(Integer.toString(vertexIndex.getId(edge.getSecondVertex())));

            Map<String, String> edgeProps = edgeEncoder.encode(edge);
            if(edgeProps != null && !edgeProps.isEmpty()) {
               writer.write(" [");
               for(Map.Entry<String, String> entry : edgeProps.entrySet()) {
                  writer.write(entry.getKey() + "=" + escape(entry.getValue()) + " ");
               }
               writer.write("];");
            }

            writer.newLine();
         }

         //write the footer
         writer.write("}");
         writer.newLine();
      }
   }

}//END OF GraphVizWriter
