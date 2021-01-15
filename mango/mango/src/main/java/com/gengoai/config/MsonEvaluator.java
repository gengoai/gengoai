/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.config;

import com.gengoai.collection.Iterables;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.ClasspathResource;
import com.gengoai.json.JsonEntry;
import com.gengoai.parsing.*;
import com.gengoai.string.Strings;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.gengoai.LogUtils.logFinest;
import static com.gengoai.function.CheckedConsumer.asFunction;

/**
 * @author David B. Bracewell
 */
@Log
class MsonEvaluator extends Evaluator<Expression> {
   final String resourceName;
   final LinkedList<String> scope = new LinkedList<>();

   MsonEvaluator(String resourceName) {
      this.resourceName = resourceName;
      $(UnaryOperatorExpression.class,
        ConfigTokenType.IMPORT,
        asFunction(exp -> handleImport(exp.getValue().as(ValueExpression.class).getValue().asString())));
      $(BinaryInfixOperatorExpression.class,
        ConfigTokenType.EQUAL_PROPERTY,
        asFunction(this::handleProperty));
      $(BinaryInfixOperatorExpression.class,
        ConfigTokenType.APPEND_PROPERTY,
        asFunction(this::handleAppendProperty));
      $(BinaryInfixOperatorExpression.class,
        ConfigTokenType.BEGIN_OBJECT,
        asFunction(this::handleSection));
   }

   private JsonEntry convertExpression(Expression exp) throws ParseException {
      if(exp.isInstance(ValueExpression.class)) {
         return JsonEntry.from(exp.as(ValueExpression.class).getValue().get());
      }

      if(exp.isInstance(ListExpression.class, ConfigTokenType.BEGIN_ARRAY)) {
         ListExpression arrayExpression = exp.as(ListExpression.class);
         JsonEntry array = JsonEntry.array();
         for(Expression e : arrayExpression) {
            array.addValue(convertExpression(e));
         }
         return array;
      }

      if(exp.isInstance(ListExpression.class, ConfigTokenType.BEGIN_OBJECT)) {
         ListExpression mve = exp.as(ListExpression.class);
         JsonEntry map = JsonEntry.object();
         for(Expression e : mve) {
            BinaryInfixOperatorExpression boe = e.as(BinaryInfixOperatorExpression.class);
            String key = boe.getLeft().as(ValueExpression.class).getValue().asString();
            map.addProperty(key, convertExpression(boe.getRight()));
         }
         return map;
      }

      throw new ParseException("Unexpected Expression: " + exp);
   }

   private String effectiveKey(String key) {
      String effectiveKey;
      if(scope.isEmpty()) {
         effectiveKey = key;
      } else {
         effectiveKey = Strings.join(scope, ".") + "." + key;
      }
      if(effectiveKey.endsWith("._") && effectiveKey.length() > 2) {
         effectiveKey = effectiveKey.substring(0, effectiveKey.length() - 2);
      }
      return effectiveKey;
   }

   private void handleAppendProperty(BinaryInfixOperatorExpression exp) throws ParseException {
      String key = effectiveKey(exp.getLeft().as(ValueExpression.class).getValue().asString());
      List<Object> list = Config.get(key).asList(Object.class);
      if(list == null) {
         list = new ArrayList<>();
      }
      processJson(list, convertExpression(exp.getRight()));
      Config.getInstance()
            .setterFunction
            .setProperty(key, JsonEntry.array(list).toString(), resourceName);
   }

   private void handleImport(String importString) throws ParseException {
      logFinest(log, "Handing Import of {0}", importString);
      if(!importString.endsWith(Config.CONF_EXTENSION) && importString.contains("/")) {
         //We don't have a MSON extension at the end and the import string is a path
         throw new ParseException(String.format("Invalid Import Statement (%s)", importString));
      }
      String path = Config.resolveVariables(importString).trim();
      if(path.contains("/")) {
         if(path.startsWith("file:")) {
            logFinest(log, "Loading config from: {0}", path);
            Config.loadConfig(Resources.from(path));
         } else {
            logFinest(log, "Loading config from resource: {0}", path);
            Config.loadConfig(new ClasspathResource(path));
         }
      } else {
         logFinest(log, "Loading package config: {0}", path);
         Config.loadPackageConfig(path);
      }
   }

   private void handleProperty(BinaryInfixOperatorExpression exp) throws ParseException {
      logFinest(log, "Handling property: {0}", exp);
      String key = effectiveKey(convertExpression(exp.getLeft()).asString());
      logFinest(log, "Effective key: {0}", key);
      JsonEntry value = convertExpression(exp.getRight());
      String stringValue = value.isPrimitive()
                           ? value.get().toString()
                           : value.toString();
      Config.getInstance().setterFunction.setProperty(key, stringValue, resourceName);
   }

   private void handleSection(BinaryInfixOperatorExpression exp) throws ParseException {
      String section = exp.getLeft().as(ValueExpression.class).getValue().asString();
      scope.addLast(section);
      ListExpression mve = exp.getRight().as(ListExpression.class);
      for(Expression expression : mve) {
         BinaryInfixOperatorExpression boe = expression.as(BinaryInfixOperatorExpression.class);
         if(boe.getType().isInstance(ConfigTokenType.BEGIN_OBJECT)) {
            handleSection(boe);
         } else if(boe.getType().equals(ConfigTokenType.APPEND_PROPERTY)) {
            handleAppendProperty(boe);
         } else {
            handleProperty(boe);
         }
      }
      scope.removeLast();
   }

   private void processJson(List<Object> list, JsonEntry entry) {
      if(entry.isNull()) {
         list.add(null);
      } else if(entry.isPrimitive()) {
         list.add(entry.get());
      } else if(entry.isObject()) {
         list.add(entry.asMap());
      } else if(entry.isArray()) {
         for(JsonEntry e : Iterables.asIterable(entry.elementIterator())) {
            processJson(list, e);
         }
      }
   }
}//END OF MsonEvaluator
