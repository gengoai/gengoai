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

import com.fasterxml.jackson.annotation.*;
import com.gengoai.Copyable;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.Val;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.JsonEntry;
import com.gengoai.json.TypedObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.gengoai.hermes.workflow.Workflow.*;

/**
 * Contexts are a specialized map that act as a shared memory for a Workflow. The context will retrieve values from its
 * internal storage and also fallback to checking for values in the global configuration.
 */
@EqualsAndHashCode(callSuper = false)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class Context implements Serializable, Copyable<Context> {
    @JsonValue
    private final Map<String, TypedObject<?>> properties = new HashMap<>();

    /**
     * Instantiates a new Context.
     */
    public Context() {

    }

    @JsonIgnore
    public Resource getWorkflowFolder() {
        return getAs(WORKFLOW_FOLDER, Resource.class);
    }

    @JsonIgnore
    public Resource getActionsFolder() {
        return getAs(ACTIONS_FOLDER, Resource.class);
    }

    @JsonIgnore
    public Resource getAnalysisFolder() {
        return getAs(ANALYSIS_FOLDER, Resource.class);
    }

    @JsonCreator
    protected Context(@JsonProperty JsonEntry entry) {
        entry.forEachProperty((key, value) -> property(key, value.as(TypedObject.class)));
    }

    /**
     * Instantiates a new Context
     *
     * @param properties the properties
     */
    @Builder
    public Context(@Singular @NonNull Map<String, ?> properties) {
        properties.forEach(this::property);
    }

    @Override
    public Context copy() {
        return Copyable.deepCopy(this);
    }

    /**
     * Gets the value for the given property name
     *
     * @param name the name
     * @return the value
     */
    public Object get(String name) {
        if (properties.get(name) != null) {
            return properties.get(name).getValue();
        }
        return null;
    }

    public void remove(String name) {
        properties.remove(name);
    }

    /**
     * Gets the value of the given property name as the given class type
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the value
     */
    public <T> T getAs(String name, @NonNull Class<T> clazz) {
        if (properties.containsKey(name)) {
            TypedObject<?> o = properties.get(name);
            if (o.getValue() instanceof Val) {
                return ((Val) o.getValue()).as(clazz);
            }
            return Cast.as(o.getValue(), clazz);
        }
        return Config.get(name).as(clazz);

    }

    /**
     * Gets the value of the given property name as the given type
     *
     * @param <T>  the type parameter
     * @param name the name
     * @param type the type
     * @return the value
     */
    public <T> T getAs(String name, @NonNull Type type) {
        if (properties.containsKey(name)) {
            return Converter.convertSilently(properties.get(name).getValue(), type);
        }
        return Config.get(name).as(type);

    }

    /**
     * Gets the value of the given property name as the given class type returning the default value if no value is
     * currently set.
     *
     * @param <T>          the type parameter
     * @param name         the name
     * @param clazz        the clazz
     * @param defaultValue the default value if none is set.
     * @return the value
     */
    public <T> T getAs(String name, @NonNull Class<T> clazz, T defaultValue) {
        if (properties.containsKey(name)) {
            return Cast.as(properties.get(name).getValue(), clazz);
        }
        return Config.get(name).as(clazz, defaultValue);
    }

    /**
     * Gets the value of the given property name as a double
     *
     * @param name the name
     * @return the value
     */
    public Double getDouble(String name) {
        return getAs(name, Double.class);
    }

    /**
     * Gets the value of the given property name as a double or the default value if not defined.
     *
     * @param name         the name
     * @param defaultValue the default value
     * @return the value
     */
    public Double getDouble(String name, double defaultValue) {
        return getAs(name, Double.class, defaultValue);
    }

    /**
     * Gets the value of the given property name as an integer
     *
     * @param name the name
     * @return the value
     */
    public Integer getInteger(String name) {
        return getAs(name, Integer.class);
    }

    /**
     * Gets the value of the given property name as an integer or the default value if not defined.
     *
     * @param name         the name
     * @param defaultValue the default value
     * @return the value
     */
    public Integer getInteger(String name, int defaultValue) {
        return getAs(name, Integer.class, defaultValue);
    }

    /**
     * Gets the value of the given property name as a String
     *
     * @param name the name
     * @return the value
     */
    public String getString(String name) {
        return getAs(name, String.class);
    }

    /**
     * Gets the value of the given property name as a String or the default value if not defined.
     *
     * @param name         the name
     * @param defaultValue the default value
     * @return the value
     */
    public String getString(String name, String defaultValue) {
        return getAs(name, String.class, defaultValue);
    }

    /**
     * Merges the given context with this one overwriting this context values with those in the given context.
     *
     * @param other the context to merge
     */
    public void merge(@NonNull Context other) {
        this.properties.putAll(other.properties);
    }

    /**
     * Defines a property in the context.
     *
     * @param name  the name
     * @param value the value
     */
    public void property(String name, Object value) {
        if (value == null) {
            this.properties.remove(name);
        } else if (value instanceof TypedObject) {
            this.properties.put(name, Cast.as(value));
        } else {
            this.properties.put(name, new TypedObject<>(value));
        }
    }

    @Override
    public String toString() {
        return properties.toString();
    }

}//END OF Context
