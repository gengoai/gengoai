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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class SafeLuceneIndex extends LuceneIndex {


   public SafeLuceneIndex(@NonNull Directory directory, @NonNull String defaultField, @NonNull Analyzer analyzer) {
      super(directory, defaultField, analyzer);
   }

   public void addDocument(@NonNull Document document) throws IOException {
      try (IndexWriter writer = openWriter()) {
         writer.addDocument(document);
      }
   }

   public void addDocument(@NonNull Iterable<Document> document) throws IOException {
      try (IndexWriter writer = openWriter()) {
         writer.addDocuments(document);
      }
   }

   @Override
   public void commit() throws IOException {

   }

   @Override
   public Iterator<Document> iterator() {
      return null;
   }

   public void updateDocument(@NonNull Term term, @NonNull Document document) throws IOException {
      try (IndexWriter writer = openWriter()) {
         writer.updateDocument(term, document);
      }
   }

   public void updateDocuments(@NonNull Iterable<? extends Map.Entry<Term, Document>> documents) throws IOException {
      try (IndexWriter writer = openWriter()) {
         for (Map.Entry<Term, Document> document : documents) {
            writer.updateDocument(document.getKey(), document.getValue());
         }
      }
   }

   @Override
   public void close() throws Exception {

   }

   public void deleteDocuments(@NonNull Term... terms) throws IOException {
      try (IndexWriter writer = openWriter()) {
         writer.deleteDocuments(terms);
      }
   }

   public void deleteDocuments(@NonNull Query... queries) throws IOException {
      try (IndexWriter writer = openWriter()) {
         writer.deleteDocuments(queries);
      }
   }

   @Override
   public void forceMerge(int numberOfSegments) throws IOException {

   }

   @Override
   public Iterable<Document> get(@NonNull Term... term) throws IOException {
      return null;
   }

   @Override
   public void search(@NonNull Query query) throws IOException {

   }

}//END OF SafeLuceneIndex
