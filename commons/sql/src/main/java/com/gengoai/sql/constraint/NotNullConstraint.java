package com.gengoai.sql.constraint;

import com.gengoai.sql.SQLElement;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class NotNullConstraint extends ConstraintWithConflictClause<NotNullConstraint> {
    @Getter
    private final SQLElement column;

    public NotNullConstraint(String name, SQLElement column) {
        super(name);
        this.column = column;
    }

}//END OF NotNullConstraint
