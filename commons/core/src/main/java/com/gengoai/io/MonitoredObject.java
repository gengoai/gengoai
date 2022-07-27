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
 *
 */

package com.gengoai.io;

import java.io.Serializable;
import java.util.function.Consumer;

import static com.gengoai.Validation.checkArgument;

/**
 * <p>A wrapper around an Object whose resources are being monitored by {@link ResourceMonitor}</p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public class MonitoredObject<T> implements Serializable {
   private static final long serialVersionUID = 1L;
   public final T object;

   protected MonitoredObject(T object) {
      checkArgument(object instanceof AutoCloseable, "Object must be AutoCloseable");
      this.object = ResourceMonitor.MONITOR.addResource(this, object);
   }

   protected MonitoredObject(T object, Consumer<T> onClose) {
      this.object = ResourceMonitor.MONITOR.addResource(this, object, onClose);
   }

}//END OF MonitoredObject
