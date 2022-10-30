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
public class PageRankTest {

  Graph<String> directed;
  Graph<String> undirected;

  @Before
  public void setUp() throws Exception {
    directed = Graph.directed();
    directed.addVertex("A");
    directed.addVertex("B");
    directed.addVertex("C");
    directed.addEdge("A", "B");
    directed.addEdge("A", "C");
    directed.addEdge("B", "C");
    directed.addEdge("C", "A");


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
    PageRank<String> pageRank = new PageRank<>();

    Counter<String> directedScore = pageRank.score(directed);
    assertEquals(1.16d, directedScore.get("A"), 0.01d);
    assertEquals(0.64d, directedScore.get("B"), 0.01d);
    assertEquals(1.19d, directedScore.get("C"), 0.01d);

    Counter<String> undirectedScore = pageRank.score(undirected);
    assertEquals(1.67d, undirectedScore.get("A"), 0.01d);
    assertEquals(1.08d, undirectedScore.get("B"), 0.01d);
    assertEquals(0.62d, undirectedScore.get("C"), 0.01d);
    assertEquals(0.62d, undirectedScore.get("D"), 0.01d);
  }

}//END OF PageRankTest
