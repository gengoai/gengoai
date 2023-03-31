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

package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.python.PythonInterpreter;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

public abstract class HuggingFacePipeline<INPUT, OUTPUT> {

    protected final PythonInterpreter interpreter;


    public HuggingFacePipeline(String pythonCode) {
        this.interpreter = new PythonInterpreter(pythonCode);
    }

    public abstract OUTPUT predict(@NonNull INPUT input);


    public List<OUTPUT> predict(@NonNull List<INPUT> inputList) {
        return inputList.stream().map(this::predict).toList();
    }

}//END OF HuggingFacePipeline
