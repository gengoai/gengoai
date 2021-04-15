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

package com.gengoai.documentdb.lucene;

import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.documentdb.*;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static com.gengoai.documentdb.lucene.LuceneDocumentUtils.parseQuery;
import static com.gengoai.documentdb.lucene.LuceneDocumentUtils.withSearchManager;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Log
public class LuceneDocumentDB implements DBTable {
   private final Resource tableFolder;
   private final Map<String, FieldType> indices;
   private final Directory directory;
   private final String primaryKey;
   private MonitoredObject<IndexWriter> writer;
   private MonitoredObject<SearcherManager> searcherManager;

   protected LuceneDocumentDB(@NonNull Resource storeLocation) throws DocumentDBException {
      try {
         this.tableFolder = storeLocation;
         directory = FSDirectory.open(storeLocation.getChild("index").asFile().orElseThrow().toPath());
         Map<String, JsonEntry> metadata = Json.parseObject(storeLocation.getChild("metadata.json"));
         this.indices = metadata.get("index").asMap(FieldType.class);
         this.primaryKey = metadata.get("primaryKey").asString();
         this.writer = ResourceMonitor.monitor(LuceneDocumentUtils.getIndexWriter(directory, this.indices));
         this.searcherManager = ResourceMonitor.monitor(new SearcherManager(this.writer.object, new SearcherFactory()));
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
   }

   protected LuceneDocumentDB(@NonNull Resource storeLocation, @NonNull IndexedField primaryKey) throws DocumentDBException {
      if (primaryKey.getType() != FieldType.String) {
         throw new DocumentDBException("LuceneDocumentDB only supports String primary keys");
      }
      try {
         this.tableFolder = storeLocation;
         directory = FSDirectory.open(storeLocation.getChild("index").asFile().orElseThrow().toPath());
         this.primaryKey = primaryKey.getName();
         this.indices = new HashMap<>();
         this.indices.put(primaryKey.getName(), FieldType.String);
         this.writer = ResourceMonitor.monitor(LuceneDocumentUtils.getIndexWriter(directory, this.indices));
         this.searcherManager = ResourceMonitor.monitor(new SearcherManager(this.writer.object, new SearcherFactory()));
         writeMetadata();
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
   }

   public static void main(String[] args) throws Exception {
      LuceneDBConnection connection = new LuceneDBConnection(Resources.from("/Users/ik/test_db"));
      boolean tableExists = connection.exists("people");


      DBTable peopleDB = tableExists
            ? connection.open("people")
            : connection.create("people", IndexedField.stringField("@id"), IndexedField.stringField("name"),
                                IndexedField.intField("age"),
                                IndexedField.intField("address.number"),
                                IndexedField.fullTextField("address.street"),
                                new IndexedField("dob", FieldType.Timestamp));

      List<String> streets = List.of("Elm St.", "Maple Dr.", "Apple Ln.", "Landmark Ln.");

      if (!tableExists) {
         var rnd = new Random();
         peopleDB.addAll(Iterables.asIterable(IntStream.range(0, 10_000)
                                                       .mapToObj(i -> {
                                                          DBDocument person = new DBDocument();
                                                          person.put("@id", Integer.toString(i));
                                                          person.put("name", Strings.randomHexString(10));
                                                          person.put("age", rnd.nextInt(100));
                                                          person.put("address", Map.of("number", rnd.nextInt(4000),
                                                                                       "street", streets
                                                                                             .get(rnd.nextInt(streets
                                                                                                                    .size()))
                                                          ));
                                                          person.put("dob", LocalDate.of(rnd.nextInt(121) + 1900,
                                                                                         rnd.nextInt(12) + 1,
                                                                                         rnd.nextInt(28) + 1));
                                                          return person;
                                                       }).iterator()));
      }

      System.out.println(peopleDB.numberOfDocuments());
      for (DBDocument search : peopleDB.search("address.street:\"Apple Ln.\" AND dob:[1977-01-01 TO 1977-12-31]")) {
         System.out.println(search);
      }

   }

   @Override
   public void addAll(@NonNull Iterable<DBDocument> documents) throws DocumentDBException {
      try {
         for (DBDocument document : documents) {
            Term pKey = new Term(primaryKey, Validation.notNullOrBlank(document.getAsString(primaryKey),
                                                                       () -> "Invalid Primary Key: '" +
                                                                             document.get(primaryKey) +
                                                                             "'"));
            writer.object.updateDocument(pKey, LuceneDocumentUtils.toDocument(document, indices));
         }
         writer.object.commit();
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
   }

   @Override
   public void addIndex(@NonNull IndexedField... indexedFields) throws DocumentDBException {
      int changed = 0;
      for (IndexedField indexedField : indexedFields) {
         if (indexedField.getName().equals(primaryKey) && indexedField.getType() != FieldType.String) {
            throw new DocumentDBException("LuceneDocumentDB only supports String primary keys");
         }
         FieldType old = indices.put(indexedField.getName(), indexedField.getType());
         if (old == null || old != indexedField.getType()) {
            changed++;
         }
      }
      try {
         writeMetadata();
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
      if (numberOfDocuments() == 0 || changed < 0) {
         return;
      }
      try {
         this.writer.object.close();
         this.searcherManager.object.close();

         this.writer = ResourceMonitor.monitor(LuceneDocumentUtils.getIndexWriter(directory, this.indices));
         this.searcherManager = ResourceMonitor.monitor(new SearcherManager(this.writer.object, new SearcherFactory()));
         for (DBDocument dbDocument : this) {
            writer.object.updateDocument(new Term(primaryKey, dbDocument.getAsString(primaryKey)),
                                         LuceneDocumentUtils.toDocument(dbDocument, indices));
         }
      } catch (IOException ioe) {
         throw new DocumentDBException(ioe);
      }
   }

   @Override
   public void close() throws Exception {
      directory.close();
      writer.object.close();
      searcherManager.object.close();
   }

   @Override
   public void commit() throws IOException {
      writer.object.commit();
   }

   @Override
   public DBDocument get(@NonNull Object id) throws DocumentDBException {
      return withSearchManager(searcherManager.object,
                               searcher -> {
                                  ScoreDoc[] r = searcher
                                        .search(new TermQuery(new Term(primaryKey, id.toString())), 1).scoreDocs;
                                  if (r.length > 0) {
                                     return Json.parse(searcher.getIndexReader().document(r[0].doc)
                                                               .get(LuceneDocumentUtils.STORED_JSON_FIELD), DBDocument.class);
                                  }
                                  return null;
                               });
   }

   @Override
   public boolean hasIndex(@NonNull String fieldName) {
      return indices.containsKey(fieldName);
   }

   @Override
   public Iterator<DBDocument> iterator() {
      try {
         searcherManager.object.maybeRefreshBlocking();
         return new LuceneDocumentIterator(ResourceMonitor.monitor(searcherManager.object.acquire().getIndexReader()));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public long numberOfDocuments() throws DocumentDBException {
      if (tableFolder.getChild("index").getChildren().isEmpty()) {
         return 0L;
      }
      return (long) withSearchManager(searcherManager.object, s -> s.getIndexReader().numDocs());
   }

   @Override
   public boolean remove(@NonNull DBDocument document) throws DocumentDBException {
      try {
         Term pKey = new Term(primaryKey, Validation.notNullOrBlank(document.getAsString(primaryKey),
                                                                    () -> "Invalid Primary Key: '" +
                                                                          document.get(primaryKey) +
                                                                          "'"));
         long deleted = writer.object.deleteDocuments(pKey);
         writer.object.commit();
         return deleted > 0;
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
   }

   @Override
   public List<DBDocument> search(String field, int numHits, float... vector) throws DocumentDBException {
      List<DBDocument> docs = new ArrayList<>();
      return docs;
//      try (IndexReader reader = getIndexReader()) {
//         return docs;
////         Counter<String> tf = ((LuceneEmbeddingAnalyzer) analyzer).analyze(EmbeddingField.f2s(vector));
////         BooleanQuery.Builder q = new BooleanQuery.Builder();
////         for (Map.Entry<String, Double> e : tf.entries()) {
////            var tq = new BoostQuery(new TermQuery(new Term(field, e.getKey())), e.getValue().floatValue());
////            var bc = new BooleanClause(tq, BooleanClause.Occur.SHOULD);
////            q.add(bc);
////         }
////         IndexSearcher searcher = new IndexSearcher(reader);
////         TopDocs results = searcher.search(q.build(), numHits);
////         for (ScoreDoc sd : results.scoreDocs) {
////            Document document = searcher.getIndexReader().document(sd.doc);
////            DBDocument out = Json.parse(document.get(STORED_JSON_FIELD), DBDocument.class);
////            out.put(SCORE_FIELD, sd.score);
////            docs.add(out);
////         }
////         return docs;
//      } catch (Exception e) {
//         throw new RuntimeException(e);
//      }
   }

   @Override
   public List<DBDocument> search(String query, int numHits) throws DocumentDBException {
      return withSearchManager(searcherManager.object,
                               searcher -> LuceneDocumentUtils.search(parseQuery(query, primaryKey, indices),
                                                                      numHits,
                                                                      searcher));
   }


   private void writeMetadata() throws IOException {
      Json.dump(Map.of("primaryKey", primaryKey, "index", this.indices), tableFolder.getChild("metadata.json"));
   }

   private static class LuceneDocumentIterator implements Iterator<DBDocument> {
      private final Bits liveDocs;
      private final MonitoredObject<IndexReader> reader;
      private int index = -1;

      private LuceneDocumentIterator(MonitoredObject<IndexReader> reader) {
         this.reader = reader;
         this.liveDocs = MultiBits.getLiveDocs(reader.object);
         advance();
      }

      @Override
      public boolean hasNext() {
         return index < reader.object.maxDoc();
      }

      @Override
      public DBDocument next() {
         if (index >= reader.object.maxDoc()) {
            throw new NoSuchElementException();
         }
         try {
            Document ld = reader.object.document(index);
            String rawJson = ld.get(LuceneDocumentUtils.STORED_JSON_FIELD);
            DBDocument doc = DBDocument.from(Json.parseObject(rawJson));
            advance();
            return doc;
         } catch (Exception e) {
            throw new RuntimeException(e);
         }

      }

      private void advance() {
         index++;
         while (index < reader.object.maxDoc() && liveDocs != null && !liveDocs.get(index)) {
            index++;
         }
      }
   }

}//END OF LuceneDocumentStore
