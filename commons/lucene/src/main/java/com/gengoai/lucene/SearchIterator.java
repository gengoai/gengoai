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

package com.gengoai.lucene;

import com.gengoai.LogUtils;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Log
class SearchIterator implements Spliterator<ScoredDocument> {
   private final MonitoredObject<LuceneIndexSearcher> searcher;
   private final ScoreDoc[] scoreDocs;
   private final long splitSize;
   private final IndexConfig config;
   private int current;
   private int end;

   public SearchIterator(@NonNull MonitoredObject<LuceneIndexSearcher> searcher,
                         @NonNull ScoreDoc[] scoreDocs,
                         @NonNull IndexConfig config,
                         Integer lo,
                         Integer hi) {
      this.searcher = searcher;
      this.scoreDocs = scoreDocs;
      this.config = config;
      this.current = lo == null
            ? 0
            : lo;
      this.end = hi == null
            ? scoreDocs.length
            : hi;
      this.splitSize = 500;
   }

   public static SearchIterator iteratorFor(@NonNull LuceneIndexSearcher searcherSupplier,
                                            @NonNull ScoreDoc[] scoreDocs,
                                            @NonNull IndexConfig schema) {
      return new SearchIterator(ResourceMonitor.monitor(searcherSupplier),
                                scoreDocs,
                                schema,
                                null,
                                null);
   }

   @Override
   public int characteristics() {
      return CONCURRENT;
   }

   @Override
   public long estimateSize() {
      return end - current;
   }

   @Override
   public boolean tryAdvance(Consumer<? super ScoredDocument> consumer) {
      if (current < end) {
         try {
            ScoreDoc sd = scoreDocs[current];
            consumer.accept(new ScoredDocument(
                  config.load(searcher.object.openSearcher().doc(sd.doc)),
                  sd.score));
         } catch (IOException e) {
            LogUtils.logSevere(log, e);
            throw new RuntimeException(e);
         }
         current++;
         return true;
      }
      return false;
   }

   @Override
   public Spliterator<ScoredDocument> trySplit() {
      int length = end - current;
      if (length < splitSize) {
         return null;
      }
      int lo = current + length / 2;
      int hi = end;
      end = lo;
      return new SearchIterator(searcher, scoreDocs, config, lo, hi);
   }
}//END OF LuceneSearchIterator
