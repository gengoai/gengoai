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

import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import lombok.NonNull;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;

import java.io.IOException;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
class WritableIndex extends LuceneIndex {
   private MonitoredObject<IndexWriter> writer;
   private MonitoredObject<SearcherManager> manager;

   protected WritableIndex(@NonNull Resource location, @NonNull IndexConfig config) throws IOException {
      super(location, config);
   }

   public WritableIndex(@NonNull Resource location) throws IOException {
      super(location);
   }

   @Override
   public void close() throws IOException {
      commit();
      this.writer.object.close();
      this.manager.object.close();
   }


   @Override
   public LuceneIndexReader reader() throws IOException {
      manager.object.maybeRefreshBlocking();
      IndexSearcher searcher = manager.object.acquire();
      return new LuceneIndexReader() {

         @Override
         public void close() throws IOException {
            manager.object.release(searcher);
         }

         @Override
         public IndexReader openReader() {
            return searcher.getIndexReader();
         }
      };
   }

   @Override
   public LuceneIndexSearcher searcher() throws IOException {
      manager.object.maybeRefreshBlocking();
      IndexSearcher searcher = manager.object.acquire();
      return new LuceneIndexSearcher() {
         @Override
         public void close() throws IOException {
            manager.object.release(searcher);
         }

         @Override
         public IndexSearcher openSearcher() {
            return searcher;
         }
      };
   }

   @Override
   public LuceneIndexWriter writer() throws IOException {
      return new LuceneIndexWriter() {
         @Override
         public void close() throws IOException {
            manager.object.maybeRefreshBlocking();
         }

         @Override
         public IndexWriter writer() {
            return writer.object;
         }
      };
   }

   @Override
   protected void init() throws IOException {
      this.writer = ResourceMonitor.monitor(new IndexWriter(indexDirectory, this.getConfig().indexWriterConfig()));
      this.manager = ResourceMonitor.monitor(new SearcherManager(writer.object, new SearcherFactory()));
   }

}//END OF ReadWriteIndex
