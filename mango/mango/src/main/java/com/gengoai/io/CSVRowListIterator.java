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

package com.gengoai.io;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator over Rows in CSV file
 */
public class CSVRowListIterator implements Iterator<List<String>> {
   private final MonitoredObject<CSVReader> rdr;
   @Getter
   private final List<String> header;
   private List<String> nextRow = null;

   /**
    * Instantiates a new Csv row iterator.
    *
    * @param reader the reader
    */
   public CSVRowListIterator(@NonNull CSVReader reader) {
      this.rdr = ResourceMonitor.monitor(reader);
      this.header = reader.getHeader();
   }

   private boolean advance() {
      if(nextRow != null) {
         return true;
      }
      try {
         nextRow = rdr.object.nextRow();
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      return nextRow != null;
   }

   @Override
   public boolean hasNext() {
      return advance();
   }

   @Override
   public List<String> next() {
      if(!advance()) {
         throw new NoSuchElementException();
      }
      List<String> toReturn = nextRow;
      nextRow = null;
      return toReturn;
   }
}//END OF CSVRowIterator
