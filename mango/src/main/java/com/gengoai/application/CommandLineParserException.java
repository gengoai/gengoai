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

/**
 * Exceptions occurring in the {@link CommandLineParser}
 * 
 * @author David B. Bracewell
 * 
 */
public class CommandLineParserException extends RuntimeException {

  private static final long serialVersionUID = -8601171308258509988L;

  /**
   * Constructs an exception where there was error parsing a command line option
   * 
   * @param key The argument
   * @param value its value
   */
  public CommandLineParserException(String key, String value) {
    super("Error parsing command line (" + key + ", " + value + ")");
  }

}// END OF CLASS CommandLineParserException