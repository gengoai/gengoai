package com.gengoai.io.resource;

import com.gengoai.io.FileUtils;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The type Zip resource.
 *
 * @author David B. Bracewell
 */
public class ZipResource extends BaseResource implements ReadOnlyResource {
   private ZipEntry entry;
   private ZipFile zipFile;

   /**
    * Instantiates a new Zip resource.
    *
    * @param zipFile the zip file
    * @param entry   the entry
    */
   public ZipResource(String zipFile, String entry) {
      try {
         this.zipFile = new ZipFile(zipFile.replaceFirst("^file:", ""));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      if (Strings.isNotNullOrBlank(entry)) {
         if (entry.startsWith("/")) {
            entry = entry.substring(1);
         }
         this.entry = this.zipFile.getEntry(entry);
      }
   }

   @Override
   public String baseName() {
      return entry == null
            ? Strings.EMPTY
            : FileUtils.baseName(entry.getName());
   }

   @Override
   protected InputStream createInputStream() throws IOException {
      return zipFile.getInputStream(entry);
   }

   private int depth(String s) {
      if (s.endsWith("/")) {
         return Strings.count(s, "/") - 1;
      }
      return Strings.count(s, "/");
   }

   @Override
   public String descriptor() {
      return zipFile.getName() + (entry == null
            ? ""
            : "!" + entry.getName());
   }

   @Override
   public boolean exists() {
      try {
         if (zipFile == null || entry == null) {
            return false;
         }
         createInputStream();
         return true;
      } catch (IOException | ExceptionInInitializerError e) {
         return false;
      }
   }

   @Override
   public Resource getChild(String relativePath) {
      if (entry == null) {
         return new ZipResource(zipFile.getName(), relativePath);
      }
      return new ZipResource(zipFile.getName(), Strings.appendIfNotPresent(entry.getName(), "/") + relativePath);
   }

   @Override
   public List<Resource> getChildren(Pattern pattern, boolean recursive) {
      if (entry != null && !entry.isDirectory()) {
         return Collections.emptyList();
      }
      List<Resource> resources = new ArrayList<>();
      Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
      final int tDepth = depth(entry.getName());
      final String path = entry.getName();
      while (enumeration.hasMoreElements()) {
         final String ze = enumeration.nextElement().getName();
         if (ze.startsWith(path)
               && ze.length() > path.length()
               && (recursive || depth(ze) == (tDepth + 1))
               && pattern.matcher(ze.substring(path.length() + 1)).matches()) {
            resources.add(new ZipResource(zipFile.getName(), ze));
         }
      }
      return resources;
   }

   @Override
   public Resource getParent() {
      return new ZipResource(zipFile.getName(), entry.getName());
   }

   @Override
   public boolean isDirectory() {
      return entry != null && entry.isDirectory();
   }
}//END OF ZipResource
