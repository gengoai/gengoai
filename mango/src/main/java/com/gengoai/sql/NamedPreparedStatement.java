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

import com.gengoai.collection.multimap.ArrayListMultimap;
import lombok.NonNull;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This class wraps a {@link PreparedStatement} allowing the parameters to be named. Named parameters are positioned
 * inside of [::], for example if the parameter was named 'id' it would be represented as [:id:]. This class has all the
 * same methods as a {@link PreparedStatement}, but also includes set methods where the name can be specified.
 * </p>
 *
 * @author David B. Bracewell
 */
public class NamedPreparedStatement implements PreparedStatement {
   private final static String namePatternString = "\\[:([\\w_]+):\\]";
   private final static Pattern namedItemPattern = Pattern.compile(namePatternString);
   private final ArrayListMultimap<String, Integer> nameIndexMap = new ArrayListMultimap<>();
   private PreparedStatement statement = null;

   public static String sanitize(@NonNull String str) {
      return str.replaceAll("\\W", "a");
   }

   /**
    * <p>
    * Creates a new {@link NamedPreparedStatement} using an existing connection and SQL string.
    * </p>
    *
    * @param connection The connection to create the statement from
    * @param sql        The SQL statement
    * @throws SQLException Something went wrong creating the statement
    */
   public NamedPreparedStatement(Connection connection, String sql)
         throws SQLException {
      init(connection, sql, false);
   }

   /**
    * <p>
    * Creates a new {@link NamedPreparedStatement} using an existing connection and SQL string.
    * </p>
    *
    * @param connection   The connection to create the statement from
    * @param sql          The SQL statement
    * @param generateKeys - True will allow the {@link #getGeneratedKeys()} method to return keys, false will not
    * @throws SQLException Something went wrong creating the statement
    */
   public NamedPreparedStatement(Connection connection, String sql,
                                 boolean generateKeys) throws SQLException {

      init(connection, sql, generateKeys);

   }

   @Override
   public void addBatch() throws SQLException {
      statement.addBatch();
   }

   @Override
   public void addBatch(String arg0) throws SQLException {
      statement.addBatch(arg0);
   }

   @Override
   public void cancel() throws SQLException {
      statement.cancel();
   }

   @Override
   public void clearBatch() throws SQLException {
      statement.clearBatch();
   }

   @Override
   public void clearParameters() throws SQLException {
      statement.clearParameters();
   }

   @Override
   public void clearWarnings() throws SQLException {
      statement.clearWarnings();
   }

   @Override
   public void close() throws SQLException {
      statement.close();
   }

   @Override
   public void closeOnCompletion() throws SQLException {
      statement.closeOnCompletion();
   }

   /**
    * <p>
    * Determines if a named parameter exists in this statement.
    * </p>
    *
    * @param parameterName The name of the parameter to check for
    * @return True - it exists, False - it does not exist
    */
   public boolean containsNamedParameter(String parameterName) {
      return nameIndexMap.containsKey(parameterName);
   }

   @Override
   public boolean execute() throws SQLException {
      return statement.execute();
   }

   @Override
   public boolean execute(String arg0) throws SQLException {
      return statement.execute(arg0);
   }

   @Override
   public boolean execute(String arg0, int arg1) throws SQLException {
      return statement.execute(arg0, arg1);
   }

   @Override
   public boolean execute(String arg0, int[] arg1) throws SQLException {
      return statement.execute(arg0, arg1);
   }

   @Override
   public boolean execute(String arg0, String[] arg1) throws SQLException {
      return statement.execute(arg0, arg1);
   }

   @Override
   public int[] executeBatch() throws SQLException {
      return statement.executeBatch();
   }

   @Override
   public ResultSet executeQuery() throws SQLException {
      return statement.executeQuery();
   }

   @Override
   public ResultSet executeQuery(String arg0) throws SQLException {
      return statement.executeQuery(arg0);
   }

   @Override
   public int executeUpdate() throws SQLException {
      return statement.executeUpdate();
   }

   @Override
   public int executeUpdate(String arg0) throws SQLException {
      return statement.executeUpdate(arg0);
   }

   @Override
   public int executeUpdate(String arg0, int arg1) throws SQLException {
      return statement.executeUpdate(arg0, arg1);
   }

   @Override
   public int executeUpdate(String arg0, int[] arg1) throws SQLException {
      return statement.executeUpdate(arg0, arg1);
   }

   @Override
   public int executeUpdate(String arg0, String[] arg1) throws SQLException {
      return statement.executeUpdate(arg0, arg1);
   }

   @Override
   public Connection getConnection() throws SQLException {
      return statement.getConnection();
   }

   @Override
   public int getFetchDirection() throws SQLException {
      return statement.getFetchDirection();
   }

   @Override
   public int getFetchSize() throws SQLException {
      return statement.getFetchSize();
   }

   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      return statement.getGeneratedKeys();
   }

   protected Collection<Integer> getLocationsForName(String name) {
      if(nameIndexMap.containsKey(name)) {
         Collection<Integer> rval = nameIndexMap.get(name);
         return rval.size() > 0
                ? rval
                : null;
      }

      return null;
   }

   @Override
   public int getMaxFieldSize() throws SQLException {
      return statement.getMaxFieldSize();
   }

   @Override
   public int getMaxRows() throws SQLException {
      return statement.getMaxRows();
   }

   @Override
   public ResultSetMetaData getMetaData() throws SQLException {
      return statement.getMetaData();
   }

   @Override
   public boolean getMoreResults() throws SQLException {
      return statement.getMoreResults();
   }

   @Override
   public boolean getMoreResults(int arg0) throws SQLException {
      return statement.getMoreResults(arg0);
   }

   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
      return statement.getParameterMetaData();
   }

   @Override
   public int getQueryTimeout() throws SQLException {
      return statement.getQueryTimeout();
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      return statement.getResultSet();
   }

   @Override
   public int getResultSetConcurrency() throws SQLException {
      return statement.getResultSetConcurrency();
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      return statement.getResultSetHoldability();
   }

   @Override
   public int getResultSetType() throws SQLException {
      return statement.getResultSetType();
   }

   @Override
   public int getUpdateCount() throws SQLException {
      return statement.getUpdateCount();
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      return statement.getWarnings();
   }

   protected void init(Connection connection, String sql, boolean generateKeys)
         throws SQLException {
      Matcher nameMatcher = namedItemPattern.matcher(sql);
      int idx = 1;

      while(nameMatcher.find()) {
         String name = nameMatcher.group(1);
         nameIndexMap.put(name, idx);
         idx++;
      }

      sql = sql.replaceAll(namePatternString, "?");

      statement = connection.prepareStatement(sql,
                                              generateKeys
                                              ? Statement.RETURN_GENERATED_KEYS
                                              : Statement.NO_GENERATED_KEYS);
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException {
      return statement.isCloseOnCompletion();
   }

   @Override
   public boolean isClosed() throws SQLException {
      return statement.isClosed();
   }

   @Override
   public boolean isPoolable() throws SQLException {
      return statement.isPoolable();
   }

   @Override
   public boolean isWrapperFor(Class<?> arg0) throws SQLException {
      return statement.isWrapperFor(arg0);
   }

   @Override
   public void setArray(int arg0, Array arg1) throws SQLException {
      statement.setArray(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given Array object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 an Array object that maps an SQL ARRAY value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setArray(String arg0, Array arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setArray(i, arg1);
      }
   }

   @Override
   public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
      statement.setAsciiStream(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given input stream, which will have the specified number of bytes.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the Java input stream that contains the ASCII parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setAsciiStream(String arg0, InputStream arg1)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setAsciiStream(i, arg1);
      }
   }

   @Override
   public void setAsciiStream(int arg0, InputStream arg1, int arg2)
         throws SQLException {
      statement.setAsciiStream(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given input stream, which will have the specified number of bytes.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the Java input stream that contains the ASCII parameter value
    * @param arg2 the number of bytes in the stream
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setAsciiStream(String arg0, InputStream arg1, int arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setAsciiStream(i, arg1, arg2);
      }
   }

   @Override
   public void setAsciiStream(int arg0, InputStream arg1, long arg2)
         throws SQLException {
      statement.setAsciiStream(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given input stream, which will have the specified number of bytes.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the Java input stream that contains the ASCII parameter value
    * @param arg2 the number of bytes in the stream
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setAsciiStream(String arg0, InputStream arg1, long arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setAsciiStream(i, arg1, arg2);
      }
   }

   @Override
   public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
      statement.setBigDecimal(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given java.math.BigDecimal value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBigDecimal(i, arg1);
      }
   }

   @Override
   public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
      statement.setBinaryStream(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given input stream.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the Java input stream that contains the ASCII parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBinaryStream(String arg0, InputStream arg1)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBinaryStream(i, arg1);
      }
   }

   @Override
   public void setBinaryStream(int arg0, InputStream arg1, int arg2)
         throws SQLException {
      statement.setBinaryStream(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given input stream, which will have the specified number of bytes.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the Java input stream that contains the ASCII parameter value
    * @param arg2 the number of bytes in the stream
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBinaryStream(String arg0, InputStream arg1, int arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBinaryStream(i, arg1, arg2);
      }
   }

   @Override
   public void setBinaryStream(int arg0, InputStream arg1, long arg2)
         throws SQLException {
      statement.setBinaryStream(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given input stream, which will have the specified number of bytes.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the Java input stream that contains the ASCII parameter value
    * @param arg2 the number of bytes in the stream
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBinaryStream(String arg0, InputStream arg1, long arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBinaryStream(i, arg1, arg2);
      }
   }

   /**
    * Sets the designated parameter to the given java.sql.Blob object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBlob(String arg0, Blob arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBlob(i, arg1);
      }
   }

   @Override
   public void setBlob(int arg0, Blob arg1) throws SQLException {
      statement.setBlob(arg0, arg1);
   }

   /**
    * Sets the designated parameter to a InputStream object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBlob(String arg0, InputStream arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBlob(i, arg1);
      }
   }

   @Override
   public void setBlob(int arg0, InputStream arg1) throws SQLException {
      statement.setBlob(arg0, arg1);
   }

   /**
    * Sets the designated parameter to a InputStream object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 the length in bytes
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBlob(String arg0, InputStream arg1, long arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBlob(i, arg1, arg2);
      }
   }

   @Override
   public void setBlob(int arg0, InputStream arg1, long arg2)
         throws SQLException {
      statement.setBlob(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given boolean value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBoolean(String arg0, boolean arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBoolean(i, arg1);
      }
   }

   @Override
   public void setBoolean(int arg0, boolean arg1) throws SQLException {
      statement.setBoolean(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given byte value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setByte(String arg0, byte arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setByte(i, arg1);
      }
   }

   @Override
   public void setByte(int arg0, byte arg1) throws SQLException {
      statement.setByte(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given Java array of bytes.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setBytes(String arg0, byte[] arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setBytes(i, arg1);
      }
   }

   @Override
   public void setBytes(int arg0, byte[] arg1) throws SQLException {
      statement.setBytes(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setCharacterStream(String arg0, Reader arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setCharacterStream(i, arg1);
      }
   }

   @Override
   public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
      statement.setCharacterStream(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 the length in bytes
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setCharacterStream(String arg0, Reader arg1, int arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setCharacterStream(i, arg1, arg2);
      }
   }

   @Override
   public void setCharacterStream(int arg0, Reader arg1, int arg2)
         throws SQLException {
      statement.setCharacterStream(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 the length in bytes
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setCharacterStream(String arg0, Reader arg1, long arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setCharacterStream(i, arg1, arg2);
      }
   }

   @Override
   public void setCharacterStream(int arg0, Reader arg1, long arg2)
         throws SQLException {
      statement.setCharacterStream(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to a java.sql.Clob object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setClob(String arg0, Clob arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setClob(i, arg1);
      }
   }

   @Override
   public void setClob(int arg0, Clob arg1) throws SQLException {
      statement.setClob(arg0, arg1);
   }

   /**
    * Sets the designated parameter to a java.sql.Clob object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setClob(String arg0, Reader arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setClob(i, arg1);
      }
   }

   @Override
   public void setClob(int arg0, Reader arg1) throws SQLException {
      statement.setClob(arg0, arg1);
   }

   /**
    * Sets the designated parameter to a java.sql.Clob object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 the length in bytes
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setClob(String arg0, Reader arg1, long arg2) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setClob(i, arg1, arg2);
      }
   }

   @Override
   public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
      statement.setClob(arg0, arg1, arg2);
   }

   @Override
   public void setCursorName(String arg0) throws SQLException {
      statement.setCursorName(arg0);
   }

   /**
    * Sets the designated parameter to the given java.sql.Date value using the default time zone of the virtual machine
    * that is running the application.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setDate(String arg0, Date arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setDate(i, arg1);
      }
   }

   @Override
   public void setDate(int arg0, Date arg1) throws SQLException {
      statement.setDate(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given java.sql.Date value, using the given Calendar object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 Calendar for determining timezone
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setDate(String arg0, Date arg1, Calendar arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setDate(i, arg1, arg2);
      }
   }

   @Override
   public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
      statement.setDate(arg0, arg1, arg2);
   }

   /**
    * Sets the designated parameter to the given Java double value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setDouble(String arg0, double arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setDouble(i, arg1);
      }
   }

   @Override
   public void setDouble(int arg0, double arg1) throws SQLException {
      statement.setDouble(arg0, arg1);
   }

   @Override
   public void setEscapeProcessing(boolean arg0) throws SQLException {
      statement.setEscapeProcessing(arg0);
   }

   @Override
   public void setFetchDirection(int arg0) throws SQLException {
      statement.setFetchDirection(arg0);
   }

   @Override
   public void setFetchSize(int arg0) throws SQLException {
      statement.setFetchSize(arg0);
   }

   /**
    * Sets the designated parameter to the given Java float value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setFloat(String arg0, float arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setFloat(i, arg1);
      }
   }

   @Override
   public void setFloat(int arg0, float arg1) throws SQLException {
      statement.setFloat(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given Java int value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setInt(String arg0, int arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setInt(i, arg1);
      }
   }

   @Override
   public void setInt(int arg0, int arg1) throws SQLException {
      statement.setInt(arg0, arg1);
   }

   /**
    * Sets the designated parameter to the given Java long value.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setLong(String arg0, long arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setLong(i, arg1);
      }
   }

   @Override
   public void setLong(int arg0, long arg1) throws SQLException {
      statement.setLong(arg0, arg1);
   }

   @Override
   public void setMaxFieldSize(int arg0) throws SQLException {
      statement.setMaxFieldSize(arg0);
   }

   @Override
   public void setMaxRows(int arg0) throws SQLException {
      statement.setMaxRows(arg0);
   }

   /**
    * Sets the designated parameter to a Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNCharacterStream(String arg0, Reader arg1)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNCharacterStream(i, arg1);
      }
   }

   @Override
   public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
      statement.setNCharacterStream(arg0, arg1);

   }

   /**
    * Sets the designated parameter to a Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 the number of characters in the parameter data.
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNCharacterStream(String arg0, Reader arg1, long arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNCharacterStream(i, arg1, arg2);
      }
   }

   @Override
   public void setNCharacterStream(int arg0, Reader arg1, long arg2)
         throws SQLException {
      statement.setNCharacterStream(arg0, arg1, arg2);

   }

   /**
    * Sets the designated parameter to a NClob object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNClob(String arg0, NClob arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNClob(i, arg1);
      }
   }

   @Override
   public void setNClob(int arg0, NClob arg1) throws SQLException {
      statement.setNClob(arg0, arg1);

   }

   /**
    * Sets the designated parameter to a Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNClob(String arg0, Reader arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNClob(i, arg1);
      }
   }

   @Override
   public void setNClob(int arg0, Reader arg1) throws SQLException {
      statement.setNClob(arg0, arg1);

   }

   /**
    * Sets the designated parameter to a Reader object.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @param arg2 the number of characters in the parameter data.
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNClob(String arg0, Reader arg1, long arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNClob(i, arg1, arg2);
      }
   }

   @Override
   public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
      statement.setNClob(arg0, arg1, arg2);

   }

   /**
    * Sets the designated parameter to the given String object. The driver converts this to a SQL NCHAR or NVARCHAR or
    * LONGNVARCHAR value (depending on the argument's size relative to the driver's limits on NVARCHAR values) when it
    * sends it to the database.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the parameter value
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNString(String arg0, String arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNString(i, arg1);
      }
   }

   @Override
   public void setNString(int arg0, String arg1) throws SQLException {
      statement.setNString(arg0, arg1);

   }

   /**
    * Sets the designated parameter to SQL NULL
    *
    * @param arg0 The name of the parameter
    * @param arg1 the SQL type code defined in java.sql.Types
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNull(String arg0, int arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNull(i, arg1);
      }
   }

   @Override
   public void setNull(int arg0, int arg1) throws SQLException {
      statement.setNull(arg0, arg1);
   }

   /**
    * Sets the designated parameter to SQL NULL. This version of the method setNull should be used for user-defined
    * types and REF type parameters. Examples of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and named
    * array types.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the SQL type code defined in java.sql.Types
    * @param arg2 the fully-qualified name of an SQL user-defined type; ignored if the parameter is not a user-defined
    *             type or REF
    * @throws SQLException if a database access error occurs or the name does not exist
    */
   public void setNull(String arg0, int arg1, String arg2) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setNull(i, arg1, arg2);
      }
   }

   @Override
   public void setNull(int arg0, int arg1, String arg2) throws SQLException {
      statement.setNull(arg0, arg1, arg2);
   }

   /**
    * ets the value of the designated parameter using the given object. The second parameter must be of type Object;
    * therefore, the java.lang equivalent objects should be used for built-in types. The JDBC specification specifies a
    * standard mapping from Java Object types to SQL types. The given argument will be converted to the corresponding
    * SQL type before being sent to the database. Note that this method may be used to pass datatabase- specific
    * abstract data types, by using a driver-specific Java type. If the object is of a class implementing the interface
    * SQLData, the JDBC driver should call the method SQLData.writeSQL to write it to the SQL data stream. If, on the
    * other hand, the object is of a class implementing Ref, Blob, Clob, NClob, Struct, java.net.URL, RowId, SQLXML or
    * Array, the driver should pass it to the database as a value of the corresponding SQL type.
    *
    * @param arg0 The name of the parameter
    * @param arg1 the object containing the input parameter value
    * @throws SQLException
    */
   public void setObject(String arg0, Object arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setObject(i, arg1);
      }
   }

   @Override
   public void setObject(int arg0, Object arg1) throws SQLException {
      statement.setObject(arg0, arg1);
   }

   /**
    * @see #setObject(int, Object, int)
    */
   public void setObject(String arg0, Object arg1, int arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setObject(i, arg1, arg2);
      }
   }

   @Override
   public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
      statement.setObject(arg0, arg1, arg2);
   }

   /**
    * @see #setObject(int, Object, int, int)
    */
   public void setObject(String arg0, Object arg1, int arg2, int arg3)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setObject(i, arg1, arg2, arg3);
      }
   }

   @Override
   public void setObject(int arg0, Object arg1, int arg2, int arg3)
         throws SQLException {
      statement.setObject(arg0, arg1, arg2, arg3);
   }

   @Override
   public void setPoolable(boolean arg0) throws SQLException {
      statement.setPoolable(arg0);
   }

   @Override
   public void setQueryTimeout(int arg0) throws SQLException {
      statement.setQueryTimeout(arg0);
   }

   /**
    * @see #setRef(int, Ref)
    */
   public void setRef(String arg0, Ref arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setRef(i, arg1);
      }
   }

   @Override
   public void setRef(int arg0, Ref arg1) throws SQLException {
      statement.setRef(arg0, arg1);
   }

   /**
    * @see #setRowId(int, RowId)
    */
   public void setRowId(String arg0, RowId arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setRowId(i, arg1);
      }
   }

   @Override
   public void setRowId(int arg0, RowId arg1) throws SQLException {
      statement.setRowId(arg0, arg1);
   }

   /**
    * @see #setSQLXML(int, SQLXML)
    */
   public void setSQLXML(String arg0, SQLXML arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setSQLXML(i, arg1);
      }
   }

   @Override
   public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
      statement.setSQLXML(arg0, arg1);
   }

   @Override
   public void setShort(int arg0, short arg1) throws SQLException {
      statement.setShort(arg0, arg1);
   }

   /**
    * @see #setShort(int, short)
    */
   public void setShort(String arg0, short arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setShort(i, arg1);
      }
   }

   @Override
   public void setString(int arg0, String arg1) throws SQLException {
      statement.setString(arg0, arg1);
   }

   /**
    * @see #setString(int, String)
    */
   public void setString(String arg0, String arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setString(i, arg1);
      }
   }

   @Override
   public void setTime(int arg0, Time arg1) throws SQLException {
      statement.setTime(arg0, arg1);
   }

   /**
    * @see #setTime(int, Time)
    */
   public void setTime(String arg0, Time arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setTime(i, arg1);
      }
   }

   @Override
   public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
      statement.setTime(arg0, arg1, arg2);

   }

   /**
    * @see #setTime(int, Time, Calendar)
    */
   public void setTime(String arg0, Time arg1, Calendar arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setTime(i, arg1, arg2);
      }
   }

   @Override
   public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
      statement.setTimestamp(arg0, arg1);
   }

   /**
    * @see #setTimestamp(int, Timestamp)
    */
   public void setTimestamp(String arg0, Timestamp arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setTimestamp(i, arg1);
      }
   }

   @Override
   public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2)
         throws SQLException {
      statement.setTimestamp(arg0, arg1, arg2);
   }

   /**
    * @see #setTimestamp(int, Timestamp, Calendar)
    */
   public void setTimestamp(String arg0, Timestamp arg1, Calendar arg2)
         throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setTimestamp(i, arg1, arg2);
      }
   }

   @Override
   public void setURL(int arg0, URL arg1) throws SQLException {
      statement.setURL(arg0, arg1);
   }

   /**
    * @see #setURL(int, URL)
    */
   public void setURL(String arg0, URL arg1) throws SQLException {
      Collection<Integer> locs = getLocationsForName(arg0);
      if(locs == null) {
         throw new SQLException(arg0 + " is not a named query item");
      }
      for(int i : locs) {
         statement.setURL(i, arg1);
      }
   }

   @Override
   @Deprecated
   public void setUnicodeStream(int arg0, InputStream arg1, int arg2)
         throws SQLException {
      statement.setUnicodeStream(arg0, arg1, arg2);
   }

   @Override
   public <T> T unwrap(Class<T> arg0) throws SQLException {
      return statement.unwrap(arg0);
   }

}// END CLASS NamedPreparedStatement
