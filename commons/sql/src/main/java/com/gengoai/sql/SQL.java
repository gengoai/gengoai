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

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.constraint.ConstraintBuilder;
import com.gengoai.sql.object.Column;
import com.gengoai.sql.object.Table;
import com.gengoai.sql.operator.PrefixUnaryOperator;
import com.gengoai.sql.operator.SQLOperable;
import com.gengoai.sql.statement.QueryStatement;
import com.gengoai.sql.statement.UpdateStatement;
import com.gengoai.string.Strings;
import lombok.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>DSL for using Mango SQL</p>
 */
public final class SQL {
   /**
    * SQLElement representing "*" for selecting ALL
    */
   public static final SQLElement ALL = sql("*");
   /**
    * SQLElement representing "?" of use with PreparedStatements
    */
   public static final SQLElement INDEXED_ARGUMENT = sql("?");

   private SQL() {
      throw new IllegalAccessError();
   }

   /**
    * An {@link SQLOperable} defining a column name.
    *
    * @param column the column name
    * @return the SQLOperable
    */
   public static SQLOperable C(String column) {
      return C(Strings.EMPTY, column);
   }

   /**
    * Static method for constructing a new Column given its name and type.
    *
    * @param name the name of the column
    * @param type the data type of the column
    * @return the column
    */
   public static Column column(String name, String type) {
      return new Column(name, type);
   }


   public static Table table(String name,
                             @NonNull Consumer<TableDef> definition) {
      return table(name, null, definition);
   }

   public static Table table(String name,
                             SQLElement type,
                             @NonNull Consumer<TableDef> definition) {
      TableDef builder = new TableDef();
      definition.accept(builder);
      return new Table(name, type, null, builder.columns, builder.constraints);
   }

   /**
    * An {@link SQLOperable} defining a fully qualified column name.
    *
    * @param table  the table name
    * @param column the column name
    * @return the SQLOperable
    */
   public static SQLOperable C(String table, String column) {
      Validation.notNullOrBlank(column);
      column = column.strip();
      column = Strings.prependIfNotPresent(Strings.appendIfNotPresent(SQLDialect.escape(column.strip()), "\""), "\"");
      if (Strings.isNullOrBlank(table)) {
         return new SimpleExpression(column);
      }
      return new SimpleExpression(table + "." + column);
   }

   public static SQLOperable C(@NonNull Table table, String column) {
      return C(table.getName(), column);
   }


   /**
    * An {@link SQLOperable} defining a literal expression, which will automatically be placed in single quotes.
    *
    * @param value the literal value
    * @return the SQLOperable
    */
   public static SQLOperable L(@NonNull String value) {
      return new Literal(value);
   }

   /**
    * Generates an {@link SQLOperable} for a numeric value
    *
    * @param value the numeric value
    * @return the SQLOperable
    */
   public static SQLOperable N(@NonNull Number value) {
      return new Numeric(value);
   }


   /**
    * Combines one or more SQLElements via an <code>AND</code>
    *
    * @param elements the elements to AND
    * @return the SQLOperable
    */
   public static SQLOperable and(@NonNull SQLElement... elements) {
      return new Grouped(new ComplexExpression(Arrays.asList(elements), " AND "));
   }

   /**
    * Creates an "exists" cause checking for the existence of a record in a sub query.
    *
    * @param query the sub query
    * @return the SQLOperable
    */
   public static SQLOperable exists(@NonNull QueryStatement query) {
      return new PrefixUnaryOperator(SQLConstants.EXISTS, query, true);
   }

   /**
    * Groups one or more expressions by placing them in parenthesis
    *
    * @param elements the elements to group
    * @return the SQLOperable
    */
   public static SQLOperable group(@NonNull SQLElement... elements) {
      if (elements.length == 1) {
         return new Grouped(elements[0]);
      }
      return new Grouped(new ComplexExpression(Arrays.asList(elements)));
   }

   /**
    * Generates a named argument for use with {@link NamedPreparedStatement}
    *
    * @param name the name of the argument
    * @return the SQLOperable
    */
   public static SQLOperable namedArgument(@NonNull String name) {
      return new NamedArgument(sql(name));
   }

   /**
    * Named argument sql element.
    *
    * @param name the name
    * @return the sql element
    */
   public static SQLElement namedArgument(@NonNull SQLElement name) {
      return new NamedArgument(name);
   }

   /**
    * Generates a null value
    *
    * @return the SQLElement
    */
   public static SQLElement nullValue() {
      return new NULL();
   }

   /**
    * Combines one or more SQLElements via an <code>OR</code>
    *
    * @param elements the elements to OR
    * @return the SQLOperable
    */
   public static SQLOperable or(@NonNull SQLElement... elements) {
      return new Grouped(new ComplexExpression(Arrays.asList(elements), " OR "));
   }

   /**
    * Free form SQL query statement
    *
    * @param statement the statement
    * @return the SQLQueryStatement
    */
   public static QueryStatement query(String statement) {
      return new SQLQueryStatementImpl(Validation.notNullOrBlank(statement));
   }

   /**
    * Free form SQL from one or more sql statements
    *
    * @param components the components
    * @return the sql element
    */
   public static SQLElement sql(@NonNull String... components) {
      if (components.length == 1) {
         return new SimpleExpression(components[0]);
      }
      return new ComplexExpression(Stream.of(components).map(SimpleExpression::new).collect(Collectors.toList()));
   }

   /**
    * Free form SQL update statement
    *
    * @param statement the statement
    * @return the SQLUpdateStatement
    */
   public static UpdateStatement update(String statement) {
      return new UpdateStatementImpl(Validation.notNullOrBlank(statement));
   }

   public static boolean columnNamesEqual(String c1, String c2) {
      if (c1 == null || c2 == null) {
         return false;
      }
      c1 = Strings.prependIfNotPresent(Strings.appendIfNotPresent(c1, "\""), "\"");
      c2 = Strings.prependIfNotPresent(Strings.appendIfNotPresent(c2, "\""), "\"");
      return c1.equalsIgnoreCase(c2);
   }

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   @AllArgsConstructor
   private static class Literal implements PreRenderedSQL {
      private static final long serialVersionUID = 1L;
      @NonNull String value;

      @Override
      public String toString() {
         return Strings.appendIfNotPresent(Strings.prependIfNotPresent(SQLDialect.escape(value), "'"), "'");
      }

   }//END OF Literal

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   @AllArgsConstructor
   private static class Numeric implements PreRenderedSQL {
      private static final long serialVersionUID = 1L;
      @NonNull Number value;

      @Override
      public String toString() {
         return value.toString();
      }

   }//END OF Numeric

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   private static class SimpleExpression implements PreRenderedSQL {
      String expression;

      /**
       * Instantiates a new Simple expression.
       *
       * @param expression the expression
       */
      public SimpleExpression(String expression) {
         this.expression = Validation.notNullOrBlank(expression);
      }

      @Override
      public String toString() {
         return expression;
      }

   }//END OF SimpleExpression

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   private static class UpdateStatementImpl implements UpdateStatement, PreRenderedSQL {
      String expression;

      /**
       * Instantiates a new Sql update statement.
       *
       * @param expression the expression
       */
      public UpdateStatementImpl(String expression) {
         this.expression = Validation.notNullOrBlank(expression);
      }

      @Override
      public String toString() {
         return expression;
      }

   }//END OF SQLUpdateStatementImpl

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   @EqualsAndHashCode(callSuper = true)
   private static class SQLQueryStatementImpl extends QueryStatement implements PreRenderedSQL {
      String expression;

      /**
       * Instantiates a new Sql query statement.
       *
       * @param expression the expression
       */
      public SQLQueryStatementImpl(String expression) {
         this.expression = Validation.notNullOrBlank(expression);
      }

      @Override
      public String toString() {
         return expression;
      }

   }//END OF SQLQueryStatementImpl

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   @AllArgsConstructor(access = AccessLevel.PUBLIC)
   private static class Grouped implements CompositeSQLElement {
      @NonNull SQLElement expression;

      @Override
      public String render(@NonNull SQLDialect dialect) {
         return "( " + dialect.render(expression) + " )";
      }
   }//END OF SQLUpdateStatementImpl

   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   private static class ComplexExpression implements CompositeSQLElement {
      private static final long serialVersionUID = 1L;
      @NonNull Collection<? extends SQLElement> values;
      String delimiter;

      /**
       * Instantiates a new Complex expression.
       *
       * @param values    the values
       * @param delimiter the delimiter
       */
      public ComplexExpression(@NonNull Collection<? extends SQLElement> values, String delimiter) {
         this.values = values;
         this.delimiter = delimiter;
      }

      /**
       * Instantiates a new Complex expression.
       *
       * @param values the values
       */
      public ComplexExpression(@NonNull Collection<? extends SQLElement> values) {
         this.values = values;
         this.delimiter = " ";
      }

      @Override
      public String render(@NonNull SQLDialect dialect) {
         return dialect.join(delimiter, values);
      }

   }//END OF ComplexExpression

   @Value
   private static class NULL implements NamedSQLElement, SQLOperable {
      private static final long serialVersionUID = 1L;

      @Override
      public String getName() {
         return "NULL";
      }


   }//END OF NULL


   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   @AllArgsConstructor
   private static class NamedArgument implements CompositeSQLElement {
      private static final long serialVersionUID = 1L;
      @NonNull SQLElement argument;

      @Override
      public String render(@NonNull SQLDialect dialect) {
         return "[:" + NamedPreparedStatement.sanitize(dialect.render(argument)) + ":]";
      }

   }//END OF NamedArgument

   /**
    * Set of commonly used SQL functions
    */
   public static final class F {

      private F() {
         throw new IllegalAccessError();
      }

      /**
       * Generates an {@link SQLOperable} representing a regular expression matching operating
       *
       * @param arg1 the value to check the regular expression against.
       * @param arg2 the regular expression
       * @return the SQLOperable
       */
      public static SQLOperable regexp(@NonNull SQLElement arg1, @NonNull SQLElement arg2) {
         return SQLFunction.function(SQLConstants.REGEXP, arg1, arg2);
      }

      /**
       * Generates an {@link SQLOperable} representing a regular expression matching operating
       *
       * @param arg1 the value to check the regular expression against.
       * @param arg2 the regular expression
       * @return the SQLOperable
       */
      public static SQLOperable regexp(@NonNull SQLElement arg1, @NonNull String arg2) {
         return SQLFunction.function(SQLConstants.REGEXP, arg1, SQL.L(arg2));
      }

      /**
       * Generates an {@link SQLOperable} representing the absolute value of the given argument.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable abs(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.ABS, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the absolute value of the given column.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable abs(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.ABS, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the average value of the given argument.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable average(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.AVERAGE, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the average value of the given column.
       *
       * @param arg the column whose values will be averaged
       * @return the SQLOperable
       */
      public static SQLOperable average(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.AVERAGE, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the coalesce function which returns the first non-null value.
       *
       * @param args the function arguments
       * @return the SQLOperable
       */
      public static SQLOperable coalesce(@NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.COALESCE, Arrays.asList(args));
      }

      /**
       * Generates an {@link SQLOperable} representing the count value of the given argument.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable count(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.COUNT, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the count value of the given column.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable count(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.COUNT, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the distinct count value of the given argument.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable countDistinct(@NonNull SQLOperable arg) {
         return SQLFunction.function(SQLConstants.COUNT_DISTINCT, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the distinct count value of the given column.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable countDistinct(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.COUNT_DISTINCT, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the date function, which converts the given time string into a
       * date object. Depending on the underlying RDBMS the date function may take extra arguments denoting such tings
       * as the date format.
       *
       * @param timeString the time string value
       * @param args       extract arguments for formatting the date
       * @return the SQLOperable
       */
      public static SQLOperable date(@NonNull SQLElement timeString, @NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.DATE, timeString, args);
      }

      /**
       * Generates an {@link SQLOperable} representing the date function, which converts the given time string into a
       * date object. Depending on the underlying RDBMS the date function may take extra arguments denoting such tings
       * as the date format.
       *
       * @param timeString the time string literal value
       * @param args       extract arguments for formatting the date
       * @return the SQLOperable
       */
      public static SQLOperable date(@NonNull String timeString, @NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.DATE, L(timeString), args);
      }

      /**
       * Generates an {@link SQLOperable} representing the datetime function, which converts the given time string into
       * a date object. Depending on the underlying RDBMS the date function may take extra arguments denoting such tings
       * as the date format.
       *
       * @param timeString the time string value
       * @param args       extract arguments for formatting the date
       * @return the SQLOperable
       */
      public static SQLOperable dateTime(@NonNull SQLElement timeString, @NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.DATETIME, timeString, args);
      }

      /**
       * Generates an {@link SQLOperable} representing the datetime function, which converts the given time string into
       * a date object. Depending on the underlying RDBMS the date function may take extra arguments denoting such tings
       * as the date format.
       *
       * @param timeString the time string literal value
       * @param args       extract arguments for formatting the date
       * @return the SQLOperable
       */
      public static SQLOperable dateTime(@NonNull String timeString, @NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.DATETIME, L(timeString), args);
      }

      /**
       * Generates an {@link SQLOperable} representing the group concat function, which concatenates one more items with
       * a given delimiter.
       *
       * @param arg       the argument to perform group concat on
       * @param delimiter the delimiter to separate items with
       * @return the SQLOperable
       */
      public static SQLOperable groupConcat(@NonNull SQLElement arg, @NonNull String delimiter) {
         return SQLFunction.function(SQLConstants.GROUP_CONCAT, arg, L(delimiter));
      }

      /**
       * Generates an {@link SQLOperable} representing the group concat function, which concatenates one more items with
       * a given delimiter.
       *
       * @param args      the arguments to perform group concat on
       * @param delimiter the delimiter to separate items with
       * @return the SQLOperable
       */
      public static SQLOperable groupConcat(@NonNull Collection<? extends SQLElement> args, @NonNull String delimiter) {
         return SQLFunction.function(SQLConstants.GROUP_CONCAT, new ComplexExpression(args, ", "), L(delimiter));
      }

      /**
       * Generates an {@link SQLOperable} representing the group concat function, which concatenates one more items with
       * a given delimiter.
       *
       * @param arg       the column to perform group concat on
       * @param delimiter the delimiter to separate items with
       * @return the SQLOperable
       */
      public static SQLOperable groupConcat(@NonNull String arg, @NonNull String delimiter) {
         return SQLFunction.function(SQLConstants.GROUP_CONCAT, C(arg), L(delimiter));
      }

      /**
       * Generates an {@link SQLOperable} representing an "if null" function, which returns the specified value if the
       * expression is null.
       *
       * @param expression the expression to evaluate
       * @param value      the value to return if the expression evalutes to null
       * @return the SQLOperable
       */
      public static SQLOperable ifNull(@NonNull SQLElement expression, @NonNull SQLElement value) {
         return SQLFunction.function(SQLConstants.IFNULL, expression, value);
      }

      /**
       * Generates an {@link SQLOperable} representing an "in string" function, which searches a string for a
       * substring.
       *
       * @param string    the string to be searched
       * @param substring the substring to search for
       * @return the SQLOperable
       */
      public static SQLOperable instr(@NonNull SQLElement string, @NonNull SQLElement substring) {
         return SQLFunction.function(SQLConstants.INSTR, string, substring);
      }

      /**
       * Generates an {@link SQLOperable} representing an "in string" function, which searches a string for a
       * substring.
       *
       * @param string    the string to be searched
       * @param substring the literal substring to search for
       * @return the SQLOperable
       */
      public static SQLOperable instr(@NonNull SQLElement string, @NonNull String substring) {
         return SQLFunction.function(SQLConstants.INSTR, string, L(substring));
      }

      /**
       * Generates an {@link SQLOperable} representing a function to extract a json value (object, array, primitive)
       * from a json column for a given path in the json tree.
       *
       * @param column   the name of the column containing json
       * @param jsonPath the path of the json element to extract.
       * @return the SQLOperable
       */
      public static SQLOperable json_extract(@NonNull String column, @NonNull String jsonPath) {
         return SQLFunction.function(SQLConstants.JSON_EXTRACT, C(column), L(jsonPath));
      }

      /**
       * Generates an {@link SQLOperable} representing a function to extract a json value (object, array, primitive)
       * from a json column for a given path in the json tree.
       *
       * @param json     the json
       * @param jsonPath the path of the json element to extract.
       * @return the SQLOperable
       */
      public static SQLOperable json_extract(@NonNull SQLElement json, @NonNull SQLElement jsonPath) {
         return SQLFunction.function(SQLConstants.JSON_EXTRACT, json, jsonPath);
      }

      /**
       * Generates an {@link SQLOperable} representing the length of the string value represented by the given
       * argument.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable length(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.LENGTH, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the length of the string value in the given column.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable length(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.LENGTH, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the lower function, which lower cases a string value.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable lower(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.LOWER, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the lower function, which lower cases a string value.
       *
       * @param arg the column whose values will be lower cased
       * @return the SQLOperable
       */
      public static SQLOperable lower(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.LOWER, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the ltrim function, which trims from the left side of a string.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable ltrim(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.LTRIM, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the ltrim function, which trims from the left side of a string.
       *
       * @param arg the column whose values will be ltrimed
       * @return the SQLOperable
       */
      public static SQLOperable ltrim(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.LTRIM, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the max function.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable max(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.MAX, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the max function.
       *
       * @param arg the column to find the max over
       * @return the SQLOperable
       */
      public static SQLOperable max(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.MAX, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the min function.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable min(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.MIN, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the min function.
       *
       * @param arg the column to find the min over
       * @return the SQLOperable
       */
      public static SQLOperable min(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.MIN, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing an "null if" function, which returns null if the two arguments
       * are equal.
       *
       * @param arg1 the first argument
       * @param arg2 the second argument
       * @return the SQLOperable
       */
      public static SQLOperable nullIf(@NonNull SQLElement arg1, @NonNull SQLElement arg2) {
         return SQLFunction.function(SQLConstants.NULLIF, arg1, arg2);
      }

      /**
       * Generates an {@link SQLOperable} representing the random function
       *
       * @return the {@link SQLOperable}
       */
      public static SQLOperable random() {
         return SQLFunction.function(SQLConstants.RANDOM);
      }

      /**
       * Generates an {@link SQLOperable} representing an string "replace" function, which replaces a given pattern with
       * a given replacement in string expression.
       *
       * @param arg         the string expression for which the pattern will be replace
       * @param pattern     the pattern to replace
       * @param replacement the string to replace the pattern with
       * @return the SQLOperable
       */
      public static SQLOperable replace(@NonNull SQLElement arg, String pattern, String replacement) {
         return SQLFunction.function(SQLConstants.REPLACE, arg, L(pattern), L(replacement));
      }

      /**
       * Generates an {@link SQLOperable} representing an string "replace" function, which replaces a given pattern with
       * a given replacement in string expression.
       *
       * @param column      the column whose values will be replaced
       * @param pattern     the pattern to replace
       * @param replacement the string to replace the pattern with
       * @return the SQLOperable
       */
      public static SQLOperable replace(@NonNull String column, String pattern, String replacement) {
         return SQLFunction.function(SQLConstants.REPLACE, SQL.C(column), L(pattern), L(replacement));
      }

      /**
       * Generates an {@link SQLOperable} that rounds the given argument to the given precision.
       *
       * @param arg       the arg
       * @param precision the precision
       * @return the SQLOperable
       */
      public static SQLOperable round(@NonNull SQLElement arg, int precision) {
         return SQLFunction.function(SQLConstants.ROUND, arg, N(precision));
      }

      /**
       * Generates an {@link SQLOperable} that rounds the given argument to the given precision.
       *
       * @param column    the column whose values will be rounded.
       * @param precision the precision
       * @return the SQLOperable
       */
      public static SQLOperable round(@NonNull String column, int precision) {
         return SQLFunction.function(SQLConstants.ROUND, C(column), N(precision));
      }

      /**
       * Generates an {@link SQLOperable} representing the right function, which trims from the right side of a string.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable rtrim(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.RTRIM, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the rtrim function, which trims from the right side of a string.
       *
       * @param arg the column whose values will be rtrimed
       * @return the SQLOperable
       */
      public static SQLOperable rtrim(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.RTRIM, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing a function to format times as strings.
       *
       * @param format     the time format
       * @param timeString the time string to convert
       * @param args       the args used in formation
       * @return the SQLOperable
       */
      public static SQLOperable strftime(@NonNull SQLElement format,
                                         @NonNull SQLElement timeString,
                                         @NonNull SQLElement... args) {
         return SQLFunction
               .function(SQLConstants.STRFTIME, format, Stream.concat(Stream.of(timeString), Arrays.stream(args))
                                                              .toArray(SQLElement[]::new));
      }

      /**
       * Generates an {@link SQLOperable} representing an "substring" function, which returns a substring from the given
       * starting position and of the given length from an argument.
       *
       * @param arg    the string expression to generate the substring from
       * @param start  the starting index
       * @param length the length of substring
       * @return the SQLOperable
       */
      public static SQLOperable substr(@NonNull SQLElement arg, int start, int length) {
         return SQLFunction.function(SQLConstants.SUBSTR, arg, N(start), N(length));
      }

      /**
       * Generates an {@link SQLOperable} representing an "substring" function, which returns a substring from the given
       * starting position and of the given length from an argument.
       *
       * @param arg    the string expression to generate the substring from
       * @param start  the starting index
       * @param length the length of substring
       * @return the SQLOperable
       */
      public static SQLOperable substr(@NonNull SQLElement arg, @NonNull SQLElement start, @NonNull SQLElement length) {
         return SQLFunction.function(SQLConstants.SUBSTR, arg, start, length);
      }

      /**
       * Generates an {@link SQLOperable} representing an "substring" function, which returns a substring from the given
       * starting position and of the given length from an argument.
       *
       * @param column the column whose values will be passed to the substring
       * @param start  the starting index
       * @param length the length of substring
       * @return the SQLOperable
       */
      public static SQLOperable substr(@NonNull String column, int start, int length) {
         return SQLFunction.function(SQLConstants.SUBSTR, SQL.C(column), N(start), N(length));
      }

      /**
       * Generates an {@link SQLOperable} representing an "substring" function, which returns a substring from the given
       * starting position and of the given length from an argument.
       *
       * @param column the column whose values will be passed to the substring
       * @param start  the starting index
       * @param length the length of substring
       * @return the SQLOperable
       */
      public static SQLOperable substr(@NonNull String column, @NonNull SQLElement start, @NonNull SQLElement length) {
         return SQLFunction.function(SQLConstants.SUBSTR, SQL.C(column), start, length);
      }

      /**
       * Generates an {@link SQLOperable} representing the sum function.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable sum(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.SUM, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the sum function.
       *
       * @param column the column whose values will be summed
       * @return the SQLOperable
       */
      public static SQLOperable sum(@NonNull String column) {
         return SQLFunction.function(SQLConstants.SUM, C(column));
      }

      /**
       * Generates an {@link SQLOperable} representing the datetime function, which converts the given time string into
       * a date object. Depending on the underlying RDBMS the date function may take extra arguments denoting such tings
       * as the date format.
       *
       * @param timeString the time string value
       * @param args       extract arguments for formatting the date
       * @return the SQLOperable
       */
      public static SQLOperable time(@NonNull SQLOperable timeString, @NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.TIME, timeString, args);
      }

      /**
       * Generates an {@link SQLOperable} representing the time function, which converts the given time string into a
       * date object. Depending on the underlying RDBMS the date function may take extra arguments denoting such tings
       * as the date format.
       *
       * @param timeString the time string literal value
       * @param args       extract arguments for formatting the date
       * @return the SQLOperable
       */
      public static SQLOperable time(@NonNull String timeString, @NonNull SQLElement... args) {
         return SQLFunction.function(SQLConstants.TIME, L(timeString), args);
      }

      /**
       * Generates an {@link SQLOperable} representing the trim function, which trims from the right and left side of a
       * string.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable trim(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.TRIM, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the trim function, which trims from the right and left side of a
       * string.
       *
       * @param arg the column whose values will be trimmed
       * @return the SQLOperable
       */
      public static SQLOperable trim(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.TRIM, C(arg));
      }

      /**
       * Generates an {@link SQLOperable} representing the lower function, which lower cases a string value.
       *
       * @param arg the function argument
       * @return the SQLOperable
       */
      public static SQLOperable upper(@NonNull SQLElement arg) {
         return SQLFunction.function(SQLConstants.UPPER, arg);
      }

      /**
       * Generates an {@link SQLOperable} representing the upper function, which lower cases a string value.
       *
       * @param arg the column whose values will be upper cased
       * @return the SQLOperable
       */
      public static SQLOperable upper(@NonNull String arg) {
         return SQLFunction.function(SQLConstants.UPPER, C(arg));
      }

   }//END OF F


   public static class TableDef {
      private final List<Column> columns = new ArrayList<>();
      private final List<Constraint> constraints = new ArrayList<>();

      public Column column(String name, String type) {
         Column column = new Column(name, type);
         columns.add(column);
         return column;
      }

      public ConstraintBuilder constraint(String name) {
         return Cast.as(Proxy.newProxyInstance(Constraint.class.getClassLoader(),
                                               new Class[]{ConstraintBuilder.class},
                                               new ConstraintHandler(Constraint.constraint(name))));
      }


      private class ConstraintHandler implements InvocationHandler {
         final ConstraintBuilder backing;

         private ConstraintHandler(ConstraintBuilder backing) {
            this.backing = backing;
         }

         @Override
         public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            Object r = method.invoke(backing, objects);
            if (r instanceof Constraint) {
               constraints.add(Cast.as(r));
            }
            return r;
         }
      }


   }
}//END OF SQL
