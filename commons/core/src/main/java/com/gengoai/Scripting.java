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

package com.gengoai;

import com.gengoai.conversion.Cast;
import lombok.NonNull;

import javax.script.*;
import java.io.Reader;


public final class Scripting {
   private final ScriptEngine engine;

   public Scripting(@NonNull String engineName) {
      ScriptEngineManager factory = new ScriptEngineManager();
      this.engine = factory.getEngineByName(engineName);
   }


   public void put(String name, Object value) {
      engine.put(name, value);
   }

   public Object get(String name) {
      return engine.get(name);
   }

   public <T> T getAs(String name, Class<? extends T> tClass) {
      return Cast.as(engine.get(name), tClass);
   }

   public Object eval(String script) throws ScriptException {
      return engine.eval(script);
   }

   public Object eval(Reader script) throws ScriptException {
      return engine.eval(script);
   }

   public Object eval(String script, Bindings bindings) throws ScriptException {
      return engine.eval(script, bindings);
   }

   public Object eval(Reader script, Bindings bindings) throws ScriptException {
      return engine.eval(script, bindings);
   }

   public <T> T evalAs(String script, Class<? extends T> tClass) throws ScriptException {
      return Cast.as(engine.eval(script), tClass);
   }

   public <T> T evalAs(Reader script, Class<? extends T> tClass) throws ScriptException {
      return Cast.as(engine.eval(script), tClass);
   }

   public <T> T evalAs(String script, Bindings bindings, Class<? extends T> tClass) throws ScriptException {
      return Cast.as(engine.eval(script, bindings), tClass);
   }

   public <T> T evalAs(Reader script, Bindings bindings, Class<? extends T> tClass) throws ScriptException {
      return Cast.as(engine.eval(script, bindings), tClass);
   }

   public Object invokeFunction(String name, Object... params) throws ScriptException, NoSuchMethodException {
      Invocable invocable = Cast.as(engine);
      return invocable.invokeFunction(name, params);
   }

   public <T> T invokeFunctionAs(String name, Object[] params, Class<? extends T> tClass) throws ScriptException, NoSuchMethodException {
      Invocable invocable = Cast.as(engine);
      return Cast.as(invocable.invokeFunction(name, params), tClass);
   }

   public Object invokeMethod(Object object, String name, Object... params) throws ScriptException, NoSuchMethodException {
      Invocable invocable = Cast.as(engine);
      return invocable.invokeMethod(object, name, params);
   }

   public <T> T invokeMethodAs(Object object, String name, Object[] params, Class<? extends T> tClass) throws ScriptException, NoSuchMethodException {
      Invocable invocable = Cast.as(engine);
      return Cast.as(invocable.invokeMethod(object, name, params), tClass);
   }

   public Bindings createBindings() {
      return engine.createBindings();
   }

}
