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
import com.gengoai.string.CharMatcher;
import com.gengoai.string.Strings;
import jep.Jep;
import jep.SharedInterpreter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>PythonInterpreter provides a wrapper around the Jep Python interpreter. It provides a mechanism for executing
 * Python code and invoking Python functions from Java.</p>
 */
public final class PythonInterpreter implements AutoCloseable {
    private static PythonInterpreter INSTANCE;

    public synchronized static PythonInterpreter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PythonInterpreter();
        }
        return INSTANCE;
    }

    private final MonitoredObject<SharedInterpreter> interpreter;
    private final Set<String> uniqueFuncNames = new HashSet<>();


    public void registerUniqueFuncName(String name) {
        uniqueFuncNames.add(name);
    }

    public boolean isRegisteredUniqueFuncName(String name) {
        return uniqueFuncNames.contains(name);
    }

    public String generateUniqueFuncName() {
        while (true) {
            String name = Strings.randomString(10, CharMatcher.anyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
            if (!uniqueFuncNames.contains(name)) {
                return name;
            }
        }
    }

    /**
     * Instantiates a new Python interpreter.
     */
    public PythonInterpreter() {
        this.interpreter = ResourceMonitor.monitor(new SharedInterpreter());
    }

    /**
     * Instantiates a new Python interpreter.
     *
     * @param initCode the init code
     */
    public PythonInterpreter(@NonNull String initCode) {
        this.interpreter = ResourceMonitor.monitor(new SharedInterpreter(), Jep::close);
        this.interpreter.object.exec(initCode);
    }

    /**
     * Executes the specified Python code.
     *
     * @param pythonCode the Python code to execute
     */
    public void exec(String pythonCode) {
        this.interpreter.object.exec(pythonCode);
    }

    /**
     * Evaluates the specified Python code.
     *
     * @param pythonCode the Python code to evaluate
     * @return the result of the evaluation
     */
    public boolean eval(String pythonCode) {
        return this.interpreter.object.eval(pythonCode);
    }

    /**
     * Runs the specified script.
     *
     * @param script the script to run
     */
    public void runScript(String script) {
        this.interpreter.object.runScript(script);
    }

    /**
     * Invokes the specified function with the given arguments.
     *
     * @param function the function to invoke
     * @param objects  the arguments to pass to the function
     * @return the result of the function invocation
     */
    public Object invoke(String function, Object... objects) {
        return this.interpreter.object.invoke(function, objects);
    }

    /**
     * Invokes the specified function with the given arguments.
     *
     * @param function the function to invoke
     * @param map      the arguments to pass to the function
     * @return the result of the function invocation
     */
    public Object invoke(String function, Map<String, Object> map) {
        return this.interpreter.object.invoke(function, map);
    }

    /**
     * Invokes the specified function with the given arguments.
     *
     * @param function the function to invoke
     * @param objects  the arguments to pass to the function
     * @param map      the keyword arguments to pass to the function
     * @return the result of the function invocation
     */
    public Object invoke(String function, Object[] objects, Map<String, Object> map) {
        return this.interpreter.object.invoke(function, objects, map);
    }

    /**
     * Gets the value of the specified variable.
     *
     * @param variable the variable to get the value of
     * @return the value of the variable
     */
    public Object getValue(String variable) {
        return this.interpreter.object.getValue(variable);
    }

    /**
     * Gets the value of the specified variable.
     *
     * @param variable the variable to get the value of
     * @param tClass   the class of the value
     * @param <T>      the type of the value
     * @return the value of the variable
     */
    public <T> T getValue(String variable, Class<T> tClass) {
        return this.interpreter.object.getValue(variable, tClass);
    }

    /**
     * Sets the value of the specified variable.
     *
     * @param variable the variable to set the value of
     * @param value    the value to set
     */
    public void setValue(String variable, Object value) {
        this.interpreter.object.set(variable, value);
    }

    @Override
    public void close() throws Exception {
        this.interpreter.object.close();
    }

}//END OF PythonInterpreter
