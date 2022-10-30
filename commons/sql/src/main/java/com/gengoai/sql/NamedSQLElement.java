package com.gengoai.sql;

/**
 * An {@link SQLElement} that represents a named object.
 */
public interface NamedSQLElement extends SQLElement {

   /**
    * Gets the name of the object.
    *
    * @return the name of the object
    */
   String getName();


}//END OF NamedSQLElement
