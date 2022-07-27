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

import com.gengoai.config.Config;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;

import static com.gengoai.lucene.LuceneIndex.LUCENE_SPLIT_SIZE_CONFIG;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
class DocumentIterator implements Spliterator<IndexDocument> {
   private final Bits liveDocs;
   private final MonitoredObject<LuceneIndexReader> reader;
   private final long splitSize;
   private final IndexConfig config;
   private int current;
   private int end;

   DocumentIterator(@NonNull MonitoredObject<LuceneIndexReader> reader,
                    @NonNull IndexConfig config,
                    Integer lo,
                    Integer hi) {
      this.liveDocs = MultiBits.getLiveDocs(reader.object.openReader());
      this.reader = reader;
      this.config = config;
      this.current = lo == null
            ? 0
            : lo;
      this.end = hi == null
            ? reader.object.openReader().maxDoc()
            : hi;
      this.splitSize = Config.get(LUCENE_SPLIT_SIZE_CONFIG).asLongValue(5000);
   }


   public static DocumentIterator iteratorFor(@NonNull LuceneIndexReader readerSupplier, @NonNull IndexConfig config) {
      return new DocumentIterator(ResourceMonitor.monitor(readerSupplier), config, null, null);
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
   public boolean tryAdvance(Consumer<? super IndexDocument> consumer) {
      while (current < end && liveDocs != null && !liveDocs.get(current)) {
         current++;
      }
      if (current < end) {
         try {
            consumer.accept(config.load(reader.object.openReader().document(current)));
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
         current++;
         return true;
      }
      return false;
   }

   @Override
   public Spliterator<IndexDocument> trySplit() {
      int length = end - current;
      if (length < splitSize) {
         return null;
      }
      int lo = current + length / 2;
      int hi = end;
      end = lo;
      return new DocumentIterator(reader, config, lo, hi);
   }

}//END OF LuceneDocumentIterator
