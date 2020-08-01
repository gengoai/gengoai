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

package com.gengoai.apollo.ml.data;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.StreamingDataSet;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.io.CSV;
import com.gengoai.io.resource.Resource;
import com.gengoai.math.Math2;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>
 * A {@link DataSetReader} for delimiter separated files. Rows in the file represents {@link Datum} and columns {@link
 * com.gengoai.apollo.ml.observation.Observation}s. Generated {@link Datum} have an observation per column in the file
 * with the source names equalling the column names.
 * </p>
 *
 * @author David B. Bracewell
 */
public class CSVDataSetReader implements DataSetReader, Serializable {
   private static final long serialVersionUID = 1L;
   private final CSV csv;
   private final Schema schema;

   /**
    * Instantiates a new CSVDataSetReader that will infer the types of each cell
    *
    * @param csv the definition of the csv file
    */
   public CSVDataSetReader(@NonNull CSV csv) {
      this.csv = csv;
      this.schema = null;
   }

   /**
    * Instantiates a new CSVDataSetReader.
    *
    * @param csv    the definition of the csv file
    * @param schema the schema
    */
   public CSVDataSetReader(@NonNull CSV csv,
                           @NonNull Schema schema) {
      this.csv = csv;
      this.schema = schema;
      if(!csv.getHasHeader() && csv.getHeader().isEmpty()) {
         throw new IllegalArgumentException("Either the CSV must have a header or one must be defined.");
      }
   }

   private Variable guess(String column, String value) {
      Double d = Math2.tryParseDouble(value);
      if(d == null) {
         return Variable.binary(column, value);
      }
      return Variable.real(column, d);
   }

   @Override
   public DataSet read(@NonNull Resource dataResource) throws IOException {
      DataSet dataSet = new StreamingDataSet(StreamingContext.local()
                                                             .stream(csv.rowMapStream(dataResource)
                                                                        .map(m -> {
                                                                           Datum datum = new Datum();
                                                                           for(String column : m.keySet()) {
                                                                              if(schema != null) {
                                                                                 datum.put(column,
                                                                                           schema.convert(column,
                                                                                                          m.get(column)));
                                                                              } else {
                                                                                 datum.put(column,
                                                                                           guess(column,
                                                                                                 m.get(column)));
                                                                              }
                                                                           }
                                                                           return datum;
                                                                        })));
      //
      //      List<Datum> data = new ArrayList<>();
      //      List<String> header;
      //      try(CSVReader reader = csv.reader(dataResource)) {
      //         header = new ArrayList<>(reader.getHeader());
      //         List<String> row;
      //         while((row = reader.nextRow()) != null) {
      //            if(row.isEmpty()) {
      //               continue;
      //            }
      //            Datum datum = new Datum();
      //            if(header.size() < row.size()) {
      //               for(int i = header.size(); i < row.size(); i++) {
      //                  header.add("AutoColumn-" + i);
      //               }
      //            }
      //            for(int i = 0; i < row.size(); i++) {
      //               String column = header.get(i);
      //               if(schema != null) {
      //                  datum.put(column, schema.convert(column, row.get(i)));
      //               } else {
      //                  datum.put(column, guess(column, row.get(i)));
      //               }
      //            }
      //            data.add(datum);
      //         }
      //      }
      //      DataSet dataSet = new InMemoryDataSet(data);
      //      for(String column : header) {
      //         dataSet.updateMetadata(column, m -> m.setType(Variable.class));
      //      }
      return dataSet;
   }

}//END OF CSVDataSetReader
