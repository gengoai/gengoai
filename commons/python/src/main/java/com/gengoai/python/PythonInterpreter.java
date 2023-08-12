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

package com.gengoai.python;

import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import jep.Jep;
import jep.SharedInterpreter;
import lombok.NonNull;

import java.util.Map;

public final class PythonInterpreter implements AutoCloseable {

    private final MonitoredObject<SharedInterpreter> interpreter;


    public PythonInterpreter() {
        this.interpreter = ResourceMonitor.monitor(new SharedInterpreter());
    }

    public PythonInterpreter(@NonNull String initCode) {
        this.interpreter = ResourceMonitor.monitor(new SharedInterpreter(), Jep::close);
        this.interpreter.object.exec(initCode);
    }

    public void exec(String pythonCode) {
        this.interpreter.object.exec(pythonCode);
    }

    public boolean eval(String pythonCode) {
        return this.interpreter.object.eval(pythonCode);
    }

    public void runScript(String script) {
        this.interpreter.object.runScript(script);
    }

    public Object invoke(String function, Object... objects) {
        return this.interpreter.object.invoke(function, objects);
    }

    public Object invoke(String function, Map<String, Object> map) {
        return this.interpreter.object.invoke(function, map);
    }

    public Object invoke(String function, Object[] objects, Map<String, Object> map) {
        return this.interpreter.object.invoke(function, objects, map);
    }

    public Object getValue(String variable) {
        return this.interpreter.object.getValue(variable);
    }

    public <T> T getValue(String variable, Class<T> tClass) {
        return this.interpreter.object.getValue(variable, tClass);
    }

    public void setValue(String variable, Object value) {
        this.interpreter.object.set(variable, value);
    }

    @Override
    public void close() throws Exception {
        this.interpreter.object.close();
    }

}//END OF PythonInterpreter
