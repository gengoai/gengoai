/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai.graph;

import com.gengoai.graph.io.GraphJson;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class GraphUtilsTest {


   Graph<String> d1;
   Graph<String> d2;

   @Before
   public void setUp() throws Exception {
      d1 = Graph.directed();
      d1.addVertex("A");
      d1.addVertex("B");
      d1.addVertex("C");
      d1.addEdge("A", "B");
      d1.addEdge("A", "C");
      d1.addEdge("C", "B");

      d2 = Graph.directed();
      d2.addVertex("A");
      d2.addVertex("B");
      d2.addVertex("C");
      d2.addVertex("D");
      d2.addEdge("A", "B", 20.0d);
      d2.addEdge("A", "D");
      d2.addEdge("B", "C");
   }

   @Test
   public void testUndirected() {
      Graph<String> g = Graph.undirected();
      g.addVertices(Arrays.asList("A", "B", "C"));
      g.addEdge("A", "B");
      g.addEdge("C", "B", 2.0);
      assertTrue(g.containsEdge("B", "A"));
      assertTrue(g.containsEdge("B", "C"));
      assertEquals(1.0, g.getWeight("A", "B"), 0);
      assertEquals(2.0, g.getWeight("C", "B"), 0);
   }

   @Test
   public void json() throws Exception {
      GraphJson<String> gJson = new GraphJson<>(String.class);
      Resource r = Resources.fromString();
      gJson.write(d1, r);
      Graph<String> d3 = gJson.read(r);
      assertEquals(d1, d3);
   }

   @Test
   public void testMergeNonEmpty() throws Exception {
      d1.merge(d2, EdgeMergeFunctions.<String>keepOriginal());
      assertEquals(4, d1.numberOfVertices());
      assertEquals(1d, d1.getWeight("A", "B"), 0d);
      assertTrue(d1.containsEdge("C", "B"));
      assertTrue(d1.containsEdge("B", "C"));

//      GraphMLReader<String> graphMLReader = new GraphMLReader<>(String.class);
//      GraphMLWriter<String> graphMlWriter = new GraphMLWriter<>();
//      graphMlWriter.setVertexEncoder(DefaultEncodersDecoders.defaultVertexEncoder());
//      graphMLReader.setVertexDecoder(DefaultEncodersDecoders.defaultVertexDecoder(String.class));
//      Resource r = new StringResource();
//      graphMlWriter.write(d1, r);
//
//
//      graphMLReader.setVertexDecoder(DefaultEncodersDecoders.defaultVertexDecoder(String.class));
//      Graph<String> g2 = graphMLReader.read(r);
   }

   @Test
   public void testMergeFromEmpty() throws Exception {
      Graph<String> d3 = Graph.directed();
      d1.merge(d3);
      assertEquals(3, d1.numberOfVertices());
   }

   @Test
   public void testMergeToEmpty() throws Exception {
      Graph<String> d3 = Graph.directed();
      d3.merge(d1);
      assertFalse(d3.isEmpty());
      assertTrue(d3.containsEdge("A", "B"));
      assertEquals(3, d3.numberOfVertices());
   }


}//END OF GraphUtilsTest
