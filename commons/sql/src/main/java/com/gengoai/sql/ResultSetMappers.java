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

package com.gengoai.sql;

import com.gengoai.conversion.Cast;
import com.gengoai.reflection.BeanMap;
import com.gengoai.reflection.Reflect;
import lombok.NonNull;
import lombok.Value;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Set of commonly used {@link ResultSetMapper}</p>
 */
public final class ResultSetMappers {

    /**
     * A {@link ResultSetMapper} that will return a Map where the key is the column name and the value is the result of
     * calling <code>getObject</code>.
     */
    public static final ResultSetMapper<Map<String, Object>> MAP_RESULT_SET_MAPPER = resultSet -> {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            m.put(resultSet.getMetaData().getColumnName(i + 1),
                  resultSet.getObject(i + 1));
        }
        return m;
    };

    /**
     * Creates a {@link ResultSetMapper} that will return objects of the given type using a {@link BeanMap}. Objects must
     * have a no-argument constructor and the column names must align with the object's properties.
     *
     * @param <T>    the type parameter
     * @param tClass the class of the object to create
     * @return the ResultSetMapper
     */
    public static <T> ResultSetMapper<T> beanMapper(@NonNull Class<T> tClass) {
        return new BeanMapper<>(tClass);
    }

    private ResultSetMappers() {
        throw new IllegalAccessError();
    }

    @Value
    private static class BeanMapper<T> implements ResultSetMapper<T> {
        private static final long serialVersionUID = 1L;
        @NonNull Class<T> tClass;

        @Override
        public T map(@NonNull ResultSet resultSet) throws Exception {
            BeanMap beanMap = new BeanMap(Reflect.onClass(tClass).create().get());
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                beanMap.put(resultSet.getMetaData().getColumnName(i + 1),
                            resultSet.getObject(i + 1));
            }
            return Cast.as(beanMap.getBean());
        }
    }

}//END OF ResultSetMappers
