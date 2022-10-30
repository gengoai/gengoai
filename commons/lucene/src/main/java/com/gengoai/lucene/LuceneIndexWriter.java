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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public interface LuceneIndexWriter extends AutoCloseable {

   default void addDocument(@NonNull Iterable<? extends IndexableField> document) throws IOException {
      writer().addDocument(document);
   }

   default void addDocuments(@NonNull Iterable<? extends Iterable<? extends IndexableField>> documents) throws IOException {
      writer().addDocuments(documents);
   }

   @Override
   void close() throws IOException;

   default void commit() throws IOException {
      writer().commit();
   }

   default void compact(int numberOfSegments) throws IOException {
      writer().forceMerge(numberOfSegments, true);
      writer().deleteUnusedFiles();
      writer().commit();
   }

   default void deleteAll() throws IOException {
      writer().deleteAll();
   }

   default void deleteDocuments(@NonNull Term... terms) throws IOException {
      writer().deleteDocuments(terms);
   }

   default void deleteDocuments(@NonNull Query... queries) throws IOException {
      writer().deleteDocuments(queries);
   }

   default void flush() throws IOException {
      writer().flush();
   }

   default void updateDocument(@NonNull Term deleteTerm, @NonNull Iterable<? extends IndexableField> document) throws IOException {
      writer().updateDocument(deleteTerm, document);
   }

   default void updateDocuments(@NonNull Term deleteTerm, @NonNull Iterable<? extends Iterable<? extends IndexableField>> documents) throws IOException {
      writer().updateDocuments(deleteTerm, documents);
   }


   IndexWriter writer();


}//END OF LuceneIndexWriter
