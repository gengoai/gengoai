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

import com.gengoai.LogUtils;
import com.gengoai.conversion.Cast;
import com.gengoai.function.Switch;
import com.gengoai.sql.constraint.*;
import com.gengoai.sql.object.*;
import com.gengoai.sql.operator.*;
import com.gengoai.sql.statement.*;
import com.gengoai.string.Strings;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.gengoai.function.Switch.$switch;

/**
 * The type Sql dialect 2.
 */
@Log
public abstract class SQLDialect implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * Controls the log level for previewing generated SQL
    */
   @NonNull
   public static Level LOG_LEVEL = Level.FINEST;

   private final Switch<String> renderer = $switch($ -> {
      //-------------------------------------------------------------------------------------
      // Special Primitive Types
      //-------------------------------------------------------------------------------------
      //Handle PreRendered
      $.instanceOf(PreRenderedSQL.class, Object::toString);
      //Handle Named Objects
      $.instanceOf(NamedSQLElement.class, NamedSQLElement::getName);
      //Handle Composite SQL Objects
      $.instanceOf(CompositeSQLElement.class, c -> c.render(this));
      //-------------------------------------------------------------------------------------

      //-------------------------------------------------------------------------------------
      // SQL Statements
      //-------------------------------------------------------------------------------------
      //Handle Alter Table and Alter Table Actions
      $.instanceOf(AlterTable.class, this::alterTable);
      $.instanceOf(AlterTableAction.AlterColumn.class, this::alterColumn);
      $.instanceOf(AlterTableAction.AddColumn.class, this::addColumn);
      $.instanceOf(AlterTableAction.DropColumn.class, this::dropColumn);
      $.instanceOf(AlterTableAction.RenameColumn.class, this::renameColumn);
      $.instanceOf(AlterTableAction.RenameTable.class, this::renameTable);

      //Handle Creates
      $.instanceOf(Create.class, create -> {
         if (create.getObject() instanceof Table) {
            return createTable(create);
         } else if (create.getObject() instanceof Index) {
            return createIndex(create);
         } else {
            return createTrigger(create);
         }
      });

      //Handle Delete
      $.instanceOf(Delete.class, this::delete);

      //Handle Drop
      $.instanceOf(Drop.class, this::drop);

      //Handle Insert
      $.instanceOf(Insert.class, this::insert);
      $.instanceOf(InsertType.class, this::insertType);

      //Handle Join and JoinType
      $.instanceOf(Join.class, this::join);
      $.instanceOf(JoinType.class, this::joinType);

      //Handle Select
      $.instanceOf(Select.class, this::select);

      //Handle Update and UpdateType
      $.instanceOf(Update.class, this::update);
      $.instanceOf(UpdateType.class, this::updateType);

      //UpsertAction and UpsertClause
      $.instanceOf(UpsertClause.class, this::upsertClause);
      $.instanceOf(UpsertAction.class, this::upsertAction);
      //-------------------------------------------------------------------------------------

      //-------------------------------------------------------------------------------------
      // SQL Functions and Operators
      //-------------------------------------------------------------------------------------
      //Handle Functions
      $.instanceOf(SQLFunction.class, this::function);

      //Handle Operators
      $.instanceOf(InfixBinaryOperator.class, this::binaryOperator);
      $.instanceOf(PrefixUnaryOperator.class, this::prefixUnaryOperator);
      $.instanceOf(PostfixUnaryOperator.class, this::postfixUnaryOperator);
      $.instanceOf(Between.class, this::between);
      $.instanceOf(QueryOperator.class, this::queryOperator);
      //-------------------------------------------------------------------------------------

      //-------------------------------------------------------------------------------------
      // SQL Constraints
      // Note: Constraints are not SQLElements as they can be reused for either Table or
      //       Column constraints
      //-------------------------------------------------------------------------------------
      //Handle Conflict Clauses
      $.instanceOf(ConflictClause.class, this::conflictClause);
      //Handle Deferrable
      $.instanceOf(Deferrable.class, this::deferrable);
      //Handle Foreign Key Actions
      $.instanceOf(ForeignKeyAction.class, this::foreignKeyAction);
      //-------------------------------------------------------------------------------------

      //-------------------------------------------------------------------------------------
      // SQL Object Helpers
      //-------------------------------------------------------------------------------------
      //Handle Trigger Time
      $.instanceOf(TriggerTime.class, this::triggerTime);
      //Handle SQLDMLOperation
      $.instanceOf(SQLDMLOperation.class, this::sqlDMLOperation);
      //-------------------------------------------------------------------------------------

   });

   /**
    * Static helper method for properly escaping SQL strings.
    *
    * @param string the string to escape
    * @return the escaped string
    */
   public static String escape(@NonNull String string) {
      return string.replaceAll("'", "''").replaceAll("\"", "\\\"");
   }

   /**
    * Add column string.
    *
    * @param alterColumn the alter column
    * @return the string
    */
   protected String addColumn(AlterTableAction.AddColumn alterColumn) {
      StringBuilder builder = new StringBuilder("ADD COLUMN ");
      defineColumn(alterColumn.getNewDefinition(), builder);
      return builder.toString();
   }

   /**
    * Alter column string.
    *
    * @param alterColumn the alter column
    * @return the string
    */
   protected String alterColumn(AlterTableAction.AlterColumn alterColumn) {
      StringBuilder builder = new StringBuilder("ALTER COLUMN ");
      defineColumn(alterColumn.getNewDefinition(), builder);
      return builder.toString();
   }

   /**
    * Alter table string.
    *
    * @param alterTable the alter table
    * @return the string
    */
   protected String alterTable(AlterTable alterTable) {
      StringBuilder builder = new StringBuilder("ALTER TABLE ")
            .append(render(alterTable.getTable()))
            .append("\n");
      for (AlterTableAction action : alterTable.getActions()) {
         builder.append(render(action)).append(";\n");
      }
      return builder.toString();
   }

   /**
    * Between string.
    *
    * @param between the between
    * @return the string
    */
   protected String between(Between between) {
      return render(between.getColumn()) +
            " " +
            translate(between.getOperator()) +
            " " +
            render(between.getLower()) +
            " AND " +
            render(between.getHigher());
   }

   /**
    * Binary operator string.
    *
    * @param binaryOperator the binary operator
    * @return the string
    */
   protected String binaryOperator(@NonNull InfixBinaryOperator binaryOperator) {
      return String.format(binaryOperator.isRequiresParenthesis()
                                 ? "(%s %s %s)"
                                 : "%s %s %s",
                           render(binaryOperator.getArg1()),
                           translate(binaryOperator.getOperator()),
                           render(binaryOperator.getArg2()));
   }

   /**
    * Check constraint.
    *
    * @param checkConstraint   the check constraint
    * @param isTableConstraint the is table constraint
    * @param builder           the builder
    */
   protected void checkConstraint(CheckConstraint checkConstraint, boolean isTableConstraint, StringBuilder builder) {
      builder.append(" ")
             .append(translate("CHECK"))
             .append(" (")
             .append(render(checkConstraint.getExpression()))
             .append(")");
   }

   /**
    * Clean under scores string.
    *
    * @param str the str
    * @return the string
    */
   protected String cleanUnderScores(String str) {
      return translate(str.replace('_', ' '));
   }

   /**
    * Generates the SQL for defining what happens on conflict
    *
    * @param conflictClause the conflict clause
    * @return the SQL
    */
   protected String conflictClause(ConflictClause conflictClause) {
      return "ON CONFLICT " + translate(conflictClause.name());
   }

   /**
    * Create index string.
    *
    * @param createStatement the create statement
    * @return the string
    */
   protected String createIndex(Create createStatement) {
      Index index = Cast.as(createStatement.getObject());
      StringBuilder create = new StringBuilder("CREATE ");
      if (index.isUnique()) {
         create.append("UNIQUE ");
      }
      create.append("INDEX");
      if (createStatement.isIfNotExists()) {
         create.append(" IF NOT EXISTS ");
      }
      return create.append("\"")
                   .append(index.getName())
                   .append("\" on ")
                   .append(render(index.getTable()))
                   .append(" ( ")
                   .append(join(", ", index.getColumns()))
                   .append(" );").toString();
   }

   /**
    * Create table string.
    *
    * @param statement the statement
    * @return the string
    */
   protected String createTable(Create statement) {
      Table table = Cast.as(statement.getObject());
      StringBuilder builder = new StringBuilder("CREATE TABLE ");
      if (statement.isIfNotExists()) {
         builder.append("IF NOT EXISTS ");
      }
      builder.append(table.getName()).append(" (");
      for (int i = 0; i < table.getColumns().size(); i++) {
         Column column = table.getColumns().get(i);
         if (i > 0) {
            builder.append(", ");
         }
         builder.append("\n");
         defineColumn(column, builder);
      }
      for (Constraint constraint : table.getConstraints()) {
         builder.append(",\n");
         defineConstraint(constraint, true, builder);
      }
      return builder.append("\n)").toString();
   }

   /**
    * Create trigger string.
    *
    * @param createStatement the create statement
    * @return the string
    */
   protected String createTrigger(Create createStatement) {
      Trigger trigger = Cast.as(createStatement.getObject());
      StringBuilder create = new StringBuilder("CREATE ");
      if (trigger.isTemporary()) {
         create.append("TEMP ");
      }
      create.append("TRIGGER ");
      if (createStatement.isIfNotExists()) {
         create.append("IF NOT EXISTS ");
      }
      create.append(trigger.getName())
            .append(" ")
            .append(triggerTime(trigger.getWhen()))
            .append(" ")
            .append(sqlDMLOperation(trigger.getOperation()))
            .append(" ON ")
            .append(render(trigger.getTable()));
      if (trigger.isRowLevelTrigger()) {
         create.append(" FOR EACH ROW");
      }
      if (trigger.getWhere() != null) {
         create.append(" WHERE ")
               .append(render(trigger.getWhere()));
      }
      return create.append(" BEGIN ")
                   .append(render(trigger.getUpdateStatement()))
                   .append("; END")
                   .toString();

   }

   /**
    * Deferrable string.
    *
    * @param deferrable the deferrable
    * @return the string
    */
   public String deferrable(Deferrable deferrable) {
      switch (deferrable) {
         case INITIALLY_IMMEDIATE:
            return "DEFERRABLE INITIALLY IMMEDIATE";
         case INITIALLY_DEFERRED:
            return "DEFERRABLE INITIALLY DEFERRED";
         default:
            return "NOT DEFERRABLE";
      }
   }

   /**
    * Generates the SQL needed to define a column for creating or altering a table.
    *
    * @param column  the column definition
    * @param builder the StringBuilder to add the definition to
    */
   protected abstract void defineColumn(Column column, StringBuilder builder);

   /**
    * Define constraint.
    *
    * @param constraint        the constraint
    * @param isTableConstraint the is table constraint
    * @param builder           the builder
    */
   protected final void defineConstraint(Constraint constraint, boolean isTableConstraint, StringBuilder builder) {
      if (Strings.isNotNullOrBlank(constraint.getName())) {
         builder.append(" ")
                .append(translate("CONSTRAINT"))
                .append(" \"")
                .append(constraint.getName())
                .append("\"");
      }
      if (constraint instanceof CheckConstraint) {
         checkConstraint(Cast.as(constraint), isTableConstraint, builder);
      } else if (constraint instanceof UniqueConstraint) {
         uniqueConstraint(Cast.as(constraint), isTableConstraint, builder);
      } else if (constraint instanceof NotNullConstraint) {
         notNullConstraint(Cast.as(constraint), isTableConstraint, builder);
      } else if (constraint instanceof PrimaryKeyConstraint) {
         primaryKeyConstraint(Cast.as(constraint), isTableConstraint, builder);
      } else if (constraint instanceof ForeignKeyConstraint) {
         foreignKeyConstraint(Cast.as(constraint), isTableConstraint, builder);
      } else {
         throw new IllegalArgumentException("Unsupported Constraint: '" + constraint.getClass().getName() + "'");
      }

      if (constraint instanceof ConstraintWithConflictClause) {
         ConstraintWithConflictClause<?> cwcc = Cast.as(constraint);
         if (cwcc.getConflictClause() != null) {
            builder.append(" ").append(conflictClause(cwcc.getConflictClause()));
         }
      }
   }

   /**
    * Generates the SQL to delete from a table.
    *
    * @param delete the delete object
    * @return the SQL
    */
   protected String delete(Delete delete) {
      StringBuilder builder = new StringBuilder("DELETE FROM ")
            .append(render(delete.getTable()));
      if (delete.getWhere() != null) {
         builder.append(" WHERE ").append(render(delete.getWhere()));
      }
      return builder.toString();
   }

   /**
    * Generates the SQL to drop an object
    *
    * @param drop the drop object
    * @return the SQL
    */
   protected String drop(Drop drop) {
      String statement = "DROP " + drop.getObject().getKeyword();
      if (drop.isIfExists()) {
         statement += " IF EXISTS";
      }
      return statement + " " + drop.getObject().getName() + ";";
   }

   /**
    * Drop column string.
    *
    * @param dropColumn the drop column
    * @return the string
    */
   protected String dropColumn(AlterTableAction.DropColumn dropColumn) {
      return "DROP COLUMN " + SQLDialect.escape(dropColumn.getName());
   }

   /**
    * Foreign key action string.
    *
    * @param action the action
    * @return the string
    */
   public String foreignKeyAction(ForeignKeyAction action) {
      return action.name().replace('_', ' ');
   }

   /**
    * Foreign key constraint.
    *
    * @param fkc               the fkc
    * @param isTableConstraint the is table constraint
    * @param builder           the builder
    */
   protected void foreignKeyConstraint(ForeignKeyConstraint fkc, boolean isTableConstraint, StringBuilder builder) {
      builder.append(" ")
             .append(translate("FOREIGN KEY"))
             .append("(")
             .append(join(", ", fkc.getColumns()))
             .append(") ")
             .append(translate("REFERENCES"))
             .append(" ")
             .append(render(fkc.getForeignTable()))
             .append("( ")
             .append(join(", ", fkc.getForeignTableColumns()))
             .append(" )");
      if (fkc.getOnUpdate() != null) {
         builder.append("\n\t")
                .append(translate("ON UPDATE"))
                .append(" ")
                .append(foreignKeyAction(fkc.getOnUpdate()));
      }
      if (fkc.getOnDelete() != null) {
         builder.append("\n\t")
                .append(translate("ON DELETE"))
                .append(" ")
                .append(foreignKeyAction(fkc.getOnDelete()));
      }
      if (fkc.getDeferred() != Deferrable.NOT_DEFERRABLE && fkc.getDeferred() != null) {
         builder.append("\n\t").append(deferrable(fkc.getDeferred()));
      }
   }

   /**
    * Generates the SQL for a call to a predefined function.
    *
    * @param function the function call
    * @return the SQL
    */
   protected String function(@NonNull SQLFunction function) {
      return translate(function.getName()) + "(" + function.getArguments()
                                                           .stream()
                                                           .map(this::render)
                                                           .collect(Collectors.joining(", ")) + ")";
   }

   /**
    * Generates the SQL to insert a row into a table
    *
    * @param insert the insert object
    * @return the SQL
    */
   protected String insert(Insert insert) {
      StringBuilder insertStmt = new StringBuilder(insertType(insert.getInsertType()))
            .append(" INTO ")
            .append(render(insert.getTable()))
            .append(" ");
      if (insert.getColumns().size() > 0) {
         insertStmt.append("( ")
                   .append(join(", ", insert.getColumns()))
                   .append(" )");
      }
      if (insert.getSelect() != null) {
         insertStmt.append(" ").append(render(insert.getSelect()));
      } else if (insert.isDefaultValues()) {
         insertStmt.append(" DEFAULT VALUES");
      } else {
         insertStmt.append(" VALUES( ")
                   .append(join(", ", insert.getValues()))
                   .append(" )");
      }

      if (insert.getOnConflict() != null) {
         insertStmt.append(" ").append(upsertClause(insert.getOnConflict()));
      }
      return insertStmt.toString();
   }

   /**
    * Insert type string.
    *
    * @param insertType the insert type
    * @return the string
    */
   protected String insertType(InsertType insertType) {
      return cleanUnderScores(insertType.name());
   }


   /**
    * Joins one or more {@link SQLElement} as SQL with the given delimiter
    *
    * @param delimiter the delimiter to use to separate the SQLElements
    * @param items     the items to join
    * @return the SQL
    */
   protected String join(@NonNull String delimiter, @NonNull Collection<? extends SQLElement> items) {
      return items.stream().map(this::render).collect(Collectors.joining(delimiter));
   }

   /**
    * Generates the SQL defining a join operation
    *
    * @param join the join
    * @return the SQL
    */
   protected String join(@NonNull Join join) {
      StringBuilder builder = new StringBuilder(joinType(join.getType()))
            .append(" JOIN");
      builder.append(" ")
             .append(render(join.getWith()));
      if (join.getOn() != null) {
         builder.append(" ON ")
                .append(render(join.getOn()));
      }
      return builder.toString();
   }

   /**
    * Join type string.
    *
    * @param joinType the join type
    * @return the string
    */
   protected String joinType(JoinType joinType) {
      return cleanUnderScores(joinType.name());
   }

   /**
    * Not null constraint.
    *
    * @param notNullConstraint the not null constraint
    * @param isTableConstraint the is table constraint
    * @param builder           the builder
    */
   protected void notNullConstraint(NotNullConstraint notNullConstraint, boolean isTableConstraint, StringBuilder builder) {
      String notNullKeyword = translate("NOT NULL");
      builder.append(" ").append(notNullKeyword);
      if (isTableConstraint) {
         builder.append(" (")
                .append(render(notNullConstraint.getColumn()))
                .append(")");
      }
   }

   /**
    * Postfix unary operator string.
    *
    * @param op the op
    * @return the string
    */
   protected String postfixUnaryOperator(@NonNull PostfixUnaryOperator op) {
      return String.format(op.isRequiresParenthesis()
                                 ? "(%s %s)"
                                 : "%s %s",
                           render(op.getArg1()),
                           translate(op.getOperator()));
   }

   /**
    * Prefix unary operator string.
    *
    * @param op the op
    * @return the string
    */
   protected String prefixUnaryOperator(@NonNull PrefixUnaryOperator op) {
      return String.format(op.isRequiresParenthesis()
                                 ? "(%s %s)"
                                 : "%s %s",
                           translate(op.getOperator()),
                           render(op.getArg1()));
   }

   /**
    * Primary key constraint.
    *
    * @param primaryKeyConstraint the primary key constraint
    * @param isTableConstraint    the is table constraint
    * @param builder              the builder
    */
   protected void primaryKeyConstraint(PrimaryKeyConstraint primaryKeyConstraint, boolean isTableConstraint, StringBuilder builder) {
      builder.append(" ")
             .append(translate("PRIMARY KEY"));
      if (isTableConstraint) {
         builder.append("(")
                .append(join(", ", primaryKeyConstraint.getColumns()))
                .append(")");
      }
   }

   /**
    * Generates the SQL for defining a query operator (UNION, EXCLUDE, etc.)
    *
    * @param queryOperator the query operator
    * @return the SQL
    */
   protected String queryOperator(QueryOperator queryOperator) {
      return render(queryOperator.getQuery1()) + "\n" + translate(queryOperator.getName()) + "\n" + render(queryOperator
                                                                                                                 .getQuery2());
   }

   /**
    * Rename column string.
    *
    * @param renameColumn the rename column
    * @return the string
    */
   protected String renameColumn(AlterTableAction.RenameColumn renameColumn) {
      return "RENAME COLUMN " + SQLDialect.escape(renameColumn.getOldName()) + " TO " + SQLDialect
            .escape(renameColumn.getNewName());
   }

   /**
    * Rename table string.
    *
    * @param renameTable the rename table
    * @return the string
    */
   protected String renameTable(AlterTableAction.RenameTable renameTable) {
      return "RENAME TO " + SQLDialect.escape(renameTable.getNewTableName());
   }

   /**
    * Render string.
    *
    * @param sqlElement the sql element
    * @return the string
    */
   public final String render(@NonNull SQLElement sqlElement) {
      String sql = renderer.apply(sqlElement);
      LogUtils.log(log, LOG_LEVEL, "Rendering {0} as\n=======================\n{1}\n=======================", sqlElement
            .getClass().getSimpleName(), sql);
      return sql;
   }

   /**
    * Generates the SQL for select statement
    *
    * @param select the select statement
    * @return the SQL
    */
   protected String select(@NonNull Select select) {
      StringBuilder statement = new StringBuilder("SELECT ");
      if (select.isDistinct()) {
         statement.append("DISTINCT ");

      }
      statement.append(join(", ", select.getColumns()))
               .append("\nFROM ")
               .append(join("\n", select.getFrom()));
      if (select.getWhere() != null) {
         statement.append("\nWHERE ").append(render(select.getWhere()));
      }
      if (select.getGroupBy().size() > 0) {
         statement.append("\nGROUP BY ").append(join(", ", select.getGroupBy()));
      }
      if (select.getHaving() != null) {
         statement.append("\nHAVING ").append(render(select.getHaving()));
      }
      if (select.getOrderBy().size() > 0) {
         statement.append("\nORDER BY ").append(join(", ", select.getOrderBy()));
      }
      if (select.getLimit() != null) {
         statement.append("\nLIMIT ").append(render(select.getLimit()));
      }
      return statement.toString();
   }

   /**
    * Sql dml operation string.
    *
    * @param operation the operation
    * @return the string
    */
   protected String sqlDMLOperation(SQLDMLOperation operation) {
      return operation.name();
   }

   /**
    * Provides dialect specific translations of keywords and symbols
    *
    * @param keyword the keyword
    * @return the tranlsation
    */
   protected String translate(String keyword) {
      return keyword;
   }

   /**
    * Trigger time string.
    *
    * @param triggerTime the trigger time
    * @return the string
    */
   protected String triggerTime(TriggerTime triggerTime) {
      return triggerTime.name().replace('_', ' ');
   }

   /**
    * Unique constraint.
    *
    * @param uniqueConstraint  the unique constraint
    * @param isTableConstraint the is table constraint
    * @param builder           the builder
    */
   protected void uniqueConstraint(UniqueConstraint uniqueConstraint, boolean isTableConstraint, StringBuilder builder) {
      builder.append(" ").append(translate("UNIQUE"));
      if (isTableConstraint) {
         builder.append(" (")
                .append(join(", ", uniqueConstraint.getColumns()))
                .append(")");
      }
   }

   /**
    * Generates the SQL For an update statement
    *
    * @param update the update statement
    * @return the SQL
    */
   protected String update(Update update) {
      StringBuilder statement = new StringBuilder(updateType(update.getUpdateType()))
            .append(" ")
            .append(render(update.getTable()))
            .append(" SET ");
      for (int i = 0; i < update.getColumns().size(); i++) {
         if (i > 0) {
            statement.append(", ");
         }
         statement.append(render(update.getColumns().get(i)))
                  .append(" = ")
                  .append(render(update.getValues().get(i)));
      }
      statement.append(" WHERE ");
      if (update.getWhere() == null) {
         statement.append("1 = 1");
      } else {
         statement.append(render(update.getWhere()));
      }
      return statement.toString();
   }

   /**
    * Update type string.
    *
    * @param updateType the update type
    * @return the string
    */
   protected String updateType(UpdateType updateType) {
      return cleanUnderScores(updateType.name());
   }

   /**
    * Upsert action string.
    *
    * @param upsertAction the upsert action
    * @return the string
    */
   protected String upsertAction(UpsertAction upsertAction) {
      return cleanUnderScores(upsertAction.name());
   }

   /**
    * Generates the SQL for an upsert clause
    *
    * @param upsertClause the upser object
    * @return the SQL
    */
   protected String upsertClause(@NonNull UpsertClause upsertClause) {
      StringBuilder statement = new StringBuilder("ON CONFLICT ( ")
            .append(join(", ", upsertClause.getIndexedColumns()))
            .append(" )");
      if (upsertClause.getIndexedWhere() != null) {
         statement.append(" WHERE ").append(render(upsertClause.getIndexedWhere()));
      }
      statement.append(" DO ").append(upsertAction(upsertClause.getAction()));
      if (upsertClause.getAction() != UpsertAction.NOTHING) {
         for (int i = 0; i < upsertClause.getUpdateColumns().size(); i++) {
            if (i > 0) {
               statement.append(",");
            }
            statement.append(" ");
            statement.append(render(upsertClause.getUpdateColumns().get(i)))
                     .append(" = ")
                     .append(render(upsertClause.getUpdateValues().get(i)));
         }
         if (upsertClause.getUpdateWhere() != null) {
            statement.append(" WHERE ").append(render(upsertClause.getUpdateWhere()));
         }
      }

      return statement.toString();
   }

}//END OF SQLDialect
