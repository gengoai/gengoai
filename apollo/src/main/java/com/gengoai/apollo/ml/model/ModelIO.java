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

package com.gengoai.apollo.ml.model;

import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import com.gengoai.io.SaveMode;
import com.gengoai.io.resource.Resource;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import lombok.NonNull;

import java.io.IOException;

/**
 * <p>
 * Methods for saving and loading models.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class ModelIO {

   /**
    * Loads a model from the given resource
    *
    * @param resource the resource containing the model
    * @return the model
    * @throws IOException Something went wrong reading the resource
    */
   public static <T extends Model> T load(@NonNull String resource) throws IOException {
      return load(Resources.from(resource));
   }

   /**
    * Loads a model from the given resource
    *
    * @param resource the resource containing the model
    * @return the model
    * @throws IOException Something went wrong reading the resource
    */
   public static <T extends Model> T load(@NonNull Resource resource) throws IOException {
      return load(Cast.as(Reflect.getClassForNameQuietly(resource.getChild("__class__").readToString().strip())),
                  resource);
   }

   /**
    * Loads a model from the given resource
    *
    * @param modelClass the class of the model to load
    * @param resource   the resource containing the model
    * @return the model
    * @throws IOException Something went wrong reading the resource
    */
   public static <T extends Model> T load(@NonNull Class<? extends Model> modelClass,
                                          @NonNull Resource resource) throws IOException {
      try {
         Reflect r = Reflect.onClass(modelClass);
         if(r.containsMethod("load")) {
            return r.getMethod("load").invoke(resource);
         }
      } catch(ReflectionException e) {
         throw new IOException(e);
      }
      return Cast.as(Model.load(resource));
   }

   /**
    * Saves a model to the given resource
    *
    * @param model    the model to save
    * @param resource the resource where to write the model
    * @param saveMode the save mode
    * @throws IOException Something went wrong writing the resource
    */
   public static void save(@NonNull Model model,
                           @NonNull Resource resource,
                           @NonNull SaveMode saveMode) throws IOException {
      if(saveMode.validate(resource)) {
         resource.delete(true);
         resource.mkdirs();
         resource.getChild("__class__")
                 .write(model.getClass().getName());
         model.save(resource);
      }
   }

   /**
    * Saves a model to the given resource
    *
    * @param model    the model to save
    * @param resource the resource where to write the model
    * @throws IOException Something went wrong writing the resource
    */
   public static void save(@NonNull Model model, @NonNull Resource resource) throws IOException {
      save(model, resource, SaveMode.OVERWRITE);
   }

   private ModelIO() {
      throw new IllegalAccessError();
   }

}//END OF ModelIO
