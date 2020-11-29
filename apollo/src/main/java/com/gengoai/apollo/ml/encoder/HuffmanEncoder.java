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

package com.gengoai.apollo.ml.encoder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.collection.IteratorSet;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Sets;
import com.gengoai.json.Json;
import com.gengoai.sql.SQL;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.string.Strings;
import lombok.*;

import java.util.*;

@NoArgsConstructor
public class HuffmanEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   @JsonProperty("alphabet")
   private final Map<String, HuffmanNode> nodes = new HashMap<>();
   private String[] vocab;
   @JsonProperty("unknown")
   private String unknownWord;

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   @AllArgsConstructor
   public static class HuffmanNode {
      byte[] code;
      int[] point;
      int index;
      int count;
   }


   public HuffmanEncoder(String unknownWord) {
      this.unknownWord = unknownWord;
   }

   @JsonCreator
   private HuffmanEncoder(@JsonProperty("alphabet") Map<String, HuffmanNode> alphabet,
                          @JsonProperty("unknown") String unknown) {
      this.nodes.putAll(alphabet);
      this.unknownWord = unknown;
      vocab = new String[alphabet.size()];
      for (Map.Entry<String, HuffmanNode> en : alphabet.entrySet()) {
         vocab[en.getValue().index] = en.getKey();
      }
   }

   public Encoder compress() {
      return new FixedEncoder(Arrays.asList(vocab), unknownWord);
   }

   public HuffmanNode getNode(String word) {
      return nodes.get(word);
   }

   public HuffmanNode getNode(double index) {
      return getNode(decode(index));
   }

   @Override
   public String decode(double index) {
      if (index >= 0 && index < vocab.length) {
         return vocab[(int) index];
      }
      return unknownWord;
   }

   @Override
   public int encode(String variableName) {
      if (nodes.containsKey(variableName)) {
         return nodes.get(variableName).index;
      } else if (unknownWord != null) {
         return nodes.get(unknownWord).index;
      }
      return -1;
   }

   @Override
   public void fit(@NonNull MStream<Observation> observations) {
      nodes.clear();
      vocab = null;

      Map<String, Long> wordCounts = observations.parallel()
                                                 .flatMap(Observation::getVariableSpace)
                                                 .map(Variable::getName)
                                                 .countByValue();

      int numberOfTokens = wordCounts.size();
      int[] parentNodes = new int[numberOfTokens * 2 + 1];
      byte[] binary = new byte[numberOfTokens * 2 + 1];
      long[] counts = new long[numberOfTokens * 2 + 1];
      int index = 0;
      for (Map.Entry<String, Long> entry : wordCounts.entrySet()) {
         counts[index] = entry.getValue();
         index++;
      }
      for (int i = numberOfTokens; i < counts.length; i++) {
         counts[i] = (long) 1e15;
      }
      createTree(parentNodes, counts, binary, wordCounts);
   }


   private void createTree(int[] parentNodes, long[] counts, byte[] binary, Map<String, Long> wordCounts) {
      int min1i;
      int min2i;
      int numberOfTokens = wordCounts.size();
      int pos1 = numberOfTokens - 1;
      int pos2 = numberOfTokens;


      for (int i = 0; i < numberOfTokens - 1; i++) {
         if (pos1 >= 0) {
            if (counts[pos1] < counts[pos2]) {
               min1i = pos1;
               pos1--;
            } else {
               min1i = pos2;
               pos2++;
            }
         } else {
            min1i = pos2;
            pos2++;
         }

         if (pos1 >= 0) {
            if (counts[pos1] < counts[pos2]) {
               min2i = pos1;
               pos1--;
            } else {
               min2i = pos2;
               pos2++;
            }
         } else {
            min2i = pos2;
            pos2++;
         }

         int newIndex = numberOfTokens + i;
         counts[newIndex] = counts[min1i] + counts[min2i];
         parentNodes[min1i] = newIndex;
         parentNodes[min2i] = newIndex;
         binary[min2i] = 1;
      }


      int vocabSize = wordCounts.size();
      vocab = new String[vocabSize];
      int nodeIndex = 0;
      for (Map.Entry<String, Long> entry : wordCounts.entrySet()) {
         int currentIndex = nodeIndex;

         ArrayList<Byte> code = new ArrayList<>();
         ArrayList<Integer> points = new ArrayList<>();
         while (currentIndex != (numberOfTokens * 2 - 2)) {
            code.add(binary[currentIndex]);
            points.add(currentIndex);
            currentIndex = parentNodes[currentIndex];
         }

         int codeLength = code.size();
         final int count = entry.getValue().intValue();
         final byte[] rawCode = new byte[codeLength];
         final int[] rawPoints = new int[codeLength + 1];
         rawPoints[0] = numberOfTokens - 2;
         for (int i = 0; i < codeLength; i++) {
            rawCode[codeLength - i - 1] = code.get(i);
            rawPoints[codeLength - i] = points.get(i) - numberOfTokens;
         }
         nodes.put(entry.getKey(), new HuffmanNode(rawCode, rawPoints, nodeIndex, count));
         vocab[nodeIndex] = entry.getKey();
         nodeIndex++;
      }


   }

   @Override
   public Set<String> getAlphabet() {
      return new IteratorSet<>(() -> Arrays.asList(vocab).iterator());
   }

   @Override
   public boolean isFixed() {
      return false;
   }

   @Override
   public int size() {
      return vocab.length;
   }
}
