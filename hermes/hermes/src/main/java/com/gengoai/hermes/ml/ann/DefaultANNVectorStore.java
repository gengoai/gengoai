package com.gengoai.hermes.ml.ann;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.embedding.VSQueryResult;
import com.gengoai.apollo.model.embedding.VectorStore;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.lucene.FrequencyType;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.LuceneIndex;
import com.gengoai.lucene.SearchResults;
import com.gengoai.lucene.field.Fields;
import com.gengoai.lucene.field.types.Vector;
import lombok.NonNull;
import org.apache.lucene.search.KnnFloatVectorQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class DefaultANNVectorStore implements VectorStore, AutoCloseable {
   private static final String ID_FIELD = "id";
   private static final String VECTOR_FIELD = "vector";
   private final LuceneIndex annIndex;
   private final int dimension;

   public DefaultANNVectorStore(@NonNull Resource indexLocation) throws IOException {
      annIndex = LuceneIndex.at(indexLocation)
                            .storedField(ID_FIELD, Fields.KEYWORD)
                            .storedField(VECTOR_FIELD, Fields.VECTOR)
                            .createIfNotExists();
      if (indexLocation.getChild("metadata.json").exists()) {
         JsonEntry entry = Json.parse(indexLocation.getChild("metadata.json").readToString());
         this.dimension = entry.getIntProperty("dimension");
      } else {
         throw new IOException("No Metdata.json Found");
      }
   }

   public DefaultANNVectorStore(@NonNull Resource indexLocation, int dimension) throws IOException {
      annIndex = LuceneIndex.at(indexLocation)
                            .storedField(ID_FIELD, Fields.KEYWORD)
                            .storedField(VECTOR_FIELD, Fields.VECTOR)
                            .createIfNotExists();
      this.dimension = dimension;
      indexLocation.getChild("metadata.json").write(Json.dumps(Map.of("dimension", dimension)));
   }

   @Override
   public void close() throws Exception {
      annIndex.close();
   }

   public void commit() {
      try {
         annIndex.commit();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public NumericNDArray getVector(String id) {
      try {
         if( containsKey(id) ) {
            return nd.DFLOAT32.array(annIndex.get(ID_FIELD, id).get(VECTOR_FIELD).as(Vector.class).getData());
         }
         return nd.DFLOAT32.zeros(dimension());
      } catch (IOException e) {
         return nd.DFLOAT32.scalar(0);
      }
   }

   @Override
   public boolean putVector(String id, NumericNDArray vector) {
      try {
         annIndex.addOrReplace(ID_FIELD,
                               IndexDocument.from(Map.of(
                                     ID_FIELD, id,
                                     VECTOR_FIELD, new Vector(vector.toFloatArray()))));
         return true;
      } catch (IOException e) {
         return false;
      }
   }

   @Override
   public boolean putAllVectors(@NonNull Map<String, NumericNDArray> vectors) {
      List<IndexDocument> bulk = new ArrayList<>();

      for (Map.Entry<String, NumericNDArray> entry : vectors.entrySet()) {
         if (containsKey(entry.getKey())) {
            if (!putVector(entry.getKey(), entry.getValue())) {
               return false;
            }
         } else {
            bulk.add(IndexDocument.from(Map.of(ID_FIELD, entry.getKey(),
                                               VECTOR_FIELD, new Vector(entry.getValue().toFloatArray())
                                              )));
         }
      }
      try {
         if (!bulk.isEmpty()) {
            annIndex.add(bulk);
         }
         return true;
      } catch (IOException e) {
         return false;
      }
   }

   @Override
   public List<VSQueryResult> query(NumericNDArray vector, int K) {
      try {
         SearchResults searchResults = annIndex.search(new KnnFloatVectorQuery(VECTOR_FIELD, vector.toFloatArray(), K), K);
         List<VSQueryResult> results = new ArrayList<>();
         searchResults.forEach(sd -> {
            VSQueryResult annResult = new VSQueryResult();
            annResult.setId(sd.getDocument().get(ID_FIELD).asString());
            annResult.setScore((float) sd.getScore());
            results.add(annResult);
         });
         return results;
      } catch (IOException e) {
         return List.of();
      }
   }

   @Override
   public Set<String> keySet() {
      try {
         return annIndex.terms(ID_FIELD, FrequencyType.TOTAL_TERM_FREQUENCY).items();
      } catch (IOException e) {
         return Set.of();
      }
   }

   @Override
   public boolean containsKey(String id) {
      try {
         return annIndex.contains(ID_FIELD, id);
      } catch (IOException e) {
         return false;
      }
   }

   @Override
   public int dimension() {
      return dimension;
   }

   @Override
   public Stream<NumericNDArray> stream() {
      return annIndex.stream().map(d -> nd.DFLOAT32.array(d.get(VECTOR_FIELD).as(Vector.class).getData()));
   }

   @Override
   public int size() {
      try {
         return annIndex.size();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

}// END OF DefaultANNVectorStore
