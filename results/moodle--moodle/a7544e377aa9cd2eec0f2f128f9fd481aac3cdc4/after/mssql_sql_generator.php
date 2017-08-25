<?php // $Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.com                                            //
//                                                                       //
// Copyright (C) 1999 onwards Martin Dougiamas     http://dougiamas.com  //
//           (C) 2001-3001 Eloy Lafuente (stronk7) http://contiento.com  //
//                                                                       //
// This program is free software; you can redistribute it and/or modify  //
// it under the terms of the GNU General Public License as published by  //
// the Free Software Foundation; either version 2 of the License, or     //
// (at your option) any later version.                                   //
//                                                                       //
// This program is distributed in the hope that it will be useful,       //
// but WITHOUT ANY WARRANTY; without even the implied warranty of        //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
// GNU General Public License for more details:                          //
//                                                                       //
//          http://www.gnu.org/copyleft/gpl.html                         //
//                                                                       //
///////////////////////////////////////////////////////////////////////////

require_once($CFG->libdir.'/ddl/sql_generator.php');

/// This class generate SQL code to be used against MSSQL
/// It extends XMLDBgenerator so everything can be
/// overriden as needed to generate correct SQL.

class mssql_sql_generator extends sql_generator {

/// Only set values that are different from the defaults present in XMLDBgenerator

    public $statement_end = "\ngo"; // String to be automatically added at the end of each statement

    public $number_type = 'DECIMAL';    // Proper type for NUMBER(x) in this DB

    public $unsigned_allowed = false;    // To define in the generator must handle unsigned information
    public $default_for_char = '';      // To define the default to set for NOT NULLs CHARs without default (null=do nothing)

    public $specify_nulls = true;  //To force the generator if NULL clauses must be specified. It shouldn't be necessary
                                     //but some mssql drivers require them or everything is created as NOT NULL :-(

    public $sequence_extra_code = false; //Does the generator need to add extra code to generate the sequence fields
    public $sequence_name = 'IDENTITY(1,1)'; //Particular name for inline sequences in this generator
    public $sequence_only = false; //To avoid to output the rest of the field specs, leaving only the name and the sequence_name variable

    public $enum_inline_code = false; //Does the generator need to add inline code in the column definition

    public $add_table_comments  = false;  // Does the generator need to add code for table comments

    public $concat_character = '+'; //Characters to be used as concatenation operator. If not defined
                                  //MySQL CONCAT function will be use

    public $rename_table_sql = "sp_rename 'OLDNAME', 'NEWNAME'"; //SQL sentence to rename one table, both
                                  //OLDNAME and NEWNAME are dinamically replaced

    public $rename_column_sql = "sp_rename 'TABLENAME.OLDFIELDNAME', 'NEWFIELDNAME', 'COLUMN'";
                                      ///TABLENAME, OLDFIELDNAME and NEWFIELDNAME are dianmically replaced

    public $drop_index_sql = 'DROP INDEX TABLENAME.INDEXNAME'; //SQL sentence to drop one index
                                                               //TABLENAME, INDEXNAME are dinamically replaced

    public $rename_index_sql = "sp_rename 'TABLENAME.OLDINDEXNAME', 'NEWINDEXNAME', 'INDEX'"; //SQL sentence to rename one index
                                      //TABLENAME, OLDINDEXNAME, NEWINDEXNAME are dinamically replaced

    public $rename_key_sql = null; //SQL sentence to rename one key
                                          //TABLENAME, OLDKEYNAME, NEWKEYNAME are dinamically replaced

    /**
     * Creates one new XMLDBmssql
     */
    public function __construct($mdb) {
        parent::__construct($mdb);
    }

    /**
     * This function will create the temporary table passed as argument with all its
     * fields/keys/indexes/sequences, everything based in the XMLDB object
     *
     * TRUNCATE the table immediately after creation. A previous process using
     * the same persistent connection may have created the temp table and failed to
     * drop it. In that case, the table will exist, and create_temp_table() will
     * will succeed.
     *
     * NOTE: The return value is the tablename - some DBs (MSSQL at least) use special
     * names for temp tables.
     *
     * @uses $CFG, $db
     * @param xmldb_table table object (full specs are required)
     * @param boolean continue to specify if must continue on error (true) or stop (false)
     * @param boolean feedback to specify to show status info (true) or not (false)
     * @return string tablename on success, false on error
     */
    function create_temp_table($xmldb_table, $continue=true, $feedback=true) {
        if (!($xmldb_table instanceof xmldb_table)) {
            debugging('Incorrect create_table() $xmldb_table parameter');
            return false;
        }

    /// Check table doesn't exist
        if ($this->table_exists($xmldb_table)) {
            debugging('Table ' . $xmldb_table->getName() .
                      ' already exists. Create skipped', DEBUG_DEVELOPER);
            return $xmldb_table->getName(); //Table exists, nothing to do
        }

        if (!$sqlarr = $this->getCreateTableSQL($xmldb_table)) {
            return $xmldb_table->getName(); //Empty array = nothing to do = no error
        }

        // TODO: somehow change the name to have a #
        /*$temporary = '';

        if (!empty($temporary)) {
            $sqlarr = preg_replace('/^CREATE/', "CREATE $temporary", $sqlarr);
        }*/

        if (execute_sql_arr($sqlarr, $continue, $feedback)) {
            return $xmldb_table->getName();
        } else {
            return false;
        }
    }

    /**
     * Given one XMLDB Type, lenght and decimals, returns the DB proper SQL type
     */
    public function getTypeSQL($xmldb_type, $xmldb_length=null, $xmldb_decimals=null) {

        switch ($xmldb_type) {
            case XMLDB_TYPE_INTEGER:    // From http://msdn.microsoft.com/library/en-us/tsqlref/ts_da-db_7msw.asp?frame=true
                if (empty($xmldb_length)) {
                    $xmldb_length = 10;
                }
                if ($xmldb_length > 9) {
                    $dbtype = 'BIGINT';
                } else if ($xmldb_length > 4) {
                    $dbtype = 'INTEGER';
                } else {
                    $dbtype = 'SMALLINT';
                }
                break;
            case XMLDB_TYPE_NUMBER:
                $dbtype = $this->number_type;
                if (!empty($xmldb_length)) {
                /// 38 is the max allowed
                    if ($xmldb_length > 38) {
                        $xmldb_length = 38;
                    }
                    $dbtype .= '(' . $xmldb_length;
                    if (!empty($xmldb_decimals)) {
                        $dbtype .= ',' . $xmldb_decimals;
                    }
                    $dbtype .= ')';
                }
                break;
            case XMLDB_TYPE_FLOAT:
                $dbtype = 'FLOAT';
                if (!empty($xmldb_decimals)) {
                    if ($xmldb_decimals < 6) {
                        $dbtype = 'REAL';
                    }
                }
                break;
            case XMLDB_TYPE_CHAR:
                $dbtype = 'NVARCHAR';
                if (empty($xmldb_length)) {
                    $xmldb_length='255';
                }
                $dbtype .= '(' . $xmldb_length . ')';
                break;
            case XMLDB_TYPE_TEXT:
                $dbtype = 'NTEXT';
                break;
            case XMLDB_TYPE_BINARY:
                $dbtype = 'IMAGE';
                break;
            case XMLDB_TYPE_DATETIME:
                $dbtype = 'DATETIME';
                break;
        }
        return $dbtype;
    }

    /**
     * Returns the code needed to create one enum for the xmldb_table and xmldb_field passes
     */
    public function getEnumExtraSQL($xmldb_table, $xmldb_field) {

        $sql = 'CONSTRAINT ' . $this->getNameForObject($xmldb_table->getName(), $xmldb_field->getName(), 'ck');
        $sql.= ' CHECK (' . $this->getEncQuoted($xmldb_field->getName()) . ' IN (' . implode(', ', $xmldb_field->getEnumValues()) . '))';

        return $sql;
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to drop the field from the table
     * MSSQL overwrites the standard sentence because it needs to do some extra work dropping the default and
     * check constraints
     */
    public function getDropFieldSQL($xmldb_table, $xmldb_field) {

        global $db;

        $results = array();

    /// Get the quoted name of the table and field
        $tablename = $this->getTableName($xmldb_table);
        $fieldname = $this->getEncQuoted($xmldb_field->getName());

    /// Look for any default constraint in this field and drop it
        if ($defaultname = $this->getDefaultConstraintName($xmldb_table, $xmldb_field)) {
            $results[] = 'ALTER TABLE ' . $tablename . ' DROP CONSTRAINT ' . $defaultname;
        }

    /// Look for any check constraint in this field and drop it
        if ($drop_check = $this->getDropEnumSQL($xmldb_table, $xmldb_field)) {
            $results = array_merge($results, $drop_check);
        }

    /// Build the standard alter table drop column
        $results[] = 'ALTER TABLE ' . $tablename . ' DROP COLUMN ' . $fieldname;

        return $results;
    }

    /**
     * Given one correct xmldb_field and the new name, returns the SQL statements
     * to rename it (inside one array)
     * MSSQL is special, so we overload the function here. It needs to
     * drop the constraints BEFORE renaming the field
     */
    public function getRenameFieldSQL($xmldb_table, $xmldb_field, $newname) {

        $results = array();  //Array where all the sentences will be stored

    /// Although this is checked in ddllib - rename_field() - double check
    /// that we aren't trying to rename one "id" field. Although it could be
    /// implemented (if adding the necessary code to rename sequences, defaults,
    /// triggers... and so on under each getRenameFieldExtraSQL() function, it's
    /// better to forbide it, mainly because this field is the default PK and
    /// in the future, a lot of FKs can be pointing here. So, this field, more
    /// or less, must be considered inmutable!
        if ($xmldb_field->getName() == 'id') {
            return array();
        }

    /// Drop the check constraint if exists
        if ($xmldb_field->getEnum()) {
            $results = array_merge($results, $this->getDropEnumSQL($xmldb_table, $xmldb_field));
        }

    /// Call to standard (parent) getRenameFieldSQL() function
        $results = array_merge($results, parent::getRenameFieldSQL($xmldb_table, $xmldb_field, $newname));

        return $results;
    }

    /**
     * Returns the code (array of statements) needed to execute extra statements on field rename
     */
    public function getRenameFieldExtraSQL($xmldb_table, $xmldb_field, $newname) {

        $results = array();

    /// If the field is enum, drop and re-create the check constraint
        if ($xmldb_field->getEnum()) {
        /// Drop the current enum (not needed, it has been dropped before for msqql (in getRenameFieldSQL)
            //$results = array_merge($results, $this->getDropEnumSQL($xmldb_table, $xmldb_field));
        /// Change field name (over a clone to avoid some potential problems later)
            $new_xmldb_field = clone($xmldb_field);
            $new_xmldb_field->setName($newname);

        /// Recreate the enum
            $results = array_merge($results, $this->getCreateEnumSQL($xmldb_table, $new_xmldb_field));
        }

        return $results;
    }

    /**
     * Returns the code (array of statements) needed to execute extra statements on table rename
     */
    public function getRenameTableExtraSQL($xmldb_table, $newname) {

        $results = array();

        $newt = new xmldb_table($newname); //Temporal table for name calculations

        $oldtablename = $this->getTableName($xmldb_table);
        $newtablename = $this->getTableName($newt);

    /// Rename all the check constraints in the table
        $oldconstraintprefix = $this->getNameForObject($xmldb_table->getName(), '');
        $newconstraintprefix = $this->getNameForObject($newt->getName(), '', '');

        if ($constraints = $this->getCheckConstraintsFromDB($xmldb_table)) {
            foreach ($constraints as $constraint) {
            /// Drop the old constraint
                $results[] = 'ALTER TABLE ' . $newtablename . ' DROP CONSTRAINT ' . $constraint->name;
            /// Calculate the new constraint name
                $newconstraintname = str_replace($oldconstraintprefix, $newconstraintprefix, $constraint->name);
            /// Add the new constraint
                $results[] = 'ALTER TABLE ' . $newtablename . ' ADD CONSTRAINT ' . $newconstraintname .
                             ' CHECK ' . $constraint->description;
            }
        }

        return $results;
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to alter the field in the table
     */
    public function getAlterFieldSQL($xmldb_table, $xmldb_field) {

        $results = array(); /// To store all the needed SQL commands

    /// Get the quoted name of the table and field
        $tablename = $xmldb_table->getName();
        $fieldname = $xmldb_field->getName();

    /// Take a look to field metadata
        $meta = $this->mdb->get_columns($tablename, false);
        $metac = $meta[$fieldname];
        $oldmetatype = $metac->meta_type;

        $oldlength = $metac->max_length;
        $olddecimals = empty($metac->scale) ? null : $metac->scale;
        $oldnotnull = empty($metac->not_null) ? false : $metac->not_null;
        $olddefault = empty($metac->has_default) ? null : strtok($metac->default_value, ':');

        $typechanged = true;  //By default, assume that the column type has changed
        $lengthchanged = true;  //By default, assume that the column length has changed

    /// Detect if we are changing the type of the column
        if (($xmldb_field->getType() == XMLDB_TYPE_INTEGER && $oldmetatype == 'I') ||
            ($xmldb_field->getType() == XMLDB_TYPE_NUMBER  && $oldmetatype == 'N') ||
            ($xmldb_field->getType() == XMLDB_TYPE_FLOAT   && $oldmetatype == 'F') ||
            ($xmldb_field->getType() == XMLDB_TYPE_CHAR    && $oldmetatype == 'C') ||
            ($xmldb_field->getType() == XMLDB_TYPE_TEXT    && $oldmetatype == 'X') ||
            ($xmldb_field->getType() == XMLDB_TYPE_BINARY  && $oldmetatype == 'B')) {
            $typechanged = false;
        }

    /// Detect if we are changing the length of the column, not always necessary to drop defaults
    /// if only the length changes, but it's safe to do it always
        if ($xmldb_field->getLength() == $oldlength) {
            $lengthchanged = false;
        }

    /// If type or length have changed drop the default if exists
        if ($typechanged || $lengthchanged) {
            $results = $this->getDropDefaultSQL($xmldb_table, $xmldb_field);
        }

    /// Just prevent default clauses in this type of sentences for mssql and launch the parent one
        $results = array_merge($results, parent::getAlterFieldSQL($xmldb_table, $xmldb_field, NULL, true, NULL)); // Call parent

    /// Finally, process the default clause to add it back if necessary
        if ($typechanged || $lengthchanged) {
            $results = array_merge($results, $this->getCreateDefaultSQL($xmldb_table, $xmldb_field));
        }

    /// Return results
        return $results;
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to modify the default of the field in the table
     */
    public function getModifyDefaultSQL($xmldb_table, $xmldb_field) {
    /// MSSQL is a bit special with default constraints because it implements them as external constraints so
    /// normal ALTER TABLE ALTER COLUMN don't work to change defaults. Because this, we have this method overloaded here

        $results = array();

    /// Decide if we are going to create/modify or to drop the default
        if ($xmldb_field->getDefault() === null) {
            $results = $this->getDropDefaultSQL($xmldb_table, $xmldb_field); //Drop but, under some circumptances, re-enable
            $default_clause = $this->getDefaultClause($xmldb_field);
            if ($default_clause) { //If getDefaultClause() it must have one default, create it
                $results = array_merge($results, $this->getCreateDefaultSQL($xmldb_table, $xmldb_field)); //Create/modify
            }
        } else {
            $results = $this->getDropDefaultSQL($xmldb_table, $xmldb_field); //Drop (only if exists)
            $results = array_merge($results, $this->getCreateDefaultSQL($xmldb_table, $xmldb_field)); //Create/modify
        }

        return $results;
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to create its enum
     * (usually invoked from getModifyEnumSQL()
     */
    public function getCreateEnumSQL($xmldb_table, $xmldb_field) {
    /// All we have to do is to create the check constraint
        return array('ALTER TABLE ' . $this->getTableName($xmldb_table) .
                     ' ADD ' . $this->getEnumExtraSQL($xmldb_table, $xmldb_field));
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to drop its enum
     * (usually invoked from getModifyEnumSQL()
     */
    public function getDropEnumSQL($xmldb_table, $xmldb_field) {
    /// Let's introspect to know the real name of the check constraint
        if ($check_constraints = $this->getCheckConstraintsFromDB($xmldb_table, $xmldb_field)) {
            $check_constraint = array_shift($check_constraints); /// Get the 1st (should be only one)
            $constraint_name = strtolower($check_constraint->name); /// Extract the REAL name
        /// All we have to do is to drop the check constraint
            return array('ALTER TABLE ' . $this->getTableName($xmldb_table) .
                     ' DROP CONSTRAINT ' . $constraint_name);
        } else { /// Constraint not found. Nothing to do
            return array();
        }
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to create its default
     * (usually invoked from getModifyDefaultSQL()
     */
    public function getCreateDefaultSQL($xmldb_table, $xmldb_field) {
    /// MSSQL is a bit special and it requires the corresponding DEFAULT CONSTRAINT to be dropped

        $results = array();

    /// Get the quoted name of the table and field
        $tablename = $this->getTableName($xmldb_table);
        $fieldname = $this->getEncQuoted($xmldb_field->getName());

    /// Now, check if, with the current field attributes, we have to build one default
        $default_clause = $this->getDefaultClause($xmldb_field);
        if ($default_clause) {
        /// We need to build the default (Moodle) default, so do it
            $sql = 'ALTER TABLE ' . $tablename . ' ADD' . $default_clause . ' FOR ' . $fieldname;
            $results[] = $sql;
        }

        return $results;
    }

    /**
     * Given one xmldb_table and one xmldb_field, return the SQL statements needded to drop its default
     * (usually invoked from getModifyDefaultSQL()
     */
    public function getDropDefaultSQL($xmldb_table, $xmldb_field) {
    /// MSSQL is a bit special and it requires the corresponding DEFAULT CONSTRAINT to be dropped

        $results = array();

    /// Get the quoted name of the table and field
        $tablename = $this->getTableName($xmldb_table);
        $fieldname = $this->getEncQuoted($xmldb_field->getName());

    /// Look for the default contraint and, if found, drop it
        if ($defaultname = $this->getDefaultConstraintName($xmldb_table, $xmldb_field)) {
            $results[] = 'ALTER TABLE ' . $tablename . ' DROP CONSTRAINT ' . $defaultname;
        }

        return $results;
    }

    /**
     * Given one xmldb_table and one xmldb_field, returns the name of its default constraint in DB
     * or false if not found
     * This function should be considered internal and never used outside from generator
     */
    public function getDefaultConstraintName($xmldb_table, $xmldb_field) {

    /// Get the quoted name of the table and field
        $tablename = $this->getTableName($xmldb_table);
        $fieldname = $xmldb_field->getName();

    /// Look for any default constraint in this field and drop it
        if ($default = get_record_sql("SELECT id, object_name(cdefault) AS defaultconstraint
                                         FROM syscolumns
                                        WHERE id = object_id('{$tablename}')
                                          AND name = '{$fieldname}'")) {
            return $default->defaultconstraint;
        } else {
            return false;
        }
    }

    /**
     * Given one xmldb_table returns one array with all the check constrainsts
     * in the table (fetched from DB)
     * Optionally the function allows one xmldb_field to be specified in
     * order to return only the check constraints belonging to one field.
     * Each element contains the name of the constraint and its description
     * If no check constraints are found, returns an empty array
     */
    public function getCheckConstraintsFromDB($xmldb_table, $xmldb_field = null) {

        $results = array();

        $tablename = $this->getTableName($xmldb_table);

        if ($constraints = get_records_sql("SELECT o.name, c.text AS description
                                            FROM sysobjects o,
                                                 sysobjects p,
                                                 syscomments c
                                           WHERE p.id = o.parent_obj
                                             AND o.id = c.id
                                             AND o.xtype = 'C'
                                             AND p.name = '{$tablename}'")) {
            foreach ($constraints as $constraint) {
                $results[$constraint->name] = $constraint;
            }
        }

    /// Filter by the required field if specified
        if ($xmldb_field) {
            $filtered_results = array();
            $filter = $xmldb_field->getName();
        /// Lets clean a bit each constraint description, looking for the filtered field
            foreach ($results as $key => $result) {
                $description = trim(preg_replace('/[\(\)]/', '',  $result->description));   // Parenthesis out & trim
                /// description starts by [$filter] assume it's a constraint beloging to the field
                if (preg_match("/^\[{$filter}\]/i", $description)) {
                    $filtered_results[$key] = $result;
                }
            }
        /// Assign filtered results to the final results array
            $results =  $filtered_results;
        }

        return $results;
    }

    /**
     * Given one object name and it's type (pk, uk, fk, ck, ix, uix, seq, trg)
     * return if such name is currently in use (true) or no (false)
     * (invoked from getNameForObject()
     */
    public function isNameInUse($object_name, $type, $table_name) {
        switch($type) {
            case 'seq':
            case 'trg':
            case 'pk':
            case 'uk':
            case 'fk':
            case 'ck':
                if ($check = get_records_sql("SELECT name
                                              FROM sysobjects
                                              WHERE lower(name) = '" . strtolower($object_name) . "'")) {
                    return true;
                }
                break;
            case 'ix':
            case 'uix':
                if ($check = get_records_sql("SELECT name
                                              FROM sysindexes
                                              WHERE lower(name) = '" . strtolower($object_name) . "'")) {
                    return true;
                }
                break;
        }
        return false; //No name in use found
    }

    /**
     * Returns the code (in array) needed to add one comment to the table
     */
    public function getCommentSQL($xmldb_table) {
        return array();
    }

    public function addslashes($s) {
        // do not use php addslashes() because it depends on PHP quote settings!
        $s = str_replace("'",  "''", $s);
        return $s;
    }

    /**
     * Returns an array of reserved words (lowercase) for this DB
     */
    public static function getReservedWords() {
    /// This file contains the reserved words for MSSQL databases
    /// from http://msdn2.microsoft.com/en-us/library/ms189822.aspx
        $reserved_words = array (
            'add', 'all', 'alter', 'and', 'any', 'as', 'asc', 'authorization',
            'avg', 'backup', 'begin', 'between', 'break', 'browse', 'bulk',
            'by', 'cascade', 'case', 'check', 'checkpoint', 'close', 'clustered',
            'coalesce', 'collate', 'column', 'commit', 'committed', 'compute',
            'confirm', 'constraint', 'contains', 'containstable', 'continue',
            'controlrow', 'convert', 'count', 'create', 'cross', 'current',
            'current_date', 'current_time', 'current_timestamp', 'current_user',
            'cursor', 'database', 'dbcc', 'deallocate', 'declare', 'default', 'delete',
            'deny', 'desc', 'disk', 'distinct', 'distributed', 'double', 'drop', 'dummy',
            'dump', 'else', 'end', 'errlvl', 'errorexit', 'escape', 'except', 'exec',
            'execute', 'exists', 'exit', 'external', 'fetch', 'file', 'fillfactor', 'floppy',
            'for', 'foreign', 'freetext', 'freetexttable', 'from', 'full', 'function',
            'goto', 'grant', 'group', 'having', 'holdlock', 'identity', 'identitycol',
            'identity_insert', 'if', 'in', 'index', 'inner', 'insert', 'intersect', 'into',
            'is', 'isolation', 'join', 'key', 'kill', 'left', 'level', 'like', 'lineno',
            'load', 'max', 'min', 'mirrorexit', 'national', 'nocheck', 'nonclustered',
            'not', 'null', 'nullif', 'of', 'off', 'offsets', 'on', 'once', 'only', 'open',
            'opendatasource', 'openquery', 'openrowset', 'openxml', 'option', 'or', 'order',
            'outer', 'over', 'percent', 'perm', 'permanent', 'pipe', 'pivot', 'plan', 'precision',
            'prepare', 'primary', 'print', 'privileges', 'proc', 'procedure', 'processexit',
            'public', 'raiserror', 'read', 'readtext', 'reconfigure', 'references',
            'repeatable', 'replication', 'restore', 'restrict', 'return', 'revoke',
            'right', 'rollback', 'rowcount', 'rowguidcol', 'rule', 'save', 'schema',
            'select', 'serializable', 'session_user', 'set', 'setuser', 'shutdown', 'some',
            'statistics', 'sum', 'system_user', 'table', 'tape', 'temp', 'temporary',
            'textsize', 'then', 'to', 'top', 'tran', 'transaction', 'trigger', 'truncate',
            'tsequal', 'uncommitted', 'union', 'unique', 'update', 'updatetext', 'use',
            'user', 'values', 'varying', 'view', 'waitfor', 'when', 'where', 'while',
            'with', 'work', 'writetext'
        );
        return $reserved_words;
    }
}

?>