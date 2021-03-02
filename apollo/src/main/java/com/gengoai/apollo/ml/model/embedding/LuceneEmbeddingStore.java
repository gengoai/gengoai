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

package com.gengoai.apollo.ml.model.embedding;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.io.Resources;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class LuceneEmbeddingStore {
   static final String FIELD_ID = "WORD";
   static final String FIELD_VECTOR = "VECTOR";

   static public Counter<String> analyze(Analyzer analyzer, String s) {

      Counter<String> c = Counters.newCounter();
      try {
         TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(s));
         CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);


         tokenStream.reset();
         while (tokenStream.incrementToken()) {
            String token = cattr.toString();
            if (token.length() == 0) {
               continue;
            }
            c.increment(cattr.toString());
         }
         tokenStream.end();
         tokenStream.close();

      } catch (IOException e) {
         e.printStackTrace();
      }

      return c;
   }

   protected static String encode(NDArray n) {
      StringBuilder b = new StringBuilder();
      for (long i = 0; i < n.length(); i++) {
         if (i > 0) {
            b.append(" ");
         }
         b.append(n.get(i));
      }
      return b.toString();
   }

   public static void main(String[] args) throws Exception {
      Path indexdir = Paths.get("/Users/ik/w2v_lucene");
      if (!Files.exists(indexdir)) {
         Files.createDirectories(indexdir);
      }
      Directory d = FSDirectory.open(indexdir);


      var ndAnalyzer = new LuceneEmbeddingAnalyzer();
      Map<String, Analyzer> map = Map.of(
            FIELD_VECTOR, ndAnalyzer
      );
      var analyze = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), map);


      PreTrainedWordEmbedding glove = PreTrainedWordEmbedding
            .readWord2VecTextFormat(Resources.from("/Users/ik/glove.6B.50d.txt"));

      IndexWriterConfig conf = new IndexWriterConfig(analyze);
      IndexWriter writer = new IndexWriter(d, conf);
      for (String s : glove.vectorStore.getAlphabet()) {
         Document doc = new Document();
         doc.add(new StringField(FIELD_ID, s, Field.Store.YES));
         StringBuilder ndStr = new StringBuilder();
         NDArray v = glove.vectorStore.getVector(s).unitize();
         for (long i = 0; i < v.length(); i++) {
            if( ndStr.length() > 0 ){
               ndStr.append(' ');
            }
            ndStr.append(v.get(i));
         }
         doc.add(new TextField(FIELD_VECTOR, ndStr.toString(), Field.Store.YES));
         writer.addDocument(doc);
      }
      writer.close();

      DirectoryReader reader = DirectoryReader.open(d);
      IndexSearcher searcher = new IndexSearcher(reader);



      query(searcher, ndAnalyzer.toQuery(lookup(searcher, "italy", "rome"), FIELD_VECTOR));
      query(searcher, ndAnalyzer.toQuery(lookup(searcher, "canada", "toronto"), FIELD_VECTOR));


//      glove.query(VSQuery.termQuery(term).limit(10))
//           .sorted(Comparator.comparingDouble(NDArray::getWeight).reversed())
//           .forEach(n -> System.out.println(n.getLabel() + " : " + n.getWeight()));

   }

   protected static NDArray lookup(IndexSearcher searcher,String... terms) throws Exception{
      int count = 0;
      NumericNDArray v = null;
      for (String term : terms) {
         TopDocs topDocs = searcher.search(new TermQuery(new Term(FIELD_ID, term)), 1);
         for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            NumericNDArray temp = readVector(searcher.getIndexReader().document(scoreDoc.doc).get(FIELD_VECTOR));
            if (v == null) {
               v = temp;
            } else {
               v.addi(temp);
            }
            count++;
         }
      }

      return v.divi(count);
   }

   protected static void query(IndexSearcher searcher, Query query) throws Exception {
      TopScoreDocCollector results = TopScoreDocCollector.create(10, Integer.MAX_VALUE);
      searcher.search(query, results);
      int rank = 1;
      for (ScoreDoc sd : results.topDocs().scoreDocs) {
         Document document = searcher.getIndexReader().document(sd.doc);
         String word = document.get(FIELD_ID);
         System.out.println(String.format("%d. %s (%.3f)", rank, word, sd.score));
         rank++;
      }
      System.out.println("------------------");
   }

   protected static NumericNDArray readVector(String vec) {
      String[] p = vec.strip().split("\\s+");
      NumericNDArray n = nd.DFLOAT32.zeros(p.length);
      for (int i = 0; i < p.length; i++) {
         n.set(i, Double.parseDouble(p[i]));
      }
      return n;
   }

}

