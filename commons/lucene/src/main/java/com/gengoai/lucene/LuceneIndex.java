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
import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.concurrent.Broker;
import com.gengoai.concurrent.IterableProducer;
import com.gengoai.io.QuietIO;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.lucene.field.Fields;
import com.gengoai.stream.Streams;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * <p>Wrapper around and the components needed to read, write and search Lucene indices. Indices can be
 * <code>schemaless</code> or have a defined <code>schema</code>. </p>
 *
 * @author David B. Bracewell
 */
@Log
public abstract class LuceneIndex implements AutoCloseable, Iterable<IndexDocument> {
   /**
    * The subdirectory name that stores the Lucene index
    */
   public static final String INDEX_SUBDIRECTORY = "index";
   /**
    * The name of the json file containing the index information
    */
   public static final String INDEX_INFORMATION_JSON = "index.json";
   public static final String LUCENE_SPLIT_SIZE_CONFIG = "lucene.splitSize";
   public static final String LUCENE_COMMIT_INTERVAL_CONFIG = "lucene.commitInterval";
   /**
    * The constant GLOBAL_LOCK.
    */
   protected static final Object GLOBAL_LOCK = new Object();
   /**
    * The Index directory.
    */
   protected final Directory indexDirectory;
   @Getter
   private final IndexConfig config;
   @Getter
   private final Resource location;


   /**
    * Instantiates a new Lucene index.
    *
    * @param location the location
    * @throws IOException the io exception
    */
   protected LuceneIndex(@NonNull Resource location) throws IOException {
      this.location = location;
      Validation.checkState(this.location.getChild(INDEX_INFORMATION_JSON).exists(),
                            () -> "Missing " +
                                  INDEX_INFORMATION_JSON +
                                  " for index at " +
                                  location);
      this.config = Json.parse(location.getChild(INDEX_INFORMATION_JSON), IndexConfig.class);
      this.indexDirectory = FSDirectory.open(location.getChild(INDEX_SUBDIRECTORY).asPath().orElseThrow());
      init();
   }

   /**
    * Instantiates a new Lucene index.
    *
    * @param location    the location
    * @param indexConfig the index config
    * @throws IOException the io exception
    */
   protected LuceneIndex(Resource location, @NonNull IndexConfig indexConfig) throws IOException {
      this.location = location;
      Validation.checkState(!this.location.getChild(INDEX_INFORMATION_JSON).exists(),
                            () -> "Existing  " +
                                  INDEX_INFORMATION_JSON +
                                  " for index at " +
                                  location);
      this.config = indexConfig;
      this.indexDirectory = FSDirectory.open(location.getChild(INDEX_SUBDIRECTORY).asPath().orElseThrow());
      this.location.getChild(INDEX_INFORMATION_JSON).write(Json.dumpsPretty(indexConfig));
      init();
   }

   /**
    * Config lucene index config.
    *
    * @param indexLocation the index location
    * @return the lucene index config
    */
   public static IndexConfig.IndexConfigBuilder at(@NonNull File indexLocation) {
      return new IndexConfig.IndexConfigBuilder(Resources.fromFile(indexLocation));
   }

   /**
    * Config lucene index config.
    *
    * @param indexLocation the index location
    * @return the lucene index config
    */
   public static IndexConfig.IndexConfigBuilder at(@NonNull Resource indexLocation) {
      return new IndexConfig.IndexConfigBuilder(indexLocation);
   }

   /**
    * Config lucene index config.
    *
    * @param indexLocation the index location
    * @return the lucene index config
    */
   public static IndexConfig.IndexConfigBuilder at(@NonNull String indexLocation) {
      return new IndexConfig.IndexConfigBuilder(Resources.from(indexLocation));
   }

   /**
    * Opens an existing index.
    *
    * @param location the location of the index
    * @param readOnly True - open in read only mode, False - open in writable mode.
    * @return the LuceneIndex
    * @throws IOException Something went wrong opening the index.
    */
   public static LuceneIndex open(@NonNull Resource location, boolean readOnly) throws IOException {
      if (readOnly) {
         return new ReadOnlyIndex(location);
      }
      return new WritableIndex(location);
   }

   /**
    * Adds all of the given documents to the index.
    *
    * @param documents the documents
    * @throws IOException Something went wrong writing the document to the index
    */
   public void add(@NonNull IndexDocument... documents) throws IOException {
      add(Arrays.asList(documents));
   }

   /**
    * Adds all of the given documents to the index.  Note: that indices with a schema will perform an update to ensure
    * that there are no duplicate primary keys.
    *
    * @param documents the documents
    * @throws IOException Something went wrong writing the documents to the index
    */
   public void add(@NonNull Iterable<? extends IndexDocument> documents) throws IOException {
      try (LuceneIndexWriter writer = writer()) {
         writer.addDocuments(Iterables.transform(documents, id -> id.asIndexableFields(config)));
      }
   }

   /**
    * Clears (deletes) all documents in the index
    *
    * @throws IOException Something went wrong deleting
    */
   public void clear() throws IOException {
      try (LuceneIndexWriter writer = writer()) {
         writer.deleteAll();
      }
   }

   @Override
   public abstract void close() throws IOException;

   /**
    * Commits any changes to the index.
    *
    * @throws IOException Something went wrong during commit.
    */
   public void commit() throws IOException {
      try (LuceneIndexWriter writer = writer()) {
         writer.commit();
      }
   }

   /**
    * Compacts the index collapsing it into a maximum of the given number of segments.
    *
    * @param numberOfSegments the number of segments
    * @throws IOException Something went wrong during merge
    */
   public void compact(int numberOfSegments) throws IOException {
      Validation.checkArgument(numberOfSegments >= 1, "Must specify a number of segments >= 1.");
      try (LuceneIndexWriter writer = writer()) {
         writer.compact(numberOfSegments);
      }
   }

   /**
    * Contains boolean.
    *
    * @return the boolean
    * @throws IOException the io exception
    */
   public boolean contains(@NonNull String fieldName, @NonNull String fieldValue) throws IOException {
      return termCount(new Term(fieldName, fieldValue), FrequencyType.DOCUMENT_FREQUENCY) > 0;
   }

   /**
    * Deletes the document with the given id
    *
    * @param docId the id of the document to delete
    * @throws IOException Something went wrong deleting the document
    */
   public void delete(@NonNull String fieldName, @NonNull String docId) throws IOException {
      delete(fieldName, Collections.singleton(docId));
   }

   /**
    * Deletes documents with the given document ids
    *
    * @param docIds the ids of the documents to delete
    * @throws IOException Something went wrong deleting
    */
   public void delete(@NonNull String fieldName, @NonNull Collection<String> docIds) throws IOException {
      if (docIds.isEmpty()) {
         return;
      }
      try (LuceneIndexWriter writer = writer()) {
         writer.deleteDocuments(docIds.stream()
                                      .map(id -> new Term(fieldName, id))
                                      .toArray(Term[]::new));
      }
   }

   /**
    * Deletes document matching the given terms.
    *
    * @param terms the terms to determine which documents are deleted
    * @throws IOException Something went wrong deleting
    */
   public void deleteWhere(@NonNull Term... terms) throws IOException {
      try (LuceneIndexWriter writer = writer()) {
         writer.deleteDocuments(terms);
      }
   }

   /**
    * Deletes document matching the given queries.
    *
    * @param queries the queries to determine which documents are deleted
    * @throws IOException Something went wrong deleting
    */
   public void deleteWhere(@NonNull Query... queries) throws IOException {
      try (LuceneIndexWriter writer = writer()) {
         writer.deleteDocuments(queries);
      }
   }

   /**
    * Calculates the number of documents that contain the given field
    *
    * @param field the field name
    * @return the number of documents with the given field
    * @throws IOException Something went wrong calculating the count
    */
   public int fieldCount(@NonNull String field) throws IOException {
      try (LuceneIndexReader reader = reader()) {
         return reader.openReader().getDocCount(field);
      }
   }

   /**
    * Gets the fields defined in the index
    *
    * @return the names of the fields in the index
    * @throws IOException Something went wrong reading the index
    */
   public Set<String> fields() throws IOException {
      try (LuceneIndexReader reader = reader()) {
         Set<String> fieldDocCounter = new HashSet<>();
         for (LeafReaderContext leaf : reader.openReader().leaves()) {
            for (FieldInfo fieldInfo : leaf.reader().getFieldInfos()) {
               fieldDocCounter.add(fieldInfo.name);
            }
         }
         return fieldDocCounter;
      }
   }

   /**
    * Gets the document with the given id.
    *
    * @param documentId the id of the document to retrieve
    * @return the Document with the given id or an empty document.
    * @throws IOException Something went wrong retrieving the document
    */
   public IndexDocument get(@NonNull String idField, @NonNull String documentId) throws IOException {
      try (LuceneIndexSearcher searcher = searcher()) {
         ScoreDoc[] scoreDocs = searcher.openSearcher()
                                        .search(new TermQuery(new Term(idField, documentId)), 1).scoreDocs;
         if (scoreDocs.length > 0) {
            return config.load(searcher.openSearcher().doc(scoreDocs[0].doc));
         }
         return new ObjectDocument();
      }
   }

   @Override
   public Iterator<IndexDocument> iterator() {
      return stream().iterator();
   }

   /**
    * Returns a parallel stream of documents contained in this index.
    *
    * @return the stream
    */
   public Stream<IndexDocument> parallelStream() {
      try {
         return Streams.asParallelStream(DocumentIterator.iteratorFor(reader(), config));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Returns an {@link LuceneIndexReader} for this index.
    *
    * @return the index reader supplier
    * @throws IOException Something went wrong trying to create the reader supplier
    */
   public abstract LuceneIndexReader reader() throws IOException;

   /**
    * Searches the index using the given query returning upto <code>N</code> results.
    *
    * @param query the query to perform
    * @param N     the maximum number of results
    * @return the results of the Search
    * @throws IOException Something went wrong during the search.
    */
   public SearchResults search(@NonNull Query query, int N) throws IOException {
      LogUtils.logInfo(log, "Calling search(query=''{0}'', n={1})", query, N);
      LuceneIndexSearcher supplier = searcher();
      try {
         TopDocs topDocs = supplier.openSearcher().search(query, N);
         return new SearchResults(topDocs.totalHits.value,
                                  Streams.asParallelStream(SearchIterator.iteratorFor(supplier,
                                                                                      topDocs.scoreDocs,
                                                                                      config)));
      } catch (IOException e) {
         QuietIO.closeQuietly(supplier);
         throw e;
      }
   }

   /**
    * Gets a {@link LuceneIndexSearcher} for this index
    *
    * @return An LuceneIndexSearcher
    * @throws IOException Something went wrong creating the searcher
    */
   public abstract LuceneIndexSearcher searcher() throws IOException;

   /**
    * Size int.
    *
    * @return the int
    * @throws IOException the io exception
    */
   public int size() throws IOException {
      try (LuceneIndexReader reader = reader()) {
         return reader.openReader().numDocs();
      }
   }

   /**
    * Gets a stream over the documents in the index
    *
    * @return the stream of documents
    */
   public Stream<IndexDocument> stream() {
      try {
         return Streams.asStream(DocumentIterator.iteratorFor(reader(), config));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Calculates the frequency for the given term using the given frequency type.
    *
    * @param term          the term whose frequency we want to calculate
    * @param frequencyType how to calculate the count
    * @return the count of the term according to the frequency type.
    * @throws IOException Something went wrong calculating the count
    */
   public long termCount(@NonNull Term term, @NonNull FrequencyType frequencyType) throws IOException {
      try (LuceneIndexReader reader = reader()) {
         return frequencyType.getFrequency(term, reader.openReader());
      }
   }

   /**
    * Gets a count of the terms for a given field in the index. This only works for String and Text fields.
    *
    * @param name          the name of the field whose terms we want want to count
    * @param frequencyType how to calculate the count
    * @return the terms and their count for the given field
    * @throws IOException Something went wrong reading the index
    */
   public Counter<String> terms(@NonNull String name, @NonNull FrequencyType frequencyType) throws IOException {
      try (LuceneIndexReader reader = reader()) {
         Counter<String> termCounter = Counters.newCounter();
         for (LeafReaderContext leaf : reader.openReader().leaves()) {
            for (Terms term : new Terms[]{
                  leaf.reader().terms(name),
                  leaf.reader().terms(Fields.getStoredFieldName(name))}) {
               if (term != null) {
                  TermsEnum tenum = term.iterator();
                  while (tenum.next() != null) {
                     termCounter.increment(tenum.term().utf8ToString(),
                                           frequencyType.getFrequency(tenum));
                  }
               }
            }
         }
         return termCounter;
      }
   }

   /**
    * Updates all documents in the index.
    *
    * @param documentUpdater the document updater function
    * @throws IOException Something went wrong updating the index
    */
   public void update(@NonNull String idField, @NonNull DocumentUpdater documentUpdater) throws IOException {
      try (LuceneIndexWriter writerSupplier = writer()) {
         Broker<IndexDocument> broker = Broker.<IndexDocument>builder()
                                              .addProducer(new IterableProducer<>(this))
                                              .bufferSize(10_000)
                                              .addConsumer(new UpdateConsumer(documentUpdater,
                                                                              config,
                                                                              writerSupplier.writer(),
                                                                              idField),
                                                           Math.max(1, Runtime.getRuntime().availableProcessors() - 1))
                                              .build();
         broker.run();
      }
   }

   /**
    * Deletes all documents matching the given term and then adds the given documents in sequence.
    *
    * @param term      the term to use for deleting documents
    * @param documents the documents to add into the index
    * @throws IOException Something went wrong during the update
    */
   public void updateWhere(@NonNull Term term, @NonNull Iterable<? extends IndexDocument> documents) throws IOException {
      try (LuceneIndexWriter writer = writer()) {
         writer.updateDocuments(term, Iterables.transform(documents, id -> id.asIndexableFields(config)));
      }
   }

   /**
    * Gets a {@link LuceneIndexWriter} for this index
    *
    * @return the LuceneIndexWriter
    * @throws IOException Something went wrong creating the LuceneIndexWriter
    */
   public abstract LuceneIndexWriter writer() throws IOException;

   /**
    * Init.
    *
    * @throws IOException the io exception
    */
   protected abstract void init() throws IOException;


}//END OF LuceneIndex