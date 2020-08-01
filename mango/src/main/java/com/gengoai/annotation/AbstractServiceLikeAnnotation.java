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

package com.gengoai.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractServiceLikeAnnotation<T extends Annotation> extends AbstractProcessor {
   private final String OUTPUT_RESOURCE_NAME;
   private final Class<T> annotationClass;
   private final Set<String> output = new HashSet<>();

   protected AbstractServiceLikeAnnotation(String output_resource_name,
                                           Class<T> annotationClass) {
      OUTPUT_RESOURCE_NAME = output_resource_name;
      this.annotationClass = annotationClass;
   }

   @Override
   public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
   }

   @Override
   public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

      if (roundEnv.processingOver()) {
         Filer filer = processingEnv.getFiler();

         try {
            FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_RESOURCE_NAME);
            AnnotationUtils.writeOutput(output, f);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                                     "write '" + OUTPUT_RESOURCE_NAME + "': ");
         } catch (IOException x) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                                     "Failed to write '" + OUTPUT_RESOURCE_NAME + "': " + x);
         }

         return false;
      }

      Elements elements = processingEnv.getElementUtils();
      roundEnv.getElementsAnnotatedWith(annotationClass)
              .stream()
              .filter(Objects::nonNull)
              .filter(e -> e.getKind() == ElementKind.CLASS || e.getKind() == ElementKind.INTERFACE)
              .forEach(e -> {
                 TypeElement classElement = (TypeElement) e;
                 output.add(processElement(classElement, elements, classElement.getAnnotation(annotationClass)));
              });

      return false;
   }


   protected abstract String processElement(TypeElement e, Elements elements, T annotation);

}//END OF AbstractServiceLikeAnnotation
