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

package com.gengoai.sql.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gengoai.sql.NamedSQLElement;
import com.gengoai.sql.SQLContext;
import com.gengoai.sql.statement.Create;
import com.gengoai.sql.statement.Drop;
import lombok.NonNull;

import java.sql.SQLException;

/**
 * Base class for SQL objects, e.g. (Tables, Columns, Triggers, etc.)
 */
public abstract class SQLObject implements NamedSQLElement {
   private static final long serialVersionUID = 1L;
   protected String name;

   /**
    * Instantiates a new SQLObject.
    */
   protected SQLObject() {
      this.name = null;
   }

   /**
    * Instantiates a new SQLObject.
    *
    * @param name the name of the object
    */
   protected SQLObject(String name) {
      this.name = name;
   }


   public boolean create(@NonNull SQLContext context) throws SQLException {
      return new Create(this).update(context) > 0;
   }

   /**
    * Creates a new {@link Create} statement setting the object to be created as this object.
    *
    * @return the create statement
    */
   public Create create() {
      return new Create(this);
   }

   public boolean createIfNotExists(@NonNull SQLContext context) throws SQLException {
      return new Create(this).ifNotExists().update(context) > 0;
   }

   public boolean drop(@NonNull SQLContext context) throws SQLException {
      return new Drop(this).update(context) > 0;
   }

   /**
    * Creates a new {@link Drop} statement setting the object to be created as this object.
    *
    * @return the drop statement
    */
   public Drop drop() {
      return new Drop(this);
   }

   public boolean dropIfExists(@NonNull SQLContext context) throws SQLException {
      return new Drop(this).ifExists().update(context) > 0;
   }


   /**
    * Gets the SQL Keyword associated with the object
    *
    * @return the SQL keyword
    */
   @JsonIgnore
   public abstract String getKeyword();

   /**
    * Gets the name of object.
    *
    * @return the name pf the object
    */
   public final String getName() {
      return name;
   }

}//END OF SQLObject
