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
import com.gengoai.apollo.ml.observation.VariableCollection;
import com.gengoai.apollo.ml.observation.VariableList;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.StreamingContext;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A {@link DataSetReader} to read files in LibSVM format. The first column is the label (class) and the remaining
 * columns represent the features. The generated {@link Datum} have the label assigned to {@link Datum#DEFAULT_OUTPUT}
 * as a binary {@link Variable}. The features are combined into a {@link VariableCollection} assigned to {@link
 * Datum#DEFAULT_INPUT} with each feature having a name assigned based on it's column number (e.g. 1, 2, 3, etc.).
 * </p>
 *
 * @author David B. Bracewell
 */
public class LibSVMDataSetReader implements DataSetReader {
   private static final long serialVersionUID = 1L;
   private final boolean distributed;
   private final boolean multiClass;

   /**
    * Instantiates a new LibSVMDataSetReader creating a local DataSet.
    *
    * @param multiClass True - handle multiclass labels, False handle binary labels
    */
   public LibSVMDataSetReader(boolean multiClass) {
      this(false, multiClass);
   }

   /**
    * Instantiates a new LibSVMDataSetReader.
    *
    * @param distributed True - create a distributed DataSet, False create a local DataSet
    * @param multiClass  True - handle multiclass labels, False handle binary labels
    */
   public LibSVMDataSetReader(boolean distributed, boolean multiClass) {
      this.distributed = distributed;
      this.multiClass = multiClass;
   }

   private Datum processLine(String line) {
      String[] parts = line.split("\\s+");
      List<Variable> featureList = new ArrayList<>();
      String target;
      if(multiClass) {
         target = parts[0];
      } else {
         switch(parts[0]) {
            case "+1":
            case "1":
               target = "true";
               break;
            case "-1":
               target = "false";
               break;
            default:
               target = parts[0];
               break;
         }
      }
      for(int j = 1; j < parts.length; j++) {
         String[] data = parts[j].split(":");
         int fnum = Integer.parseInt(data[0]);
         double val = Double.parseDouble(data[1]);
         featureList.add(Variable.real(Integer.toString(fnum), val));
      }
      Datum datum = new Datum();
      datum.setDefaultOutput(Variable.binary(target));
      datum.setDefaultInput(new VariableList(featureList));
      return datum;
   }

   @Override
   public DataSet read(@NonNull Resource dataResource) throws IOException {
      StreamingDataSet dataSet = new StreamingDataSet(StreamingContext.get(distributed)
                                                                      .textFile(dataResource)
                                                                      .filter(Strings::isNotNullOrBlank)
                                                                      .map(this::processLine));
      dataSet.updateMetadata(Datum.DEFAULT_INPUT, m -> m.setType(VariableCollection.class));
      dataSet.updateMetadata(Datum.DEFAULT_OUTPUT, m -> m.setType(Variable.class));
      return dataSet;
   }
}//END OF LibSVMDataSetReader
