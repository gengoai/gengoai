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


import lombok.NonNull;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Map;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public abstract class LuceneIndex implements Iterable<Document>, AutoCloseable {
   protected final Analyzer analyzer;
   protected final String defaultField;
   protected final Directory directory;

   public LuceneIndex(@NonNull Directory directory,
                      @NonNull String defaultField,
                      @NonNull Analyzer analyzer) {
      this.analyzer = analyzer;
      this.defaultField = defaultField;
      this.directory = directory;
   }


   public abstract void addDocument(@NonNull Document document) throws IOException;

   public abstract void addDocument(@NonNull Iterable<Document> document) throws IOException;

   public abstract void commit() throws IOException;

   public abstract void deleteDocuments(@NonNull Term... terms) throws IOException;

   public abstract void deleteDocuments(@NonNull Query... queries) throws IOException;

   public abstract void forceMerge(int numberOfSegments) throws IOException;

   public abstract Iterable<Document> get(@NonNull Term... term) throws IOException;

   public abstract void search(@NonNull Query query) throws IOException;

   public abstract void updateDocument(@NonNull Term term, @NonNull Document document) throws IOException;

   public abstract void updateDocuments(@NonNull Iterable<? extends Map.Entry<Term, Document>> documents) throws IOException;

   protected IndexWriterConfig getWriterConfig() {
      IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
      writerConfig.setCommitOnClose(true);
      writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
      return writerConfig;
   }

   protected final IndexReader openReader() throws IOException {
      return DirectoryReader.open(directory);
   }

   protected final IndexWriter openWriter() throws IOException {
      return new IndexWriter(directory, getWriterConfig());
   }

}//END OF LuceneIndex
