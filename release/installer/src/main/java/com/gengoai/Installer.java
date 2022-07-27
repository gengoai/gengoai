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

import com.gengoai.application.Application;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.config.Config;
import com.gengoai.io.FileUtils;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.*;

/**
 * @author David B. Bracewell
 */
@Application.Description(
      "=======================================================================================\n" +
            "          The GengoAI Library and Model Installer\n" +
            "=======================================================================================\n" +
            "Usage: java -jar gengoai-installer.jar [CORE_LIBS, SPARK_LIBS, MODEL]\n" +
            "   CORE_LIBS: Installs Core libraries and scripts for running Hermes\n" +
            "   SPARK_LIBS: Installs Required Apache Spark libraries for running in Local Mode\n" +
            "   MODEL: Installs one or more specified models\n" +
            "----------------------------------------------------------------------------------------\n" +
            "     Installing models:\n" +
            "----------------------------------------------------------------------------------------\n" +
            "    java -jar gengoai-installer.jar MODEL LANGUAGE MODEL1 MODEL2 MODEL3 ... MODEL_N\n" +
            "The model may specify a package which is defined for each language and contains a\n" +
            "set of models (each Language will have a 'CORE' package which will install all \n" +
            "required models for that language). \n" +
            "To list the available models and packages for a language pass in the --ls command line \n" +
            "argument.\n" +
            "----------------------------------------------------------------------------------------\n" +
            " Command Line Arguments"
)
public class Installer extends CommandLineApplication {
   private static final long serialVersionUID = 1L;
   private static final Resource BASE_URL = Resources.from("https://gengoai.link/");
   private static final Resource RELEASES_URL = BASE_URL.getChild("releases/");
   private static final String MANIFEST_FILE = "manifest.json";
   private static final String HERMES_CORE_FILE = "hermes.tar.gz";
   private static final String SPARK_LIB_FILE = "hermes_spark.tar.gz";
   private static final String RELEASES_JSON = "releases.json";
   private static final String PACKAGES_JSON = "packages.json";

   @Option(description = "The directory to install the files to (defaults to '$HOME/hermes'.")
   private Resource installDir;

   @Option(description = "The model or release version to install", defaultValue = "latest", aliases = "v")
   private String version;

   public static void main(String[] args) {
      new Installer().run(args);
   }

   private void download(Resource url, Resource out) throws IOException {
      URLConnection connection = url.asURL().orElseThrow().openConnection();
      Resource tmpFile = Resources.temporaryFile();
      try (InputStream is = connection.getInputStream()) {
         FileUtils.copyWithProgress(is,
                                    tmpFile,
                                    connection.getContentLength(),
                                    pct -> progress("Downloading", FileUtils.baseName(url.baseName()), pct));
      }
      FileUtils.extract(tmpFile.asFile().orElseThrow(),
                        out.asFile().orElseThrow(),
                        (name, pct) -> progress("\tExtracting", name, pct));
   }

   private Set<String> gatherModels(Resource langDir, String[] args) throws Exception {
      PackageList packageList = Json.parse(langDir.getChild(PACKAGES_JSON), PackageList.class);
      Set<String> effectiveInstall = new HashSet<>();
      for (int i = 2; i < args.length; i++) {
         effectiveInstall.addAll(packageList.expand(args[i]));
      }
      return effectiveInstall;
   }

   private void installModel(Manifest manifest, String[] args) throws Exception {
      Validation.checkArgument(args.length > 2,
                               "Model installation should be expressed in the form: MODEL LANGUAGE PACKAGE1 PACKAGE2 ... PACKAGE N");

      Language language = Language.fromString(args[1].toUpperCase());
      Validation.checkArgument(language != Language.UNKNOWN, "Invalid language: '" + args[1] + "'");

      Resource rootDir = BASE_URL.getChild(Strings.appendIfNotPresent(manifest.languageDir(language), "/"));
      ModelDefinitions modelDefinitions = new ModelDefinitions(manifest.languageDir(language));
      Set<String> models = gatherModels(rootDir, args);

      installDir = installDir.getChild(language.getCode().toLowerCase());
      installDir.mkdirs();
      for (String model : models) {
         download(modelDefinitions.resolveDownloadURL(model, version), modelDefinitions
               .resolveTargetDir(model, installDir));
      }
   }

   private void listModels(Manifest manifest, String[] args) throws Exception {
      Validation.checkArgument(args.length > 1, "A language is required");
      Validation.checkArgument(!args[1].equals("--ls"), "A language is required");
      Language language = Language.fromString(args[1].toUpperCase());
      Validation.checkArgument(language != Language.UNKNOWN, "Invalid language: '" + args[1] + "'");

      Resource rootDir = BASE_URL.getChild(Strings.appendIfNotPresent(manifest.languageDir(language), "/"));
      ModelDefinitions modelDefinitions = new ModelDefinitions(manifest.languageDir(language));
      PackageList packageList = Json.parse(rootDir.getChild(PACKAGES_JSON), PackageList.class);
      System.out.printf("   Packages for %s\n", language);
      for (String s : packageList.packages.keySet()) {
         System.out.println(s);
      }
      System.out.println("-----------------------------------\n");
      modelDefinitions.print();
   }


   @Override
   protected void programLogic() throws Exception {
      String[] args = getPositionalArgs();
      Validation.checkArgument(args.length > 0,
                               "No installation resource specified. Must be one of [CORE_LIBS,SPARK_LIBS, MODEL]");
      Manifest manifest = Json.parse(BASE_URL.getChild(MANIFEST_FILE), Manifest.class);
      InstallResource resource = InstallResource.valueOf(getPositionalArgs()[0].toUpperCase());
      if (installDir == null) {
         installDir = Resources.from(SystemInfo.USER_HOME).getChild("hermes");
      }
      installDir.mkdirs();
      switch (resource) {
         case CORE_LIBS:
            download(RELEASES_URL.getChild(manifest.effectiveReleaseVersion(version) + "/")
                                 .getChild(HERMES_CORE_FILE), installDir);
            File hermes = installDir.getChild("hermes").asFile().orElseThrow();
            hermes.setExecutable(true);
            break;
         case SPARK_LIBS:
            download(RELEASES_URL.getChild(manifest.effectiveReleaseVersion(version) + "/")
                                 .getChild(SPARK_LIB_FILE), installDir);
            break;
         case MODEL:
            if (Config.get("ls").asBooleanValue(false)) {
               listModels(manifest, args);
            } else {
               if (installDir.getChild("config.conf").exists()) {
                  installDir.getChild("config.conf").copy(installDir.getChild("config.conf.bak"));
                  installDir.getChild("config.conf")
                            .append("\n\nhermes.resources.dir=\"" + installDir.path() + "\"\n");
               }
               installModel(manifest, args);
            }
      }
   }

   private void progress(String prefix, String file, int amount) {
      String s = String.format("%s '%s'", prefix, file);
      String bar = Strings.repeat("#", amount / 5);
      if (bar.length() < 20) {
         bar += ">";
      }
      String progress = Strings.padEnd(bar, 20, ' ');
      if (amount < 100) {
         System.out.printf("%s [%s] (%d%%)\r", s, progress, amount);
      } else {
         System.out.printf("%s [%s] (100%%)\n", s, progress);
      }
   }

   public enum InstallResource {
      CORE_LIBS,
      SPARK_LIBS,
      MODEL
   }

   public static class ModelDefinitions implements Serializable {
      private static final long serialVersionUID = 1L;
      Map<String, ModelEntry> models = new HashMap<>();
      Map<String, String> defaultVariants = new HashMap<>();
      private String languageDir;

      public ModelDefinitions(String languageDir) throws Exception {
         this.languageDir = languageDir;
         Map<String, JsonEntry> json = Json.parseObject(BASE_URL.getChild(Strings.appendIfNotPresent(languageDir, "/"))
                                                                .getChild(RELEASES_JSON));
         json.forEach((m, j) -> {
            if (j.hasProperty("variants")) {
               defaultVariants.put(m, j.getStringProperty("default"));
               j.getProperty("variants")
                .forEachProperty((vName, vJson) -> {
                   String name = m + "-" + vName;
                   ModelEntry e = vJson.as(ModelEntry.class);
                   e.target = j.getStringProperty("target");
                   models.put(name, e);
                });
            } else {
               models.put(m, j.as(ModelEntry.class));
            }
         });
      }

      public void print() {
         if (defaultVariants.size() > 0) {
            System.out.println("----- Default Variants -------");
            defaultVariants.forEach((m, d) -> System.out.println(m + " => " + m + "-" + d));
            System.out.println("---------------------------\n");
         }
         models.forEach((name, entry) -> {
            System.out.println("---------------------------");
            System.out.println(name);
            System.out.println("---------------------------");
            if (!Strings.isNullOrBlank(entry.getDescription())) {
               System.out.println(entry.getDescription());
            }
            System.out.println("latest: " + entry.getLatest());
            System.out.println("versions: " + entry.getVersions());
            System.out.println("---------------------------");
            System.out.println();
         });
      }

      public Resource resolveDownloadURL(String model, String version) {
         model = model.toLowerCase();
         model = defaultVariants.getOrDefault(model, model);
         ModelEntry e = models.get(model);
         Validation.notNull(e, "Unknown Model '" + model + "'");

         version = version.toLowerCase();
         if (version.equals("latest")) {
            version = e.latest;
         }
         Validation.checkArgument(e.versions.contains(version), "Unknown Version: '" + version + "'");

         return BASE_URL.getChild(languageDir + "/")
                        .getChild(version + "/")
                        .getChild(String.format("%s.tar.gz", model));
      }

      public Resource resolveTargetDir(String model, Resource installDir) {
         model = model.toLowerCase();
         model = defaultVariants.getOrDefault(model, model);
         ModelEntry e = models.get(model);
         Validation.notNull(e, "Unknown Model '" + model + "'");

         return installDir.getChild(e.target);
      }

   }

   @Data
   public static class ModelEntry implements Serializable {
      private static final long serialVersionUID = 1L;
      private String description;
      private String latest;
      private List<String> versions;
      private String target;
   }

   @Data
   public static class PackageList implements Serializable {
      private static final long serialVersionUID = 1L;
      private Map<String, List<String>> packages;

      public List<String> expand(String model) {
         model = model.toLowerCase();
         return packages.getOrDefault(model, Collections.singletonList(model));
      }

   }

   @Data
   public static class Manifest implements Serializable {
      private String latest;
      private List<String> versions;
      private Map<String, String> languages;


      public String effectiveReleaseVersion(String version) {
         version = version.toLowerCase();
         if (version.equals("latest")) {
            return latest;
         }
         Validation.checkArgument(versions.contains(version), "Unknown Version: '" + version + "'");
         return version;
      }

      public String languageDir(Language language) {
         String dir = languages.get(language.toString());
         Validation.notNullOrBlank(dir, "No models exist for '" + language + "'");
         return dir;
      }
   }


}//END OF Downloader
