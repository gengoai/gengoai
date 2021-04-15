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

import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.documentdb.DBDocument;
import com.gengoai.documentdb.DocumentDBException;
import com.gengoai.documentdb.FieldType;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
final class LuceneDocumentUtils {
   public static final String SCORE_FIELD = "___SCORE___";
   public static final String STORED_JSON_FIELD = "___RAW___";

   private LuceneDocumentUtils() {
      throw new IllegalAccessError();
   }

   public static Analyzer createAnalyzer(Map<String, FieldType> indices) {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      indices.forEach((name, type) -> {
         switch (type) {
            case Embedding:
               analyzerMap.put(name, new LuceneEmbeddingAnalyzer());
               break;
            case FullText:
               analyzerMap.put(name, new StandardAnalyzer());
         }
      });
      return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzerMap);
   }

   public static IndexWriter getIndexWriter(Directory directory, Map<String,FieldType> indices) throws IOException {
      final IndexWriterConfig writerConfig = new IndexWriterConfig(createAnalyzer(indices));
      writerConfig.setUseCompoundFile(true);
      return new IndexWriter(directory, writerConfig);
   }

   public static Query parseQuery(String query, String primaryKey, Map<String, FieldType> indices) throws DocumentDBException {
      QueryParser parser = new LuceneDocumentDBQueryParser(primaryKey, indices);
      parser.setAllowLeadingWildcard(true);
      try {
         return parser.parse(query);
      } catch (ParseException e) {
         throw new DocumentDBException(e);
      }
   }

   public static List<DBDocument> search(Query query, int numHits, IndexSearcher searcher) throws IOException {
      List<DBDocument> docs = new ArrayList<>();
      TopDocs results = searcher.search(query, numHits);
      for (ScoreDoc sd : results.scoreDocs) {
         Document document = searcher.getIndexReader().document(sd.doc);
         DBDocument out = Json.parse(document.get(LuceneDocumentUtils.STORED_JSON_FIELD), DBDocument.class);
         out.put(LuceneDocumentUtils.SCORE_FIELD, sd.score);
         docs.add(out);
      }
      return docs;
   }

   public static Document toDocument(DBDocument d,
                                     Map<String, FieldType> indices) throws DocumentDBException {
      Document document = new Document();
      JsonEntry json = Json.asJsonEntry(d);
      document.add(new StoredField(STORED_JSON_FIELD, json.toString()));
      for (Iterator<Map.Entry<String, JsonEntry>> itr = json.propertyIterator(); itr.hasNext(); ) {
         Map.Entry<String, JsonEntry> e = itr.next();
         addField(e.getKey(), e.getValue(), document, indices);
      }
      return document;
   }

   public static <T> T withSearchManager(SearcherManager manager, SearchFunction<T> function) throws DocumentDBException {
      IndexSearcher searcher = null;
      try {
         manager.maybeRefreshBlocking();
         searcher = manager.acquire();
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
      try {
         return function.apply(searcher);
      } catch (IOException e) {
         throw new DocumentDBException(e);
      } finally {
         release(manager, searcher);
      }
   }

   private static void addField(String name,
                                JsonEntry value,
                                Document luceneDocument,
                                Map<String, FieldType> indices) throws DocumentDBException {
      try {
         switch (indices.getOrDefault(name, FieldType.None)) {
            case Timestamp:
               luceneDocument.add(new LongPoint(name, Converter.convert(value.get(), LocalDateTime.class)
                                                               .toEpochSecond(ZoneOffset.UTC)));
               break;
            case Date:
               luceneDocument
                     .add(new LongPoint(name, Converter.convert(value.get(), LocalDate.class).toEpochDay()));
               break;
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
                  for (JsonEntry e : Iterables.asIterable(value.elementIterator())) {
                     addField(name, e, luceneDocument, indices);
                  }
               } else {
                  luceneDocument.add(new DoublePoint(name, value.asNumber().doubleValue()));
               }
               break;
            case Long:
               if (value.isArray()) {
                  for (JsonEntry e : Iterables.asIterable(value.elementIterator())) {
                     addField(name, e, luceneDocument, indices);
                  }
               } else {
                  luceneDocument.add(new LongPoint(name, value.asNumber().longValue()));
               }
               break;
            case Integer:
               if (value.isArray()) {
                  for (JsonEntry e : Iterables.asIterable(value.elementIterator())) {
                     addField(name, e, luceneDocument, indices);
                  }
               } else {
                  luceneDocument.add(new IntPoint(name, value.asNumber().intValue()));
               }
               break;
            case Float:
               if (value.isArray()) {
                  for (JsonEntry e : Iterables.asIterable(value.elementIterator())) {
                     addField(name, e, luceneDocument, indices);
                  }
               } else {
                  luceneDocument.add(new FloatPoint(name, value.asNumber().floatValue()));
               }
               break;
            case Boolean:
               luceneDocument.add(new StringField(name, Boolean.toString(value.asBoolean()), Field.Store.NO));
               break;
            case None:
               if (value.isObject()) {
                  for (Map.Entry<String, JsonEntry> e : Iterables.asIterable(value.propertyIterator())) {
                     addField(name + "." + e.getKey(), e.getValue(), luceneDocument, indices);
                  }
               }
         }
      } catch (TypeConversionException e) {
         throw new DocumentDBException(e);
      }
   }

   private static void release(SearcherManager manager, IndexSearcher searcher) throws DocumentDBException {
      try {
         manager.release(searcher);
      } catch (IOException e) {
         throw new DocumentDBException(e);
      }
   }

}//END OF LuceneDocumentUtils
