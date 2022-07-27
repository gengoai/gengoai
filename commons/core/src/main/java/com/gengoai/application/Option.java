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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Field annotation that allows the field to be set via the command line using a {@link CommandLineParser}.
 * </p>
 *
 * @author David B. Bracewell
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

   /**
    * @return The name of the command line option
    */
   String name() default "";

   /**
    * @return The description for the command line option
    */
   String description();

   /**
    * @return The aliases for the command line option
    */
   String[] aliases() default {};

   /**
    * @return The default value for the command line option
    */
   String defaultValue() default "";

   /**
    * @return True if the option is optional (This only needs to be set if the specification is not given)
    */
   boolean required() default false;

}//END OF Option
