package com.gengoai.sql.constraint;


import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ConstraintWithConflictClause<T extends ConstraintWithConflictClause<T>> extends Constraint {
   private static final long serialVersionUID = 1L;
   private ConflictClause conflictClause;

   public ConstraintWithConflictClause() {
   }

   public ConstraintWithConflictClause(String name) {
      super(name);
   }

   public T onConflict(ConflictClause conflictClause) {
      this.conflictClause = conflictClause;
      return Cast.as(this);
   }

   @Override
   public String toString() {
      if (Strings.isNotNullOrBlank(name)) {
         return getClass().getSimpleName() +
               "{" +
               "name='" + name + '\'' +
               '}';
      }
      return getClass().getSimpleName() + "{}";
   }

}//END OF ConstraintWithConflictClause
