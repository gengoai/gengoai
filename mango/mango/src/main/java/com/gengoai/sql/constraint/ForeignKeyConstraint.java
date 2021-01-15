package com.gengoai.sql.constraint;

import com.gengoai.Validation;
import com.gengoai.sql.SQLElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ForeignKeyConstraint extends Constraint {
   private static final long serialVersionUID = 1L;
   protected final List<SQLElement> columns;
   protected final List<SQLElement> foreignTableColumns;
   protected final SQLElement foreignTable;
   protected ForeignKeyAction onUpdate;
   protected ForeignKeyAction onDelete;
   protected Deferrable deferred = Deferrable.NOT_DEFERRABLE;


   public ForeignKeyConstraint(String name,
                               @NonNull Collection<SQLElement> columns,
                               @NonNull SQLElement foreignTable,
                               @NonNull Collection<SQLElement> foreignTableColumns) {
      super(name);
      this.foreignTable = foreignTable;
      Validation.checkArgument(columns.size() > 0, "Must specify at least 1 table column.");
      this.columns = new ArrayList<>(columns);
      Validation.checkArgument(foreignTableColumns.size() > 0, "Must specify at least 1 foreign table column.");
      this.foreignTableColumns = new ArrayList<>(foreignTableColumns);
   }

   public List<SQLElement> getColumns() {
      return Collections.unmodifiableList(columns);
   }

   public List<SQLElement> getForeignTableColumns() {
      return Collections.unmodifiableList(foreignTableColumns);
   }

   public ForeignKeyConstraint initiallyDeferred() {
      this.deferred = Deferrable.INITIALLY_DEFERRED;
      return this;
   }

   public ForeignKeyConstraint initiallyImmediateDeferred() {
      this.deferred = Deferrable.INITIALLY_IMMEDIATE;
      return this;
   }

   public ForeignKeyConstraint notDeferred() {
      this.deferred = Deferrable.NOT_DEFERRABLE;
      return this;
   }

   public ForeignKeyConstraint onDelete(ForeignKeyAction action) {
      this.onDelete = action;
      return this;
   }

   public ForeignKeyConstraint onUpdate(ForeignKeyAction action) {
      this.onUpdate = action;
      return this;
   }

}//END OF ForeignKeyConstraint
