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

package com.gengoai.sql.statement;

import com.gengoai.Copyable;
import com.gengoai.conversion.Converter;
import com.gengoai.sql.*;
import com.gengoai.sql.operator.QueryOperator;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * SQL Statement that performs a query over a database.
 */
public abstract class QueryStatement implements SQLElement, Copyable<QueryStatement> {
    private static final long serialVersionUID = 1L;

    @Override
    public QueryStatement copy() {
        return Copyable.deepCopy(this);
    }

    /**
     * Combines this query with the given second query using an EXCEPT operator
     *
     * @param query2 the second query.
     * @return the combined SQLQueryStatement
     */
    public QueryStatement except(@NonNull QueryStatement query2) {
        return new QueryOperator(SQLConstants.EXCEPT, this, query2);
    }

    /**
     * Runs the query returning true if there is at least on result.
     *
     * @param context the context to perform the query over
     * @return True if there is at least one result
     * @throws SQLException something happened trying to query
     */
    public boolean exists(@NonNull SQLContext context) throws SQLException {
        try (Statement statement = context.getConnection().createStatement()) {
            try (ResultSet rs = statement.executeQuery(context.render(this))) {
                return rs.next();
            }
        }
    }

    /**
     * Combines this query with the given second query using an INTERSECT operator
     *
     * @param query2 the second query.
     * @return the combined SQLQueryStatement
     */
    public QueryStatement intersect(@NonNull QueryStatement query2) {
        return new QueryOperator(SQLConstants.INTERSECT, this, query2);
    }

    /**
     * Performs the query specified in this statement using the given {@link SQLContext} returning the results as a
     * {@link ResultSetIterator} using the given {@link ResultSetMapper} to map ResultSet to objects
     *
     * @param <T>             the Iterator type parameter
     * @param context         the context to perform the query over
     * @param resultSetMapper the mapper from ResultSet to Object
     * @return the ResultSetIterator over the results of the given {@link QueryStatement}
     * @throws SQLException something happened trying to query
     */
    public <T> Stream<T> query(@NonNull SQLContext context,
                               @NonNull ResultSetMapper<? extends T> resultSetMapper) throws SQLException {
        return Streams.asStream(new ResultSetIterator<>(resultSetMapper,
                                                        context.getConnection().createStatement()
                                                               .executeQuery(context.getDialect().render(this))));
    }

    /**
     * Performs the query specified in this statement using the given {@link SQLContext} filling in indexed value
     * placeholders using the given List of values and returning the results as a {@link ResultSetIterator}* using the
     * given {@link ResultSetMapper} to map ResultSet to objects
     *
     * @param <T>             the Iterator type parameter
     * @param context         the context to perform the query over
     * @param values          the values to use to fill the query statement (by Index)
     * @param resultSetMapper the mapper from ResultSet to Object
     * @return the ResultSetIterator over the results of the given {@link QueryStatement}
     * @throws SQLException something happened trying to query
     */
    public <T> Stream<T> query(@NonNull SQLContext context,
                               @NonNull List<?> values,
                               @NonNull ResultSetMapper<? extends T> resultSetMapper) throws SQLException {
        PreparedStatement preparedStatement = context.getConnection().prepareStatement(context.getDialect().render(this));
        for (int i = 0; i < values.size(); i++) {
            preparedStatement.setObject(i + 1, values.get(i));
        }
        return Streams.asStream(new ResultSetIterator<>(resultSetMapper, preparedStatement.executeQuery()));
    }

    /**
     * Performs the query specified in this statement using the given {@link SQLContext}  filling in named value
     * placeholders using the given Map of values and returning the results as a {@link ResultSetIterator} using the
     * given {@link ResultSetMapper} to map ResultSet to objects
     *
     * @param <T>             the Iterator type parameter
     * @param context         the context to perform the query over
     * @param values          the values to use to fill the query statement (by Name)
     * @param resultSetMapper the mapper from ResultSet to Object
     * @return the ResultSetIterator over the results of the given {@link QueryStatement}
     * @throws SQLException something happened trying to query
     */
    public <T> Stream<T> query(@NonNull SQLContext context,
                               @NonNull Map<String, ?> values,
                               @NonNull ResultSetMapper<? extends T> resultSetMapper) throws SQLException {
        NamedPreparedStatement preparedStatement = new NamedPreparedStatement(context.getConnection(),
                                                                              context.getDialect().render(this));
        for (Map.Entry<String, ?> e : values.entrySet()) {
            preparedStatement.setObject(NamedPreparedStatement.sanitize(e.getKey()), e.getValue());
        }
        return Streams.asStream(new ResultSetIterator<>(resultSetMapper, preparedStatement.executeQuery()));
    }

    /**
     * Runs the query on the given context returning a ResultSet
     *
     * @param context the context to perform the query over
     * @return the ResultSet
     * @throws SQLException Something went wrong querying
     */
    public final ResultSet query(@NonNull SQLContext context) throws SQLException {
        return context.getConnection().createStatement().executeQuery(context.getDialect().render(this));
    }

    /**
     * Performs this query on the given {@link SQLContext} which expects a single value for a single column to return.
     * The resultant value is converted into the given class type for return. If there is no value or the value cannot be
     * converted a <code>null</code> value is returned.
     *
     * @param <T>     the type parameter
     * @param context the context to perform the query over
     * @param tClass  the class type desired for the scalar object
     * @return the scalar result
     * @throws SQLException something happened trying to query
     */
    public <T> T queryScalar(@NonNull SQLContext context,
                             @NonNull Class<T> tClass) throws SQLException {
        try (Statement statement = context.getConnection().createStatement()) {
            try (ResultSet rs = statement.executeQuery(context.getDialect().render(this))) {
                if (rs.next()) {
                    return Converter.convertSilently(rs.getObject(1), tClass);
                }
                return null;
            }
        }
    }

    /**
     * Performs this query on the given {@link SQLContext} which expects a single value for a single column to return.
     * The resultant value is converted into a Double return. If there is no value or the value cannot be converted a
     * <code>null</code> value is returned.
     * <p>
     *
     * @param context the context to perform the query over
     * @return the scalar result
     * @throws SQLException something happened trying to query
     */
    public Double queryScalarDouble(@NonNull SQLContext context) throws SQLException {
        return queryScalar(context, Double.class);
    }

    /**
     * Performs this query on the given {@link SQLContext} which expects a single value for a single column to return.
     * The resultant value is converted into a Long return. If there is no value or the value cannot be converted a
     * <code>null</code> value is returned.
     *
     * @param context the context to perform the query over
     * @return the scalar result
     * @throws SQLException something happened trying to query
     */
    public Long queryScalarLong(@NonNull SQLContext context) throws SQLException {
        return queryScalar(context, Long.class);
    }

    /**
     * Performs this query on the given {@link SQLContext} which expects a single value for a single column to return.
     * The resultant value is converted into a String return. If there is no value or the value cannot be converted a
     * <code>null</code> value is returned.
     *
     * @param context the context to perform the query over
     * @return the scalar result
     * @throws SQLException something happened trying to query
     */
    public String queryScalarString(@NonNull SQLContext context) throws SQLException {
        return queryScalar(context, String.class);
    }

    /**
     * Combines this query with the given second query using an UNION operator
     *
     * @param query2 the second query.
     * @return the combined SQLQueryStatement
     */
    public QueryStatement union(@NonNull QueryStatement query2) {
        return new QueryOperator(SQLConstants.UNION, this, query2);
    }

    /**
     * Combines this query with the given second query using an UNION ALL operator
     *
     * @param query2 the second query.
     * @return the combined SQLQueryStatement
     */
    public QueryStatement unionAll(@NonNull QueryStatement query2) {
        return new QueryOperator(SQLConstants.UNION_ALL, this, query2);
    }

}//END OF QueryStatement
