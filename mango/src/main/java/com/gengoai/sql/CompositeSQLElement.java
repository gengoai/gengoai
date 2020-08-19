package com.gengoai.sql;

import com.gengoai.sql.operator.SQLOperable;
import lombok.NonNull;

/**
 * An {@link SQLElement} that is made up of one more sub elements. Rendering is handled by passing the dialect into a
 * custom {@link #render(SQLDialect)} method, which provide custom logic for rending the SQL.
 */
public interface CompositeSQLElement extends SQLOperable {

   /**
    * Renders this element as valid SQL in the given dialect.
    *
    * @param dialect2 the dialect to use for rendering the SQL
    * @return the rendered SQL
    */
   String render(@NonNull SQLDialect dialect2);

}//END OF CompositeSQLElement
