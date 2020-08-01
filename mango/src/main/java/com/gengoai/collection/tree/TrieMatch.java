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

package com.gengoai.collection.tree;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>Represents a match in a Trie.</p>
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public class TrieMatch<V> implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The starting index.
    */
   public final int start;
   /**
    * The ending index.
    */
   public final int end;
   /**
    * The value of the matched key
    */
   public final V value;

   /**
    * Instantiates a new Trie match.
    *
    * @param start the start
    * @param end   the end
    * @param value the value
    */
   public TrieMatch(int start, int end, V value) {
      this.end = end;
      this.start = start;
      this.value = value;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TrieMatch)) return false;
      TrieMatch<?> trieMatch = (TrieMatch<?>) o;
      return start == trieMatch.start &&
                end == trieMatch.end &&
                Objects.equals(value, trieMatch.value);
   }

   /**
    * Extracts the matched portion of a content.
    *
    * @param content the content
    * @return the match
    */
   public String getMatch(String content) {
      return content.substring(start, end);
   }

   @Override
   public int hashCode() {
      return Objects.hash(start, end, value);
   }

   public String toString() {
      return "TrieMatch(start=" + this.start + ", end=" + this.end + ", value=" + this.value + ")";
   }

}//END OF TrieMatch
