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

package com.gengoai.hermes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Language;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.io.FileUtils;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * @author David B. Bracewell
 */
public class Downloader extends CommandLineApplication {
   private static final String REPO_URL = "/home/ik/prj/gengoai/models/models.json";
   private static final String URL = "https://github.com/gengoai/models/releases/download/%s/%s.tar.gz";
   private static final Type MAP_DATA_ENTRY = parameterizedType(Map.class, String.class, DataEntry.class);
   @Option(description = "Model version to download", defaultValue = "latest", aliases = {"v"})
   private String version;
   @Option(description = "Language of Models to download", required = true, aliases = {"l"})
   private Language language;
   @Option(description = "Variant names", defaultValue = "{}")
   private Map<String, String> variants;

   private Map<String, DataEntry> dataDefinitions;

   public static void main(String[] args) {
      new Downloader().run(args);
   }

   private void download(@NonNull Language language,
                         @NonNull String model,
                         @NonNull String version,
                         @NonNull String variant) throws Exception {


      model = model.toLowerCase();
      if (!dataDefinitions.containsKey(model)) {
         throw new RuntimeException("Unknown Model: " + model);
      }

      Resource targetDir = Hermes.getResourcesDir().getChild(language.getCode().toLowerCase());
      DataEntry dataEntry = dataDefinitions.get(model);

      version = version.toLowerCase();
      if (version.equals("latest")) {
         version = dataEntry.getLatest();
      }

      if (!dataEntry.versions.contains(version)) {
         throw new IllegalArgumentException("Unknown version '" + version + "' for '" + model + "'");
      }


      variant = Strings.nullToEmpty(variant).toLowerCase();
      if (Strings.isNullOrBlank(variant) || variant.equalsIgnoreCase("*")) {
         variant = dataEntry.defaultVariant == null
               ? model
               : dataEntry.defaultVariant;
      }

      if (dataEntry.variants != null) {
         if (!dataEntry.variants.containsKey(variant)) {
            throw new IllegalArgumentException("Unknown variant '" + variant + "' for '" + model + "'");
         }
      } else if (!variant.equalsIgnoreCase(model)) {
         throw new IllegalArgumentException("Unknown variant '" + variant + "' for '" + model + "'");
      }

      String file = model;
      if (!variant.equalsIgnoreCase(model)) {
         file += "-" + variant;
      }

      Resource tarxz = Resources.from(String.format(URL,
                                                    language.getCode().toLowerCase() + "-" + version,
                                                    file));
      Resource tmp = Resources.temporaryFile();
      System.out.printf("Downloading %s %s...", language, model);
      tarxz.copy(tmp);
      System.out.println(" COMPLETED");
      System.out.printf("Extracting %s %s... ", language, model);
      try {
         FileUtils.extract(tmp.asFile().orElseThrow(),
                           targetDir.asFile().orElseThrow());
         System.out.println(" COMPLETED");
      } catch (FileAlreadyExistsException e) {
         System.out.println(" FAILED");
         System.err
               .printf("%s %s already exists, if you wish to update the file please delete the existing one first.\n", language, model);
      }

   }


   @Override
   protected void programLogic() throws Exception {
      if (getPositionalArgs().length == 0) {
         throw new IllegalStateException("No Operation Given");
      }

      dataDefinitions = Json.parse(Resources.from(REPO_URL), MAP_DATA_ENTRY);
      variants = (variants == null) ? Collections.emptyMap() : variants;
      String model = (getPositionalArgs().length > 1
            ? getPositionalArgs()[1]
            : Strings.EMPTY).toLowerCase();

      switch (getPositionalArgs()[0].toLowerCase()) {
         case "ls":
            dataDefinitions.entrySet()
                           .stream()
                           .sorted(Map.Entry.comparingByKey())
                           .forEach(e -> e.getValue().print(e.getKey()));
            break;
         case "info":
            if (dataDefinitions.containsKey(model)) {
               dataDefinitions.get(model).print(model);
            } else {
               throw new IllegalArgumentException("Unknown model '" + model + "'");
            }
            break;
         case "core":
            break;
         case "get":
            download(language, model, version, variants.getOrDefault(model, "*"));
            break;
         case "all":
            for (String name : dataDefinitions.keySet()) {
               download(language, name, version, variants.getOrDefault(name, "*"));
            }
            break;
         default:
            throw new IllegalArgumentException("Unknown command '" + getPositionalArgs()[0] + "'");
      }

   }

   @Data
   private static class DataEntry {
      private String description;
      private String latest;
      private Set<String> versions;
      @JsonProperty("default")
      private String defaultVariant;
      private Map<String, String> variants;

      public void print(String name) {
         System.out.println("---------------------------");
         System.out.println(name);
         System.out.println("---------------------------");
         if (!Strings.isNullOrBlank(getDescription())) {
            System.out.println(getDescription());
         }
         System.out.println("latest: " + getLatest());
         System.out.println("versions: " + getVersions());
         if (!Strings.isNullOrBlank(getDefaultVariant())) {
            System.out.println("default: " + getDefaultVariant());
         }
         if (getVariants() != null) {
            System.out.println("---------------------------");
            System.out.println("Variants");
            System.out.println("---------------------------");
            getVariants()
                  .forEach((k, v) -> System.out.println(k + ": " + v));
         }
         System.out.println("---------------------------");
         System.out.println();
      }

   }
}//END OF Downloader
