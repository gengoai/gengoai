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
import lombok.NonNull;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;


class ReadOnlyIndex extends LuceneIndex {
   private MonitoredObject<IndexReader> reader;

   public ReadOnlyIndex(@NonNull Resource location, @NonNull IndexConfig indexConfig) throws IOException {
      super(location,indexConfig);
   }

   public ReadOnlyIndex(@NonNull Resource location) throws IOException {
      super(location);
   }

   @Override
   public void close() throws IOException {
      this.reader.object.close();
   }

   @Override
   public LuceneIndexReader reader() throws IOException {
      return new LuceneIndexReader() {
         @Override
         public void close() throws IOException {

         }

         @Override
         public IndexReader openReader() {
            return reader.object;
         }
      };
   }

   @Override
   public LuceneIndexSearcher searcher() throws IOException {
      return new LuceneIndexSearcher() {
         @Override
         public void close() throws IOException {

         }

         @Override
         public IndexSearcher openSearcher() {
            return new IndexSearcher(reader.object);
         }
      };
   }

   @Override
   public LuceneIndexWriter writer() throws IOException {
      throw new IllegalStateException("Cannot get a writer for a read only index");
   }

   @Override
   protected void init() throws IOException {
      this.reader = ResourceMonitor.monitor(DirectoryReader.open(indexDirectory));
   }
}//END OF ReadOnlyIndex
