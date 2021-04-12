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

import com.gengoai.LogUtils;
import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.documentdb.FieldType;
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Log
public class LuceneDocumentDB implements DBTable {
   public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;
   public static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
   public static final String SCORE_FIELD = "___SCORE___";
   public static final String STORED_JSON_FIELD = "___RAW___";
   private final Resource tableFolder;
   private final Map<String, FieldType> indices;
   private final Directory directory;
   private final String primaryKey;
   private final MonitoredObject<IndexWriter> writer;
   private final MonitoredObject<SearcherManager> searcherManager;

   protected LuceneDocumentDB(@NonNull Resource storeLocation) throws DocumentDBException {
      try {
         this.tableFolder = storeLocation;
         directory = FSDirectory.open(storeLocation.getChild("index").asFile().orElseThrow().toPath());
         Map<String, JsonEntry> metadata = Json.parseObject(storeLocation.getChild("metadata.json"));
         this.indices = metadata.get("index").asMap(FieldType.class);
         this.primaryKey = metadata.get("primaryKey").asString();
         this.writer = ResourceMonitor.monitor(getIndexWriter());
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
         this.writer = ResourceMonitor.monitor(getIndexWriter());
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
                                                          Calendar calendar = Calendar.getInstance();
                                                          calendar.set(Calendar.YEAR, rnd.nextInt(90) + 1900);
                                                          calendar.set(Calendar.MONTH, rnd.nextInt(12));
                                                          calendar.set(Calendar.DAY_OF_MONTH, rnd.nextInt(28));
                                                          Date date = calendar.getTime();
                                                          person.put("dob", date);
                                                          return person;
                                                       }).iterator()));
      }

      peopleDB.addIndex(new IndexedField("dob", FieldType.Date));
      System.out.println(peopleDB.numberOfDocuments());
      for (DBDocument search : peopleDB.search("dob:[1970-07-24 TO 1977-07-28]")) {
         Long date = search.getAs("dob", Long.class);
         System.out.println(new Date(date));
      }

   }

   @Override
   public void add(@NonNull DBDocument document) throws IOException {
      addAll(Collections.singleton(document));
   }

   @Override
   public void addAll(@NonNull Iterable<DBDocument> documents) throws IOException {
      for (DBDocument document : documents) {
         Document luceneDoc = toDocument(document);
         if (document.getAsString(primaryKey) == null) {
            document.put(primaryKey, Strings.randomHexString(16));
         }
         writer.object.updateDocument(new Term(primaryKey, document.getAsString(primaryKey)), luceneDoc);
      }
      writer.object.commit();
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
         for (DBDocument dbDocument : this) {
            writer.object.updateDocument(new Term(primaryKey, dbDocument.getAsString(primaryKey)),
                                         toDocument(dbDocument));
         }
      } catch (IOException ioe) {
         throw new DocumentDBException(ioe);
      }
   }

   @Override
   public void close() throws Exception {
      directory.close();
   }

   @Override
   public void commit() throws IOException {
      try (IndexWriter writer = getIndexWriter()) {
         writer.forceMerge(1);
      }
   }

   @Override
   public DBDocument get(@NonNull Object id) {
      Validation.notNullOrBlank(primaryKey, "Retrieving a document by id requires a primary key to be set.");
      try {
         TermQuery idQuery = new TermQuery(new Term(primaryKey, Converter.convert(id, String.class)));
         searcherManager.object.maybeRefreshBlocking();
         IndexSearcher searcher = searcherManager.object.acquire();
         ScoreDoc[] r = searcher.search(idQuery, 1).scoreDocs;
         if (r.length > 0) {
            return Json.parse(searcher.getIndexReader().document(r[0].doc).get(STORED_JSON_FIELD), DBDocument.class);
         }
      } catch (IOException | TypeConversionException e) {
         LogUtils.logWarning(log, e);
      }
      return null;
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
      try {
         searcherManager.object.maybeRefreshBlocking();
         return searcherManager.object.acquire().getIndexReader().numDocs();
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
   }

   @Override
   public boolean remove(@NonNull DBDocument document) {
      return false;
   }

   @Override
   public List<DBDocument> search(String field, int numHits, float... vector) throws IOException {
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
   public List<DBDocument> search(String query, int numHits) throws IOException {
      List<DBDocument> docs = new ArrayList<>();

      try {
         QueryParser parser = new CustomQueryParser(primaryKey, createAnalyzer());
         parser.setAllowLeadingWildcard(true);
         Query q = parser.parse(query);
         searcherManager.object.maybeRefreshBlocking();
         IndexSearcher searcher = searcherManager.object.acquire();
         TopDocs results = searcher.search(q, numHits);
         for (ScoreDoc sd : results.scoreDocs) {
            Document document = searcher.getIndexReader().document(sd.doc);
            DBDocument out = Json.parse(document.get(STORED_JSON_FIELD), DBDocument.class);
            out.put(SCORE_FIELD, sd.score);
            docs.add(out);
         }
         return docs;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }


   private void addField(String name, JsonEntry value, Document luceneDocument) {
      switch (indices.getOrDefault(name, FieldType.None)) {
         case Timestamp:
            luceneDocument.add(new LongPoint(name,
                                             LocalDateTime.from(value.as(Date.class)
                                                                     .toInstant()
                                                                     .atZone(ZoneId.systemDefault()))
                                                          .toEpochSecond(ZoneOffset.UTC)));

         case Date:
            luceneDocument.add(new LongPoint(name,
                                             value.as(Date.class)
                                                  .toInstant()
                                                  .atZone(ZoneId.systemDefault())
                                                  .toLocalDate()
                                                  .toEpochDay()));
         case Embedding:
            luceneDocument.add(new EmbeddingField(name, value.asVal().as(float[].class)));
            break;
         case FullText:
            luceneDocument.add(new TextField(name, value.asString(), Field.Store.NO));
            break;
         case GeoSpatial:
            int lat;
            int lon;
            if (value.isArray()) {
               int[] a = value.asIntArray();
               lat = a[0];
               lon = a[1];
            } else if (value.isObject()) {
               lat = value.getIntProperty("lat", value.getIntProperty("latitude", -1));
               lon = value.getIntProperty("lon", value.getIntProperty("longitude", -1));
            } else {
               throw new RuntimeException("Indexing as GeoSpatial expects either an int[] or an object with keys lat/latitude and lon/longitude.");
            }
            luceneDocument.add(new LatLonPoint(name, lat, lon));
            break;
         case String:
            luceneDocument.add(new StringField(name, value.asString(), Field.Store.NO));
            break;
         case Double:
            if (value.isArray()) {
               value.elementIterator().forEachRemaining(e -> addField(name, e, luceneDocument));
            } else {
               luceneDocument.add(new DoublePoint(name, value.asNumber().doubleValue()));
            }
            break;
         case Long:
            if (value.isArray()) {
               value.elementIterator().forEachRemaining(e -> addField(name, e, luceneDocument));
            } else {
               luceneDocument.add(new LongPoint(name, value.asNumber().longValue()));
            }
            break;
         case Integer:
            if (value.isArray()) {
               value.elementIterator().forEachRemaining(e -> addField(name, e, luceneDocument));
            } else {
               luceneDocument.add(new IntPoint(name, value.asNumber().intValue()));
            }
            break;
         case Float:
            if (value.isArray()) {
               value.elementIterator().forEachRemaining(e -> addField(name, e, luceneDocument));
            } else {
               luceneDocument.add(new FloatPoint(name, value.asNumber().floatValue()));
            }
            break;
         case Boolean:
            luceneDocument.add(new StringField(name, Boolean.toString(value.asBoolean()), Field.Store.NO));
            break;
         case None:
            if (value.isObject()) {
               value.propertyIterator()
                    .forEachRemaining(e -> addField(name + "." + e.getKey(), e.getValue(), luceneDocument));
            }
      }
   }

   private Analyzer createAnalyzer() {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      this.indices.forEach((name, type) -> {
         if (type == FieldType.String || type == FieldType.Boolean) {
            analyzerMap.put(name, new KeywordAnalyzer());
         } else if (type == FieldType.Embedding) {
            analyzerMap.put(name, new LuceneEmbeddingAnalyzer());
         } else if (type == FieldType.Date || type == FieldType.Timestamp) {
            analyzerMap.put(name, new KeywordAnalyzer());
         }
      });
      return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);
   }


   private IndexWriter getIndexWriter() throws IOException {
      final IndexWriterConfig writerConfig = new IndexWriterConfig(new StandardAnalyzer());
      return new IndexWriter(directory, writerConfig);
   }

   protected Document toDocument(DBDocument d) {
      Document document = new Document();
      JsonEntry json = Json.asJsonEntry(d);
      document.add(new StoredField(STORED_JSON_FIELD, json.toString()));
      json.propertyIterator().forEachRemaining(e -> addField(e.getKey(), e.getValue(), document));
      return document;
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
            String rawJson = ld.get(STORED_JSON_FIELD);
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

   public class CustomQueryParser extends QueryParser {
      public CustomQueryParser(String f, Analyzer a) {
         super(f, a);
      }

      @Override
      public Query newRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) {
         FieldType fieldType = indices.get(field);
         if (fieldType != null) {
            switch (fieldType) {
               case Long:
                  return LongPoint.newRangeQuery(field,
                                                 Long.parseLong(part1),
                                                 Long.parseLong(part2));
               case Integer:
                  return IntPoint.newRangeQuery(field,
                                                Integer.parseInt(part1),
                                                Integer.parseInt(part2));
               case Double:
                  return DoublePoint.newRangeQuery(field,
                                                   Double.parseDouble(part1),
                                                   Double.parseDouble(part2));
               case Float:
                  return FloatPoint.newRangeQuery(field,
                                                  Float.parseFloat(part1),
                                                  Float.parseFloat(part2));
               case Date:
                  return LongPoint.newRangeQuery(field,
                                                 LocalDate.parse(part1, DATE_FORMAT).toEpochDay(),
                                                 LocalDate.parse(part2, DATE_FORMAT).toEpochDay());
               case Timestamp:
                  return LongPoint.newRangeQuery(field,
                                                 LocalDateTime.parse(part1, TIMESTAMP_FORMAT)
                                                              .toEpochSecond(ZoneOffset.UTC),
                                                 LocalDateTime.parse(part2, TIMESTAMP_FORMAT)
                                                              .toEpochSecond(ZoneOffset.UTC));
            }
         }
         return super.newRangeQuery(field, part1, part2, startInclusive, endInclusive);
      }


      @Override
      public Query newTermQuery(Term term) {
         FieldType fieldType = indices.get(term.field());
         if (fieldType != null) {
            switch (fieldType) {
               case Long:
                  return LongPoint.newExactQuery(field,
                                                 Long.parseLong(term.text()));
               case Integer:
                  return IntPoint.newExactQuery(field,
                                                Integer.parseInt(term.text()));
               case Double:
                  return DoublePoint.newExactQuery(field,
                                                   Double.parseDouble(term.text()));
               case Float:
                  return FloatPoint.newExactQuery(field,
                                                  Float.parseFloat(term.text()));
               case Date:
                  Date date = Converter.convertSilently(term.text(), Date.class);
                  return LongPoint.newExactQuery(field, date.getTime());
            }
         }

         return super.newTermQuery(term);
      }
   }
}//END OF LuceneDocumentStore
