package com.gengoai.annotation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Collection;

/**
 * @author David B. Bracewell
 */
final class AnnotationUtils {

   public static String getClassName(Elements elements, TypeMirror mirror) {
      DeclaredType dt = (DeclaredType) mirror;
      TypeElement te = (TypeElement) dt.asElement();
      return elements.getBinaryName(te).toString();
   }

   public static void readCurrent(Collection<String> target, FileObject fileObject) throws IOException {
      try {
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileObject.openInputStream(),
                                                                               StandardCharsets.UTF_8))) {
            reader.lines().forEach(target::add);
         }
      } catch (FileNotFoundException | NoSuchFileException x) {
         // ignore because we don't care at this point
      }
   }

   public static void writeOutput(Collection<String> collection, FileObject fileObject) throws IOException {
      try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(fileObject.openOutputStream(), StandardCharsets.UTF_8))) {
         for (String line : collection) {
            writer.write(line);
            writer.newLine();
         }
      }
   }

}//END OF AnnotationUtils
