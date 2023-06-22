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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                "   RESOURCE: Installs a specified resources\n" +
                "----------------------------------------------------------------------------------------\n" +
                "     Installing resources:\n" +
                "----------------------------------------------------------------------------------------\n" +
                "    java -jar gengoai-installer.jar RESOURCE NAME VERSION\n" +
                "The resource specifies a specific resource, e.g. en_core, to download and install.\n" +
                "A version can be given or omitted in which case the latest version of resource will be\n " +
                "retrieved \n" +
                "To list the available models and packages for a language pass in the --ls command line \n" +
                "argument.\n" +
                "----------------------------------------------------------------------------------------\n" +
                " Command Line Arguments"
)
public class Installer extends CommandLineApplication {
    private static final long serialVersionUID = 1L;
    private static final Resource RAW_ROOT = Resources.from("https://raw.githubusercontent.com/gengoai/models/main/");
    private static final Resource MANIFEST_FILE = Resources.from("https://raw.githubusercontent.com/gengoai/models/main/manifest.json");
    private static final Resource RELEASES_URL = Resources.from("https://github.com/gengoai/models/releases/download/");
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

    private void download(Resource url, Resource out, boolean extract) throws IOException {
        URLConnection connection = url.asURL().orElseThrow().openConnection();
        Resource tmpFile = Resources.temporaryFile();
        try (InputStream is = connection.getInputStream()) {
            FileUtils.copyWithProgress(is,
                    tmpFile,
                    connection.getContentLength(),
                    pct -> progress("Downloading", FileUtils.baseName(url.baseName()), pct));
        }
        if (extract) {
            FileUtils.extract(tmpFile.asFile().orElseThrow(),
                    out.asFile().orElseThrow(),
                    (name, pct) -> progress("\tExtracting", name, pct));
        } else {
            tmpFile.copy(out);
        }
    }

    private void installModel(Manifest manifest, String[] args) throws Exception {
        Validation.checkArgument(args.length > 1,
                "Model installation should be expressed in the form: RESOURCE NAME VERSION");

        final String resource_name = args[1] + ".json";

        if (!manifest.resources.containsKey(args[1])) {
            System.err.println("Unknown resource: " + args[1]);
            System.exit(-1);
        }

        String version = "latest";
        if (args.length > 2) {
            version = args[2].toLowerCase();
        }


        PackageDef pkg = Json.parse(RAW_ROOT.getChild(resource_name), PackageDef.class);
        if (version.toLowerCase().equals("latest")) {
            version = pkg.latest;
        }

        for (JsonEntry file : pkg.versions.get(version)) {
            Resource url = Resources.from(file.getStringProperty("url"));
            String resourceType = file.getStringProperty("resource").toLowerCase();
            boolean extract = file.getBooleanProperty("extract", false);
            Resource targetDir = installDir.getChild("resources").getChild(pkg.language).getChild(resourceType);
            if (file.hasProperty("targetName")) {
                targetDir = targetDir.getChild(file.getStringProperty("targetName"));
            }
            download(url, targetDir, extract);
        }
    }

    private void listModels(Manifest manifest, String[] args) throws Exception {
        var sorted = new TreeMap<>(manifest.resources);
        sorted.forEach((String name, String desc) -> System.out.println(name + ": " + desc));
    }


    @Override
    protected void programLogic() throws Exception {
        String[] args = getPositionalArgs();
        Validation.checkArgument(args.length > 0,
                "No installation resource specified. Must be one of [CORE_LIBS,SPARK_LIBS, RESOURCE]");
        Manifest manifest = Json.parse(MANIFEST_FILE, Manifest.class);
        InstallResource resource = InstallResource.valueOf(getPositionalArgs()[0].toUpperCase());
        if (installDir == null) {
            installDir = Resources.from(SystemInfo.USER_HOME).getChild("hermes");
        }
        installDir.mkdirs();
        switch (resource) {
            case CORE_LIBS:
                download(RELEASES_URL.getChild(manifest.effectiveReleaseVersion(version) + "/")
                        .getChild(HERMES_CORE_FILE), installDir, true);
                File hermes = installDir.getChild("hermes").asFile().orElseThrow();
                hermes.setExecutable(true);
                break;
            case SPARK_LIBS:
                download(RELEASES_URL.getChild(manifest.effectiveReleaseVersion(version) + "/")
                        .getChild(SPARK_LIB_FILE), installDir, true);
                break;
            case RESOURCE:
                if (Config.get("ls").asBooleanValue(false)) {
                    listModels(manifest, args);
                } else {
                    if (installDir.getChild("config.conf").exists()) {
                        installDir.getChild("config.conf").copy(installDir.getChild("config.conf.bak"));
                        installDir.getChild("config.conf")
                                .append("\n\nhermes.resources.dir=\"" + installDir.getChild("resources").path() + "\"\n");
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
        RESOURCE
    }


    @Data
    public static class PackageDef implements Serializable {
        String language;
        String latest;
        Map<String, List<JsonEntry>> versions;
    }

//    public static class ModelDefinitions implements Serializable {
//        private static final long serialVersionUID = 1L;
//        Map<String, ModelEntry> models = new HashMap<>();
//        Map<String, String> defaultVariants = new HashMap<>();
//        private String languageDir;
//
//        public ModelDefinitions(String languageDir) throws Exception {
////            this.languageDir = languageDir;
////            Map<String, JsonEntry> json = Json.parseObject(BASE_URL.getChild(Strings.appendIfNotPresent(languageDir, "/"))
////                    .getChild(RELEASES_JSON));
////            json.forEach((m, j) -> {
////                if (j.hasProperty("variants")) {
////                    defaultVariants.put(m, j.getStringProperty("default"));
////                    j.getProperty("variants")
////                            .forEachProperty((vName, vJson) -> {
////                                String name = m + "-" + vName;
////                                ModelEntry e = vJson.as(ModelEntry.class);
////                                e.target = j.getStringProperty("target");
////                                models.put(name, e);
////                            });
////                } else {
////                    models.put(m, j.as(ModelEntry.class));
////                }
////            });
//        }
//
//        public void print() {
//            if (defaultVariants.size() > 0) {
//                System.out.println("----- Default Variants -------");
//                defaultVariants.forEach((m, d) -> System.out.println(m + " => " + m + "-" + d));
//                System.out.println("---------------------------\n");
//            }
//            models.forEach((name, entry) -> {
//                System.out.println("---------------------------");
//                System.out.println(name);
//                System.out.println("---------------------------");
//                if (!Strings.isNullOrBlank(entry.getDescription())) {
//                    System.out.println(entry.getDescription());
//                }
//                System.out.println("latest: " + entry.getLatest());
//                System.out.println("versions: " + entry.getVersions());
//                System.out.println("---------------------------");
//                System.out.println();
//            });
//        }
//
//        public Resource resolveDownloadURL(String model, String version) {
//            model = model.toLowerCase();
//            model = defaultVariants.getOrDefault(model, model);
//            ModelEntry e = models.get(model);
//            Validation.notNull(e, "Unknown Model '" + model + "'");
//
//            version = version.toLowerCase();
//            if (version.equals("latest")) {
//                version = e.latest;
//            }
//            Validation.checkArgument(e.versions.contains(version), "Unknown Version: '" + version + "'");
//
//            return null;
////            BASE_URL.getChild(languageDir + "/")
////                    .getChild(version + "/")
////                    .getChild(String.format("%s.tar.gz", model));
//        }
//
//        public Resource resolveTargetDir(String model, Resource installDir) {
//            model = model.toLowerCase();
//            model = defaultVariants.getOrDefault(model, model);
//            ModelEntry e = models.get(model);
//            Validation.notNull(e, "Unknown Model '" + model + "'");
//
//            return installDir.getChild(e.target);
//        }
//
//    }
//
//    @Data
//    public static class ModelEntry implements Serializable {
//        private static final long serialVersionUID = 1L;
//        private String description;
//        private String latest;
//        private List<String> versions;
//        private String target;
//    }
//
//    @Data
//    public static class PackageList implements Serializable {
//        private static final long serialVersionUID = 1L;
//        private Map<String, List<String>> packages;
//
//        public List<String> expand(String model) {
//            model = model.toLowerCase();
//            return packages.getOrDefault(model, Collections.singletonList(model));
//        }
//
//    }

    @Data
    public static class Manifest implements Serializable {
        private String latest;
        private List<String> versions;
        private Map<String, String> resources;


        public String effectiveReleaseVersion(String version) {
            version = version.toLowerCase();
            if (version.equals("latest")) {
                return latest;
            }
            Validation.checkArgument(versions.contains(version), "Unknown Version: '" + version + "'");
            return version;
        }
    }


}//END OF Downloader
