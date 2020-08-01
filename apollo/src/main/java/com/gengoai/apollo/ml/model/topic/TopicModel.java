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

package com.gengoai.apollo.ml.model.topic;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.MultiInputModel;
import com.gengoai.collection.Iterators;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * Topic modelling in an unsupervised machine learning technique that discovers abstract "topics" as groups of words.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class TopicModel implements MultiInputModel<TopicModelFitParameters, TopicModel>, Iterable<Topic> {
   private static final long serialVersionUID = 1L;
   protected final List<Topic> topics = new ArrayList<>();

   @Override
   public abstract TopicModelFitParameters getFitParameters();

   /**
    * Gets the number of topics.
    *
    * @return the number of topics
    */
   public final int getNumberOfTopics() {
      return topics.size();
   }

   @Override
   public LabelType getOutputType() {
      return LabelType.NDArray;
   }

   /**
    * Gets the topic by its id.
    *
    * @param id the id of the topic to retrieve
    * @return the topic
    * @throws IndexOutOfBoundsException if the id is invalid
    */
   public Topic getTopic(int id) {
      return topics.get(id);
   }

   /**
    * Gets the topic by its name.
    *
    * @param name the name of the topic
    * @return the topic
    * @throws IndexOutOfBoundsException if the name is invalid
    */
   public Topic getTopic(@NonNull String name) {
      return topics.stream()
                   .filter(t -> name.equals(t.getName()))
                   .findFirst()
                   .orElseThrow(IndexOutOfBoundsException::new);
   }

   /**
    * Gets the distribution across topics for a given feature.
    *
    * @param feature the feature (word) whose topic distribution is desired
    * @return the distribution across topics for the given feature
    */
   public abstract NDArray getTopicDistribution(String feature);

   @Override
   public Iterator<Topic> iterator() {
      return Iterators.unmodifiableIterator(topics.iterator());
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      DataSet data = dataset.map(this::transform);
      for(String output : getOutputs()) {
         data.updateMetadata(output, m -> {
            m.setDimension(getNumberOfTopics());
            m.setEncoder(null);
            m.setType(NDArray.class);
         });
      }
      return data;
   }

}//END OF TopicModel
