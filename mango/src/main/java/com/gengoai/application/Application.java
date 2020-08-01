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

package com.gengoai.application;

import com.gengoai.LogUtils;
import com.gengoai.config.Config;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Set;

import static com.gengoai.LogUtils.logSevere;

/**
 * <p>Generic interface for building applications that use Mango's {@link Config} and {@link CommandLineParser} to
 * reduce the boilerplate of application configuration and command line parsing. The command line parser is constructed
 * during the call to {@link #run(String[])} and has it options set to fields in the application defined using the
 * {@link Option} annotation.</p>
 *
 * <p>Applications provide access to all arguments on the command line through {@link #getAllArguments()} and to
 * positional (non-parsed) objects through {@link #getPositionalArgs()}. Application flow begins with a call to {@link
 * #run(String[])} which loads the config files and parses the command line and then calls {@link #setup()} to setup the
 * application, and finally {@link #run()} implementing the run logic. Note that additional config packages can be
 * loaded by defining additional packages in {@link #getDependentPackages()}</p>
 *
 * <p>A <code>Description</code> annotation can be added to a class to provide a short description of what the program
 * does and will be displayed as part of the help screen. </p>
 *
 * @author David B. Bracewell
 */
public abstract class Application implements Runnable, Serializable {
   private static final long serialVersionUID = 1L;
   private final String name;
   private String[] allArguments = new String[0];
   private String[] positionalArguments = new String[0];

   public Application() {
      this(null);
   }

   public Application(String name) {
      this.name = Strings.isNullOrBlank(name)
                  ? getClass().getSimpleName()
                  : name;
      try {
         LogUtils.addFileHandler(this.name);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * <p>Get all arguments passed to the application.</p>
    *
    * @return the array of arguments passed to the application
    */
   public final String[] getAllArguments() {
      return allArguments;
   }

   /**
    * Gets the set of dependent packages whose config needs to be loaded
    *
    * @return the dependent packages
    */
   public Set<String> getDependentPackages() {
      return Collections.emptySet();
   }

   /**
    * Gets the name of the application
    *
    * @return The name of the application
    */
   public final String getName() {
      return name;
   }

   /**
    * <p>Gets the command line arguments that were not specified as part of the command line parser</p>
    *
    * @return Arguments not specified in the command line parser
    */
   public final String[] getPositionalArgs() {
      return positionalArguments;
   }

   /**
    * <p>Runs the application by first parsing the command line arguments and initializing the config. The process then
    * runs the {@link #setup()} method to perform any special user-defined setup (e.g. gui initialization for JavaFX or
    * swing) and then finally runs the {@link #run()} command which performs the application logic and is specific to
    * each implementation.</p>
    *
    * @param args the command line arguments
    */
   public final void run(String[] args) {
      if(args == null) {
         args = new String[0];
      }

      this.allArguments = new String[args.length];
      System.arraycopy(args, 0, this.allArguments, 0, args.length);

      StringBuilder cliDesc = new StringBuilder();
      for(Description description : this.getClass().getAnnotationsByType(Description.class)) {
         cliDesc.append("\n").append(description.value());
      }

      CommandLineParser parser = new CommandLineParser(this, cliDesc.toString());
      this.positionalArguments = Config.initialize(getName(), args, parser);
      if(getDependentPackages().size() > 0) {
         for(String dependentPackage : getDependentPackages()) {
            Config.loadPackageConfig(dependentPackage);
         }
         Config.setAllCommandLine(parser);
      }
      try {
         setup();
      } catch(Exception e) {
         logSevere(LogUtils.getLogger(getClass()), e);
         System.exit(-1);
      }
      run();
   }

   /**
    * <p>Runs specialized code to setup the application before the {@link #run()} command is called.</p>
    *
    * @throws Exception Something went wrong during setup.
    */
   public abstract void setup() throws Exception;

   /**
    * Provides a helpful description about the purpose of the  application to display in the application's help
    */
   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Description {

      /**
       * Value string.
       *
       * @return the string
       */
      String value() default "";

   }//END OF Description

}//END OF Application
