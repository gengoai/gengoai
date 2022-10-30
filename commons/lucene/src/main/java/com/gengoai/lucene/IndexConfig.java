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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.LogUtils;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.lucene.field.BeanField;
import com.gengoai.lucene.field.Field;
import com.gengoai.lucene.field.Fields;
import com.gengoai.lucene.field.TeeField;
import com.gengoai.lucene.field.types.BeanFieldType;
import com.gengoai.lucene.field.types.FieldType;
import com.gengoai.string.Strings;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static com.gengoai.lucene.LuceneIndex.INDEX_INFORMATION_JSON;
import static com.gengoai.lucene.LuceneIndex.INDEX_SUBDIRECTORY;


/**
 * <p>Encapsulate the configuration options for a {@link LuceneIndex}. While this does not expose all possible
 * options available for creating an index, it should provide the most widely used. These include options pertaining to
 * memory usage, committing, flushing, and similarity measure. In addition, it defines the schema for the index.</p>
 *
 * <p>A index's schema defines how document fields are indexed. By default only fields whose are defined in the schema
 * will be processed and indexed. However, the option exists to define fields of a document manually using a Lucene
 * IndexableField (care must be taken not to mix manual definition with schema-defined fields. The schema defines a
 * mapping of field names to {@link Field} where the processor defines how to handle a field. The processor also takes
 * care of converting objects into the right format, defining the required Analyzer for query parsing, and how to
 * generate range and term queries. </p>
 *
 * <p>Every document added to an index should have a primary key. By default the field for the primary key is defined
 * as <code>@id</code>. If the config is set to <code>autoIncrementPrimaryKey</code> then an auto incrementing value
 * will be used to define the primary key.</p>
 *
 * <p>If the original document is required, the <code>serializedDocumentField</code> option can be set to define a
 * stored field on the index that holds a json representation of the document. This is useful when you do not want to
 * index all fields on the document, but want access to the original for processing purposes.</p>
 *
 * <p>The {@link IndexConfigBuilder} is used to create {@link IndexConfig} and provides methods for opening and
 * creating {@link LuceneIndex}.</p>
 *
 * @author David B. Bracewell
 */

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Log
public class IndexConfig implements Serializable {
   private static final long serialVersionUID = 1L;
   @JsonProperty
   private boolean commitOnClose = true;
   @JsonProperty
   private boolean checkPendingFlushUpdate = false;
   @JsonProperty
   private double ramBufferSizeMB = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
   @JsonProperty
   private int maxBufferedDocs = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DOCS;
   @JsonProperty
   private boolean useCompoundFile = IndexWriterConfig.DEFAULT_USE_COMPOUND_FILE_SYSTEM;
   @JsonProperty
   private @NonNull LuceneSimilarity similarity = LuceneSimilarity.TF_IDF;
   @JsonProperty("docField")
   @Getter
   private String serializedDocumentField;
   @JsonProperty
   private Set<Field> fields = new HashSet<>();
   @JsonProperty
   private int ramPerThreadHardLimitMB;

   /**
    * Gets a per-field analyzer based on the schema defined in the config.
    *
    * @return the analyzer
    */
   @JsonIgnore
   public Analyzer getAnalyzer() {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      fields.forEach(field -> analyzerMap.putAll(field.getAnalyzers()));
      return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);
   }

   /**
    * Gets the processor for the given field.
    *
    * @param field the name of the field of the field
    * @return the field processor or null if one is not defined
    */
   @JsonIgnore
   public Optional<Field> getField(@NonNull String field) {
      return fields.stream()
                   .filter(f -> f.getName().equals(field))
                   .findFirst();
   }

   public Set<Field> getFields() {
      return Collections.unmodifiableSet(fields);
   }


   private void setFields(Set<Field> fields) {
      this.fields = new HashSet<>(fields);
   }

   /**
    * Constructs a Lucene IndexWriterConfig based on the settings in this config object.
    *
    * @return the IndexWriterConfig
    */
   public IndexWriterConfig indexWriterConfig() {
      IndexWriterConfig config;
      config = new IndexWriterConfig(getAnalyzer());
      config.setCheckPendingFlushUpdate(checkPendingFlushUpdate);
      config.setMaxBufferedDocs(maxBufferedDocs);
      config.setRAMBufferSizeMB(ramBufferSizeMB);
      config.setCommitOnClose(commitOnClose);
      config.setUseCompoundFile(useCompoundFile);
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
      config.setSimilarity(similarity.getSimilarityMeasure());
      config.setMergeScheduler(new ConcurrentMergeScheduler());
      config.setRAMPerThreadHardLimitMB(ramPerThreadHardLimitMB);
      return config;
   }

   /**
    * Determines if a field by the given name is defined in the schema or not.
    *
    * @param field the name of the field of the field
    * @return True - defined, False - not defined
    */
   public boolean isDefined(@NonNull String field) {
      return fields.contains(new Field(field, Fields.STRING));
   }

   /**
    * Converts a lucene Document into an {@link IndexDocument} taking care of auto-incrementing primary keys and loading
    * stored documents.
    *
    * @param luceneDocument the lucene document we want to load/convert
    * @return the IndexDocument
    */
   public IndexDocument load(@NonNull Document luceneDocument) {
      if (Strings.isNotNullOrBlank(serializedDocumentField)) {
         try {
            return Json.<ObjectDocument>parse(luceneDocument.get(serializedDocumentField), ObjectDocument.class);
         } catch (IOException e) {
            LogUtils.logSevere(log, e);
         }
      }
      return new FieldedDocument(luceneDocument);
   }


   /**
    * Creates a new QueryParser with the given default field that is aware of the schema used in the index.
    *
    * @param defaultField the default field
    * @return the query parser
    */
   public QueryParser queryParser(@NonNull String defaultField) {
      return new SchemaAwareQueryParser(defaultField, this);
   }


   /**
    * <p>A builder for creating {@link IndexConfig} and {@link LuceneIndex}. Most properties relate to fields on the
    * Lucene IndexWriterConfig class. Additional options include:
    * <ul>
    *    <li>readOnly: True - open an index as read only, False - open as writable.</li>
    *    <li>autoIncrementPrimaryKey: True - auto-incrementing primary key, False - user supplied primary key.</li>
    *    <li>serializedDocumentField: The field in which to store the jsonified version of the IndexDocument.</li>
    * </ul>
    * The schema is defined using the <code>field</code> methods, e.g. <code>stringField</code>, <code>floatField</code>,
    * etc. </p>
    *
    * <p>After all options and fields are defined a {@link LuceneIndex} can be obtained via:
    * <ul>
    *    <li>create: Create a new index throwing an error if it already exists.</li>
    *    <li>open: Opens an existing index throwing an error if it does not exist</li>
    *    <li>createIfNotExists: Create or open an index depending on whether or not it already exists. If the index
    *        already exists, the configuration for the existing index will be used.</li>
    *    <li>dropAndCreate: Deletes any existing index before creating an index according this config.</li>
    * </ul>
    * </p>
    */
   @Accessors(fluent = true)
   @Data
   public static class IndexConfigBuilder {
      private final List<Field> fields = new ArrayList<>();
      private Resource location;
      private boolean readOnly = false;
      private boolean checkPendingFlushUpdate = false;
      private boolean commitOnClose = true;
      private int maxBufferedDocs = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DOCS;
      private double ramBufferSizeMB = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
      private int ramPerThreadHardLimitMB = IndexWriterConfig.DEFAULT_RAM_PER_THREAD_HARD_LIMIT_MB;
      private @NonNull LuceneSimilarity similarity = LuceneSimilarity.TF_IDF;
      private boolean useCompoundFile = IndexWriterConfig.DEFAULT_USE_COMPOUND_FILE_SYSTEM;
      private String serializedDocumentField;

      IndexConfigBuilder(@NonNull Resource location) {
         this.location = location;
      }

      public IndexConfigBuilder beanField(@NonNull String name, @NonNull Class<?> beanClass) {
         return field(new BeanField(name, beanClass, false));
      }

      public IndexConfigBuilder beanField(@NonNull String name, @NonNull Class<?> beanClass, boolean serializeObject, boolean storeProperties) {
         return field(new BeanField(name, serializeObject, beanClass, storeProperties));
      }

      /**
       * Clears all defined fields from the config's schema
       *
       * @return this IndexConfigBuilder
       */
      public IndexConfigBuilder clearFields() {
         fields.clear();
         return this;
      }

      /**
       * Creates a new {@link LuceneIndex} throwing an error if the index already exists.
       *
       * @return the lucene index
       * @throws IOException Something went wrong creating the index
       */
      public LuceneIndex create() throws IOException {
         IndexConfig config = build();
         if (readOnly) {
            return new ReadOnlyIndex(location, config);
         }
         return new WritableIndex(location, config);
      }

      public IndexConfigBuilder createFieldsForBean(@NonNull Class<?> beanClass, boolean store) {
         BeanFieldType beanFieldType = new BeanFieldType(beanClass, store);
         beanFieldType.createFields().forEach(this::field);
         return this;
      }

      /**
       * Creates a new {@link LuceneIndex} if one does not already exist. If an index already exists, the index will be
       * opened using its already defined configuration, i.e. the configuration settings here will be ignored. This is
       * to ensure that redefined fields will not break the index.
       *
       * @return the lucene index
       * @throws IOException Something went wrong creating or opening the index
       */
      public LuceneIndex createIfNotExists() throws IOException {
         if (location.getChild(INDEX_INFORMATION_JSON).exists()) {
            return open();
         }
         return create();
      }

      /**
       * Drops (deletes) any existing index before creating the index.
       *
       * @return the lucene index
       * @throws IOException Something went wrong creating the index
       */
      public LuceneIndex dropAndCreate() throws IOException {
         if (location.getChild(INDEX_INFORMATION_JSON).exists()) {
            location.getChild(INDEX_INFORMATION_JSON).delete();
         }
         if (location.getChild(INDEX_SUBDIRECTORY).exists()) {
            location.getChild(INDEX_SUBDIRECTORY).delete(true);
         }
         return create();
      }

      /**
       * Defines the field with the given name as the given value type.
       *
       * @return this IndexConfigBuilder
       */
      public IndexConfigBuilder field(@NonNull Field field) {
         fields.remove(field);
         fields.add(field);
         return this;
      }

      public IndexConfigBuilder field(@NonNull String name, @NonNull FieldType fieldType) {
         return field(new Field(name,fieldType));
      }

      /**
       * Opens an existing index throwing an error if the index does not exist.
       *
       * @return the lucene index
       * @throws IOException Something went wrong opening the index.
       */
      public LuceneIndex open() throws IOException {
         if (readOnly) {
            return new ReadOnlyIndex(location);
         }
         return new WritableIndex(location);
      }

      /**
       * Removes the field with given name from the schema
       *
       * @param name the name of the field
       * @return this IndexConfigBuilder
       */
      public IndexConfigBuilder removeField(@NonNull String name) {
         fields.removeIf(f -> f.getName().equals(name));
         return this;
      }

      public IndexConfigBuilder storedField(@NonNull String name, @NonNull FieldType fieldType) {
         return field(new Field(name, fieldType, true));
      }

      public IndexConfigBuilder storedTeeField(@NonNull String name, @NonNull Field... teeFields) {
         return field(new TeeField(name, true, teeFields));
      }

      public IndexConfigBuilder teeField(@NonNull String name, @NonNull Field... teeFields) {
         return field(new TeeField(name, teeFields));
      }

      private IndexConfig build() {
         IndexConfig config = new IndexConfig();
         config.fields = new HashSet<>();
         config.checkPendingFlushUpdate = checkPendingFlushUpdate;
         config.commitOnClose = commitOnClose;
         config.maxBufferedDocs = maxBufferedDocs;
         config.ramBufferSizeMB = ramBufferSizeMB;
         config.similarity = similarity;
         config.useCompoundFile = useCompoundFile;
         config.serializedDocumentField = serializedDocumentField;
         config.ramPerThreadHardLimitMB = ramPerThreadHardLimitMB;
         config.fields.addAll(fields);
         return config;
      }
   }

}//END OF LuceneIndexConfig
