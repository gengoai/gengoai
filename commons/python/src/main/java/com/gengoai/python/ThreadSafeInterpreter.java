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
import jep.SharedInterpreter;

import java.util.Map;

public class ThreadSafeInterpreter {
   private static final ThreadLocal<MonitoredObject<SharedInterpreter>> interpreter =
         ThreadLocal.withInitial(() -> ResourceMonitor.monitor(new SharedInterpreter()));

   private static void exec(String pythonCode) {
      ThreadSafeInterpreter.interpreter.get().object.exec(pythonCode);
   }

   private static Object getValue(String variable) {
      return ThreadSafeInterpreter.interpreter.get().object.getValue(variable);
   }

   private static <T> T getValue(String variable, Class<T> tClass) {
      return ThreadSafeInterpreter.interpreter.get().object.getValue(variable, tClass);
   }

   private static boolean eval(String pythonCode) {
      return ThreadSafeInterpreter.interpreter.get().object.eval(pythonCode);
   }

   private static void runScript(String script) {
      ThreadSafeInterpreter.interpreter.get().object.runScript(script);
   }

   private static Object invoke(String function, Object... objects) {
      return ThreadSafeInterpreter.interpreter.get().object.invoke(function, objects);
   }

   private static Object invoke(String function, Map<String, Object> map) {
      return ThreadSafeInterpreter.interpreter.get().object.invoke(function, map);
   }

   private static Object invoke(String function, Object[] objects, Map<String, Object> map) {
      return ThreadSafeInterpreter.interpreter.get().object.invoke(function, objects, map);
   }

   public static void main(String[] args) {
      new Thread(() -> {
         ThreadSafeInterpreter.eval("import tensorflow as tf");
         System.out.println(ThreadSafeInterpreter.getValue("tf.__version__"));
         ThreadSafeInterpreter.exec("def add(x,y): return x +y");
         System.out.println(ThreadSafeInterpreter.invoke("add", 2, 3));
      }).run();
      new Thread(() -> {
         ThreadSafeInterpreter.exec("def add(x,y): return x +y");
         System.out.println(ThreadSafeInterpreter.invoke("add", 4, 3));
      }).run();
   }

}
