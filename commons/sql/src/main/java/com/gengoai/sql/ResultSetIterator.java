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

package com.gengoai.sql;

import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>Wraps a ResultSet as an Iterator mapping the Result to an object with a given {@link ResultSetMapper}. This class
 * takes care of closing the result when the underlying ResultSet is no longer in memory. Note that all SQLExceptions
 * are rethrown as RuntimeException to fit within the Iterator paradigm.</p>
 *
 * @param <T> the type of Object we will map the ResultSet to
 */
public class ResultSetIterator<T> implements Iterator<T>, Serializable {
   private static final long serialVersionUID = 1L;
   private final ResultSetMapper<? extends T> mapper;
   private final MonitoredObject<ResultSet> resultSet;
   private boolean hasNext;

   /**
    * Instantiates a new ResultSetIterator.
    *
    * @param mapper    the mapper taking a ResultSet and returning an Object
    * @param resultSet the ResultSet to iterate over
    */
   @SneakyThrows
   public ResultSetIterator(@NonNull ResultSetMapper<? extends T> mapper,
                            @NonNull ResultSet resultSet) {
      this.mapper = mapper;
      this.resultSet = ResourceMonitor.monitor(resultSet);
      hasNext = resultSet.next();
   }

   @Override
   public boolean hasNext() {
      return hasNext;
   }

   @Override
   @SneakyThrows
   public T next() {
      if (!hasNext) {
         throw new NoSuchElementException();
      }
      T out = mapper.map(resultSet.object);
      hasNext = resultSet.object.next();
      return out;
   }

}//END OF ResultSetIterator
