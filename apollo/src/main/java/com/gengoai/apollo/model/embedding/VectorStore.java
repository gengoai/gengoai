package com.gengoai.apollo.model.embedding;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface VectorStore extends Serializable {

   /**
    * The dimension of the vectors in the store
    *
    * @return the dimension of the vectors in the store
    */
   int dimension();

   /**
    * Gets the vector associated with the given id.
    *
    * @param id The id of the vector to retrieve
    * @return The vector associated with the id.
    */
   NumericNDArray getVector(String id);

   /**
    * Gets a stream over the vectors in the store.
    *
    * @return the stream of vectors.
    */
   Stream<NumericNDArray> stream();

   /**
    * Puts the given vector into the vector store
    *
    * @param id     The id of the vector
    * @param vector The vector
    * @return True - if the vector is successfully added, False otherwise.
    */
   boolean putVector(String id, NumericNDArray vector);

   /**
    * Puts the given vector into the vector store
    *
    * @param vectors Map of id to vector
    * @return True - if all vectors are successfully added, False otherwise.
    */
   boolean putAllVectors(Map<String, NumericNDArray> vectors);

   /**
    * Gets the total number of vectors in the vector store.
    *
    * @return the total number of vectors in the vector store.
    */
   int size();

   /**
    * Gets the keys (ids) of the vectors in the vector store.
    *
    * @return the keys (ids) of the vectors in the vector store.
    */
   Set<String> keySet();

   /**
    * Checks if the given id is a key in the vector store
    *
    * @param id the id of the vector
    * @return True if the id is a key in the vector store, False otherwise
    */
   boolean containsKey(String id);

   /**
    * Queries the vector store with the given vector finding the K nearest ids in the vector store.
    *
    * @param vector The query vector
    * @param K      the number of results desired
    * @return The up to K results most similar (Cosine Similarity) to the given vector.
    */
   List<VSQueryResult> query(NumericNDArray vector, int K);


   default void writeWord2VecFormat(@NonNull Resource output) throws IOException {
      try (Writer writer = output.writer()) {
         writer.write(Integer.toString(size()));
         writer.write(" ");
         writer.write(Integer.toString(dimension()));
         writer.write("\n");
         for (String word : keySet()) {
            writer.write(word.replace(' ', '_'));
            writer.write(" ");
            writer.write(VSTextUtils.vectorToLine(getVector(word)));
            writer.write("\n");
         }
      }
   }

}//END OF VectorStore
