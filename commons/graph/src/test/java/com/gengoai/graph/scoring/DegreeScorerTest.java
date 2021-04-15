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

package com.gengoai.graph.scoring;

import com.gengoai.collection.counter.Counter;
import com.gengoai.graph.Graph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class DegreeScorerTest {

   Graph<String> directed;
   Graph<String> undirected;

   @Before
   public void setUp() throws Exception {
      directed = Graph.directed();
      directed.addVertex("A");
      directed.addVertex("B");
      directed.addVertex("C");
      directed.addVertex("D");
      directed.addEdge("A", "B");
      directed.addEdge("A", "C");
      directed.addEdge("A", "D");
      directed.addEdge("B", "B");
      directed.addEdge("D", "A");


      undirected = Graph.undirected();
      undirected.addVertex("A");
      undirected.addVertex("B");
      undirected.addVertex("C");
      undirected.addVertex("D");
      undirected.addEdge("A", "B");
      undirected.addEdge("A", "C");
      undirected.addEdge("A", "D");
      undirected.addEdge("B", "B");
   }

   @Test
   public void testScore() throws Exception {
      DegreeScorer<String> scorer = new DegreeScorer<>(DegreeScorer.DEGREE_TYPE.IN);

      Counter<String> directedIN = scorer.score(directed);
      Counter<String> undirectedIN = scorer.score(undirected);

      scorer = new DegreeScorer<>(DegreeScorer.DEGREE_TYPE.OUT);
      Counter<String> directedOUT = scorer.score(directed);
      Counter<String> undirectedOUT = scorer.score(undirected);

      scorer = new DegreeScorer<>(DegreeScorer.DEGREE_TYPE.TOTAL);
      Counter<String> directedTOTAL = scorer.score(directed);
      Counter<String> undirectedTOTAL = scorer.score(undirected);

      /*
       * Test the undirected graph
       */
      for (String v : undirected.vertices()) {
         assertEquals(undirectedIN.get(v), undirectedOUT.get(v), 0.1);
         assertEquals(undirectedIN.get(v), undirectedTOTAL.get(v), 0.1);
      }
      assertEquals(3, undirectedOUT.get("A"), 0.1);
      assertEquals(2, undirectedOUT.get("B"), 0.1);
      assertEquals(1, undirectedOUT.get("C"), 0.1);
      assertEquals(1, undirectedOUT.get("D"), 0.1);

      /*
       * Test the directed graph
       */
      for (String v : directed.vertices()) {
         assertEquals(directedTOTAL.get(v), directedOUT.get(v) + directedIN.get(v), 0.1);
      }
      assertEquals(3, directedOUT.get("A"), 0.1);
      assertEquals(1, directedOUT.get("B"), 0.1);
      assertEquals(0, directedOUT.get("C"), 0.1);
      assertEquals(1, directedOUT.get("D"), 0.1);

      assertEquals(1, directedIN.get("A"), 0.1);
      assertEquals(2, directedIN.get("B"), 0.1);
      assertEquals(1, directedIN.get("C"), 0.1);
      assertEquals(1, directedIN.get("D"), 0.1);

   }
}//END OF DegreeScorerTest
