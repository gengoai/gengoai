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

package com.gengoai.sql.sqlite;

/**
 * Dialect implementation for SQLite
 */
public final class SQLiteDialect { //} implements SQLDialect {
//   /**
//    * Singleton Instance of the dialect
//    */
//   public static final SQLDialect INSTANCE = new SQLiteDialect();
//   private static final long serialVersionUID = 1L;
//
//   @Override
//   public String columnDefinition(@NonNull Column column) {
//      return column.getName() + " " + column.getDataType() + " " + column.getConstraints()
//                                                                         .stream()
//                                                                         .map(cc -> cc.toSQL(this))
//                                                                         .collect(Collectors.joining(" "));
//   }
//
//   @Override
//   public String create(@NonNull Create createStatement) {
//      SQLObject object = createStatement.getObject();
//
//      if (object instanceof Table) {
//         return createTable(Cast.as(object), createStatement);
//      }
//      if (object instanceof SQLiteFullTextTable) {
//         return createFTable(Cast.as(object), createStatement);
//      }
//      if (object instanceof Index) {
//         return createIndex(Cast.as(object), createStatement);
//      }
//      if (object instanceof Trigger) {
//         return createTrigger(Cast.as(object), createStatement);
//      }
//
//      StringBuilder create = new StringBuilder("CREATE ");
//      create.append(object.getKeyword())
//            .append(" ");
//      if (createStatement.isIfNotExists()) {
//         create.append("IF NOT EXISTS ");
//      }
//      return create.toString();
//   }
//
//   private String createFTable(SQLiteFullTextTable table, Create createStatement) {
//      StringBuilder create = new StringBuilder("CREATE VIRTUAL TABLE ");
//      if (createStatement.isIfNotExists()) {
//         create.append("IF NOT EXISTS ");
//      }
//      return create.append(table.getName())
//                   .append(" USING fts4(content=")
//                   .append('"')
//                   .append(table.getOwner().getName())
//                   .append('"')
//                   .append(");").toString();
//   }
//
//   private String createIndex(Index index, Create createStatement) {
//      StringBuilder create = new StringBuilder("CREATE ");
//      if (index.isUnique()) {
//         create.append("UNIQUE ");
//      }
//      create.append("INDEX");
//      if (createStatement.isIfNotExists()) {
//         create.append(" IF NOT EXISTS ");
//      }
//      return create.append("\"")
//                   .append(index.getName())
//                   .append("\" on ")
//                   .append(toSQL(index.getTable()))
//                   .append(" ( ")
//                   .append(join(", ", index.getColumns()))
//                   .append(" );").toString();
//   }
//
//   private String createTable(Table table, Create createStatement) {
//      StringBuilder create = new StringBuilder("CREATE ");
//      if (table.getTableType() != null) {
//         create.append(table.getTableType()).append(" ");
//      }
//      create.append(table.getKeyword())
//            .append(" ");
//      if (createStatement.isIfNotExists()) {
//         create.append("IF NOT EXISTS ");
//      }
//      create.append(table.getName());
//      create.append(" (\n");
//      int i = 0;
//      for (Column c : table.getColumns()) {
//         if (i > 0) {
//            create.append(",\n");
//         }
//         i++;
//         create.append(columnDefinition(c));
//      }
//      if (table.getConstraints().size() > 0) {
//         for (TableConstraint constraint : table.getConstraints()) {
//            create.append(",\n ").append(constraint.toSQL(this));
//         }
//      }
//      create.append("\n);");
//      return create.toString();
//   }
//
//   private String createTrigger(Trigger trigger, Create createStatement) {
//      StringBuilder create = new StringBuilder("CREATE ");
//      if (trigger.isTemporary()) {
//         create.append("TEMP ");
//      }
//      create.append("TRIGGER ");
//      if (createStatement.isIfNotExists()) {
//         create.append("IF NOT EXISTS ");
//      }
//      create.append(trigger.getName())
//            .append(" ")
//            .append(trigger.getWhen().toSQL(this))
//            .append(" ")
//            .append(trigger.getOperation().toSQL(this))
//            .append(" ON ")
//            .append(toSQL(trigger.getTable()));
//      if (trigger.isRowLevelTrigger()) {
//         create.append(" FOR EACH ROW");
//      }
//      if (trigger.getWhere() != null) {
//         create.append(" WHERE ")
//               .append(toSQL(trigger.getWhere()));
//      }
//      return create.append(" BEGIN ")
//                   .append(trigger.getUpdateStatement().toSQL(this))
//                   .append("; END")
//                   .toString();
//
//   }
//
//   @Override
//   public String delete(@NonNull Delete delete) {
//      StringBuilder statement = new StringBuilder("DELETE FROM ")
//            .append(toSQL(delete.getTable()));
//      if (delete.getWhere() != null) {
//         statement.append(" WHERE ").append(toSQL(delete.getWhere()));
//      }
//      return statement.toString();
//   }
//
//   @Override
//   public String drop(@NonNull Drop drop) {
//      String statement = "DROP " + drop.getObject().getKeyword();
//      if (drop.isIfExists()) {
//         statement += " IF EXISTS";
//      }
//      return statement + " " + drop.getObject().getName() + ";";
//   }
//
//   @Override
//   public String getFunctionTemplate(@NonNull String function, int argSize) {
//      if (function.equalsIgnoreCase("AVG")) {
//         return "(AVG(${arg1}*${arg1}) - AVG(${arg1})*AVG(${arg1}))";
//      }
//      return SQLDialect.super.getFunctionTemplate(function, argSize);
//   }
//
//   @Override
//   public String insert(@NonNull Insert insert) {
//      StringBuilder insertStmt = new StringBuilder(insert.getInsertType().name().replace('_', ' '))
//            .append(" INTO ")
//            .append(toSQL(insert.getTable()))
//            .append(" ");
//      if (insert.getColumns().size() > 0) {
//         insertStmt.append("( ")
//                   .append(join(", ", insert.getColumns()))
//                   .append(" )");
//      }
//      if (insert.getSelect() != null) {
//         insertStmt.append(" ").append(insert.getSelect().toSQL(this));
//      } else if (insert.isDefaultValues()) {
//         insertStmt.append(" DEFAULT VALUES");
//      } else {
//         insertStmt.append(" VALUES( ")
//                   .append(join(", ", insert.getValues()))
//                   .append(" )");
//      }
//
//      if (insert.getOnConflict() != null) {
//         insertStmt.append(" ").append(upsertClause(insert.getOnConflict()));
//      }
//      return insertStmt.toString();
//   }
//
//   @Override
//   public String select(@NonNull Select select) {
//      StringBuilder statement = new StringBuilder("SELECT ");
//      if (select.isDistinct()) {
//         statement.append("DISTINCT ");
//
//      }
//      statement.append(join(", ", select.getColumns()))
//               .append("\nFROM ")
//               .append(join("\n", select.getFrom()));
//      if (select.getWhere() != null) {
//         statement.append("\nWHERE ").append(toSQL(select.getWhere()));
//      }
//      if (select.getGroupBy().size() > 0) {
//         statement.append("\nGROUP BY ").append(join(", ", select.getGroupBy()));
//      }
//      if (select.getHaving() != null) {
//         statement.append("\nHAVING ").append(toSQL(select.getHaving()));
//      }
//      if (select.getOrderBy().size() > 0) {
//         statement.append("\nORDER BY ").append(join(", ", select.getOrderBy()));
//      }
//      if (select.getLimit() != null) {
//         statement.append("\nLIMIT ").append(toSQL(select.getLimit()));
//      }
//      return statement.toString();
//   }
//
//   @Override
//   public String update(@NonNull Update update) {
//      StringBuilder statement = new StringBuilder(toSQL(update.getUpdateType()))
//            .append(" ")
//            .append(toSQL(update.getTable()))
//            .append(" SET ");
//      for (int i = 0; i < update.getColumns().size(); i++) {
//         if (i > 0) {
//            statement.append(", ");
//         }
//         statement.append(toSQL(update.getColumns().get(i)))
//                  .append(" = ")
//                  .append(toSQL(update.getValues().get(i)));
//      }
//      statement.append(" WHERE ");
//      if (update.getWhere() == null) {
//         statement.append("1 = 1");
//      } else {
//         statement.append(toSQL(update.getWhere()));
//      }
//      return statement.toString();
//   }
//
//   private String upsertClause(@NonNull UpsertClause upsertClause) {
//      StringBuilder statement = new StringBuilder("ON CONFLICT ( ")
//            .append(join(", ", upsertClause.getIndexedColumns()))
//            .append(" )");
//      if (upsertClause.getIndexedWhere() != null) {
//         statement.append(" WHERE ").append(toSQL(upsertClause.getIndexedWhere()));
//      }
//      statement.append(" DO ").append(toSQL(upsertClause.getAction()));
//      if (upsertClause.getAction() != UpsertAction.NOTHING) {
//         for (int i = 0; i < upsertClause.getUpdateColumns().size(); i++) {
//            if (i > 0) {
//               statement.append(",");
//            }
//            statement.append(" ");
//            statement.append(toSQL(upsertClause.getUpdateColumns().get(i)))
//                     .append(" = ")
//                     .append(toSQL(upsertClause.getUpdateValues().get(i)));
//         }
//         if (upsertClause.getUpdateWhere() != null) {
//            statement.append(" WHERE ").append(toSQL(upsertClause.getUpdateWhere()));
//         }
//      }
//
//      return statement.toString();
//   }
}//END OF SQLiteDialect
