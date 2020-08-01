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
 */

package com.gengoai.reflection;

import com.gengoai.collection.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class TestBean {


   private Set<String> children;
   private final String name;
   private Map<String, Double> stocks = new HashMap<>();

   public TestBean() {
      this.name = "";
      this.children = new HashSet<>();
   }

   public TestBean(String name) {
      this.name = name;
      this.children = new HashSet<>();
   }

   public TestBean(String name, String... children) {
      this.name = name;
      this.children = Sets.hashSetOf(children);
   }

   public String getName() {
      return name;
   }

   public Set<String> getChildren() {
      return children;
   }

   public void setChildren(Set<String> children) {
      this.children = children;
   }

   public Map<String, Double> getStocks() {
      return stocks;
   }

   public void setStocks(Map<String, Double> stocks) {
      this.stocks = stocks;
   }

   @Override
   public String toString() {
      return "TestBean{" +
                "children=" + children +
                ", name='" + name + '\'' +
                ", stocks=" + stocks +
                '}';
   }
}
