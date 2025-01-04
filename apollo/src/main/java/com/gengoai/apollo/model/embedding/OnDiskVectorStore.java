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

package com.gengoai.apollo.model.embedding;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.collection.Iterators;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.kv.KeyValueStore;
import com.gengoai.kv.MapDBKeyValueStore;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class OnDiskVectorStore extends MLVectorStore {
   private final KeyValueStore<String, Long> offsetMap;
   private final KeyValueStore<String, String> metadata;
   private final RandomAccessFile vectors;
   private final int dimension;
   private final boolean writeable;
   private final File vectorFile;

   public OnDiskVectorStore(int dimension,
                            String unknownWord,
                            String[] specialKeys,
                            @NonNull Resource storeLocation) {
      storeLocation.mkdirs();
      this.offsetMap = new MapDBKeyValueStore<>(storeLocation.getChild("offsets.bin"), "offsets", true, false);
      this.metadata = new MapDBKeyValueStore<>(storeLocation.getChild("offsets.bin"), "metadata", true, false);
      this.metadata.put("dimension", Integer.toString(dimension));
      this.metadata.put("unknownWord", unknownWord);
      this.metadata.put("specialKeys", Strings.join(specialKeys, "\t"));
      this.dimension = dimension;
      this.vectorFile = new File(storeLocation.getChild("vectors.bin").path());
      try {
         this.vectors = new RandomAccessFile(vectorFile, "rw");
      } catch (FileNotFoundException e) {
         throw new RuntimeException(e);
      }
      this.writeable = true;
   }

   public OnDiskVectorStore(@NonNull Resource storeLocation) {
      this.offsetMap = new MapDBKeyValueStore<>(storeLocation.getChild("offsets.bin"), "offsets", true, true);
      this.metadata = new MapDBKeyValueStore<>(storeLocation.getChild("offsets.bin"), "metadata", true, true);
      this.dimension = Integer.parseInt(metadata.get("dimension"));
      this.vectorFile = new File(storeLocation.getChild("vectors.bin").path());
      try {
         this.vectors = new RandomAccessFile(vectorFile, "r");
      } catch (FileNotFoundException e) {
         throw new RuntimeException(e);
      }
      this.writeable = false;
   }

   @Override
   public boolean containsKey(String id) {
      return offsetMap.containsKey(id);
   }

   @Override
   public int dimension() {
      return dimension;
   }

   @Override
   public String[] getSpecialKeys() {
      return metadata.get("specialKeys").split("\t");
   }

   @Override
   public String getUnknownKey() {
      return metadata.get("unknownWord");
   }

   @Override
   protected NumericNDArray getVectorImpl(String key) {
      Long offset = offsetMap.get(key);
      try {
         vectors.seek(offset);
         byte[] bytes = new byte[Float.BYTES * dimension];
         vectors.read(bytes);
         float[] farray = toFloatArray(bytes);
         NumericNDArray array = nd.DFLOAT32.array(farray);
         array.setLabel(key);
         return array;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Stream<NumericNDArray> stream() {
      return Streams.asStream(Iterators.transform(offsetMap.keySet().iterator(),
                                                  this::getVector));
   }

   @Override
   public boolean putVector(String word, @NonNull NumericNDArray vector) {
      if (writeable) {
         long offset = offsetMap.getOrDefault(word, vectorFile.length());
         offsetMap.put(word, offset);
         try {
            vectors.seek(offset);
            vectors.write(toByteArray(vector.toFloatArray()));
         } catch (IOException ioe) {
            throw new RuntimeException(ioe);
         }
         return true;
      } else {
         throw new RuntimeException("Not Supported");
      }
   }

   @Override
   public Set<String> keySet() {
      return offsetMap.keySet();
   }

   @Override
   public int size() {
      return offsetMap.size();
   }

   public void commit() {
      offsetMap.commit();
      metadata.commit();
   }

   final static int BYTES_IN_FLOAT = Float.SIZE / Byte.SIZE;

   public static byte[] toByteArray(float[] floatArray) {
      ByteBuffer buffer = ByteBuffer.allocate(floatArray.length * BYTES_IN_FLOAT);
      buffer.asFloatBuffer().put(floatArray);
      return buffer.array();
   }


   public static float[] toFloatArray(byte[] byteArray) {
      float[] result = new float[byteArray.length / BYTES_IN_FLOAT];
      ByteBuffer.wrap(byteArray).asFloatBuffer().get(result, 0, result.length);
      return result;
   }


   private static void createVectorStore(Resource vectorsFile,
                                         int dimension,
                                         String unknownToken,
                                         String[] specialTokens,
                                         Resource vectorStoreLocation) throws Exception {
      OnDiskVectorStore vectorStore = new OnDiskVectorStore(dimension,
                                                            unknownToken,
                                                            specialTokens,
                                                            vectorStoreLocation);


      for (String specialKey : specialTokens) {
         vectorStore.putVector(specialKey, nd.DFLOAT32.zeros(dimension));
      }
      if (Strings.isNotNullOrBlank(unknownToken)) {
         vectorStore.putVector(unknownToken, nd.DFLOAT32.zeros(dimension));
      }
      AtomicInteger ai = new AtomicInteger();
      NumericNDArray avg = nd.DFLOAT32.zeros(dimension);
      vectorsFile.lines().skip(1).forEach(line -> {
         try {
            if (Strings.isNotNullOrBlank(line) && !line.startsWith("#")) {
               NumericNDArray v = VSTextUtils.convertLineToVector(line, dimension);
               vectorStore.putVector(v.getLabel(), v);
               avg.addi(v);
            }
            System.out.println(ai.incrementAndGet());
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      avg.divi(ai.get());
      if (Strings.isNotNullOrBlank(unknownToken)) {
         vectorStore.putVector(unknownToken, avg);
      }
      vectorStore.commit();
   }

   public static void main(String[] args) throws Exception {
      final String GLOVE_LARGE = "glove.840B.300d";
      final String GLOVE_SMALL_50 = "glove.6B.50d";
      final String GLOVE_SMALL_100 = "glove.6B.100d";
      final String GLOVE_SMALL_200 = "glove.6B.200d";
      final String GLOVE_SMALL_300 = "glove.6B.300d";

      final Resource path = Resources.from("/home/ik/Downloads/").getChild(GLOVE_LARGE + ".txt");
      final Resource vectorFile = Resources.from("/home/ik/").getChild(GLOVE_LARGE);
      createVectorStore(path,
                        300,
                        "--UNKNOWN--",
                        new String[]{"--PAD--"},
                        vectorFile);
      OnDiskVectorStore vectorStore = new OnDiskVectorStore(vectorFile);
      System.out.println(vectorStore.getVector("--UNKNOWN--"));
   }
}
