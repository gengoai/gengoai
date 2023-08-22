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

package com.gengoai.hermes.workflow;

import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class Actions {

    private static final Map<String, Class<? extends Action>> name2ActionClass = new HashMap<>();
    private static final Map<String, String> actionDefinitions = new HashMap<>();

    static {
        for (Action provider : ServiceLoader.load(Action.class)) {
            name2ActionClass.put(provider.getName().toUpperCase(), provider.getClass());
            actionDefinitions.put(provider.getName().toUpperCase(), provider.getDescription());
        }
    }


    private Actions() {
        throw new IllegalAccessError();
    }


    public static Action getAction(@NonNull String name) {
        try {
            return Reflect.onClass(name2ActionClass.get(name.toUpperCase())).create().get();
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getDefinition(@NonNull String name) {
        return actionDefinitions.get(name.toUpperCase());
    }


}//END OF CLASS Actions
