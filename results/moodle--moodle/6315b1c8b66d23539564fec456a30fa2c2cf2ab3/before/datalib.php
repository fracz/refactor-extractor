<?php

/**
 * Library of functions for database manipulation.
 *
 * Other main libraries:
 * - weblib.php - functions that produce web output
 * - moodlelib.php - general-purpose Moodle functions
 * @author Martin Dougiamas and many others
 * @version $Id$
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package moodlecore
 */


/// GLOBAL CONSTANTS /////////////////////////////////////////////////////////

if ($SITE = get_site()) {
    /**
     * If $SITE global from {@link get_site()} is set then SITEID to $SITE->id, otherwise set to 1.
     */
    define('SITEID', $SITE->id);
} else {
    /**
     * @ignore
     */
    define('SITEID', 1);
}

/// FUNCTIONS FOR DATABASE HANDLING  ////////////////////////////////

/**
 * Execute a given sql command string
 *
 * Completely general function - it just runs some SQL and reports success.
 *
 * @uses $db
 * @param string $command The sql string you wish to be executed.
 * @param boolean $feedback Set this argument to true if the results generated should be printed. Default is true.
 * @return string
 */
function execute_sql($command, $feedback=true) {
/// Completely general function - it just runs some SQL and reports success.

    global $db;

    $result = $db->Execute($command);

    if ($result) {
        if ($feedback) {
            echo '<p><font color="green"><strong>'. get_string('success') .'</strong></font></p>';
        }
        return true;
    } else {
        if ($feedback) {
            echo '<p><font color="red"><strong>'. get_string('error') .'</strong></font></p>';
        }
        return false;
    }
}
/**
* on DBs that support it, switch to transaction mode and begin a transaction
* you'll need to ensure you call commit_sql() or your changes *will* be lost
* this is _very_ useful for massive updates
*/
function begin_sql() {
/// Completely general function - it just runs some SQL and reports success.

    global $CFG;
    if ($CFG->dbtype === 'postgres7') {
        return execute_sql('BEGIN', false);
    }
    return true;
}
/**
* on DBs that support it, commit the transaction
*/
function commit_sql() {
/// Completely general function - it just runs some SQL and reports success.

    global $CFG;
    if ($CFG->dbtype === 'postgres7') {
        return execute_sql('COMMIT', false);
    }
    return true;
}

/**
 * Run an arbitrary sequence of semicolon-delimited SQL commands
 *
 * Assumes that the input text (file or string) consists of
 * a number of SQL statements ENDING WITH SEMICOLONS.  The
 * semicolons MUST be the last character in a line.
 * Lines that are blank or that start with "#" are ignored.
 * Only tested with mysql dump files (mysqldump -p -d moodle)
 *
 * @uses $CFG
 * @param string $sqlfile The path where a file with sql commands can be found on the server.
 * @param string $sqlstring If no path is supplied then a string with semicolon delimited sql
 * commands can be supplied in this argument.
 * @return boolean Returns true if databse was modified successfully.
 */
function modify_database($sqlfile='', $sqlstring='') {

    global $CFG;

    $success = true;  // Let's be optimistic

    if (!empty($sqlfile)) {
        if (!is_readable($sqlfile)) {
            $success = false;
            echo '<p>Tried to modify database, but "'. $sqlfile .'" doesn\'t exist!</p>';
            return $success;
        } else {
            $lines = file($sqlfile);
        }
    } else {
        $lines[] = $sqlstring;
    }

    $command = '';

    foreach ($lines as $line) {
        $line = rtrim($line);
        $length = strlen($line);

        if ($length and $line[0] <> '#') {
            if (substr($line, $length-1, 1) == ';') {
                $line = substr($line, 0, $length-1);   // strip ;
                $command .= $line;
                $command = str_replace('prefix_', $CFG->prefix, $command); // Table prefixes
                if (! execute_sql($command)) {
                    $success = false;
                }
                $command = '';
            } else {
                $command .= $line;
            }
        }
    }

    return $success;

}

/// FUNCTIONS TO MODIFY TABLES ////////////////////////////////////////////

/**
 * Add a new field to a table, or modify an existing one (if oldfield is defined).
 *
 * @uses $CFG
 * @uses $db
 * @param string $table ?
 * @param string $oldfield ?
 * @param string $field ?
  * @param string $type ?
 * @param string $size ?
 * @param string $signed ?
 * @param string $default ?
 * @param string $null ?
 * @todo Finish documenting this function
 */

function table_column($table, $oldfield, $field, $type='integer', $size='10',
                      $signed='unsigned', $default='0', $null='not null', $after='') {
    global $CFG, $db;

    switch (strtolower($CFG->dbtype)) {

        case 'mysql':
        case 'mysqlt':

            switch (strtolower($type)) {
                case 'text':
                    $type = 'TEXT';
                    $signed = '';
                    break;
                case 'integer':
                    $type = 'INTEGER('. $size .')';
                    break;
                case 'varchar':
                    $type = 'VARCHAR('. $size .')';
                    $signed = '';
                    break;
            }

            if (!empty($oldfield)) {
                $operation = 'CHANGE '. $oldfield .' '. $field;
            } else {
                $operation = 'ADD '. $field;
            }

            $default = 'DEFAULT \''. $default .'\'';

            if (!empty($after)) {
                $after = 'AFTER `'. $after .'`';
            }

            return execute_sql('ALTER TABLE '. $CFG->prefix . $table .' '. $operation .' '. $type .' '. $signed .' '. $default .' '. $null .' '. $after);
            break;

        case 'postgres7':        // From Petri Asikainen
            //Check db-version
            $dbinfo = $db->ServerInfo();
            $dbver = substr($dbinfo['version'],0,3);

            //to prevent conflicts with reserved words
            $realfield = '"'. $field .'"';
            $field = '"'. $field .'_alter_column_tmp"';
            $oldfield = '"'. $oldfield .'"';

            switch (strtolower($type)) {
                case 'integer':
                    if ($size <= 4) {
                        $type = 'INT2';
                    }
                    if ($size <= 10) {
                        $type = 'INT';
                    }
                    if  ($size > 10) {
                        $type = 'INT8';
                    }
                    break;
                case 'varchar':
                    $type = 'VARCHAR('. $size .')';
                    break;
            }

            $default = '\''. $default .'\'';

            //After is not implemented in postgesql
            //if (!empty($after)) {
            //    $after = "AFTER '$after'";
            //}

            //Use transactions
            execute_sql('BEGIN');

            //Allways use temporaly column
            execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ADD COLUMN '. $field .' '. $type);
            //Add default values
            execute_sql('UPDATE '. $CFG->prefix . $table .' SET '. $field .'='. $default);


            if ($dbver >= '7.3') {
                // modifying 'not null' is posible before 7.3
                //update default values to table
                if ($null == 'NOT NULL') {
                    execute_sql('UPDATE '. $CFG->prefix . $table .' SET '. $field .'='. $default .' WHERE '. $field .' IS NULL');
                    execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ALTER COLUMN '. $field .' SET '. $null);
                } else {
                    execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ALTER COLUMN '. $field .' DROP NOT NULL');
                }
            }

            execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ALTER COLUMN '. $field .' SET DEFAULT '. $default);

            if ( $oldfield != '""' ) {
                execute_sql('UPDATE '. $CFG->prefix . $table .' SET '. $field .' = '. $oldfield);
                execute_sql('ALTER TABLE  '. $CFG->prefix . $table .' DROP COLUMN '. $oldfield);
            }

            execute_sql('ALTER TABLE '. $CFG->prefix . $table .' RENAME COLUMN '. $field .' TO '. $realfield);

            return execute_sql('COMMIT');
            break;

        default:
            switch (strtolower($type)) {
                case 'integer':
                    $type = 'INTEGER';
                    break;
                case 'varchar':
                    $type = 'VARCHAR';
                    break;
            }

            $default = 'DEFAULT \''. $default .'\'';

            if (!empty($after)) {
                $after = 'AFTER '. $after;
            }

            if (!empty($oldfield)) {
                execute_sql('ALTER TABLE '. $CFG->prefix . $table .' RENAME COLUMN '. $oldfield .' '. $field);
            } else {
                execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ADD COLUMN '. $field .' '. $type);
            }

            execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ALTER COLUMN '. $field .' SET '. $null);
            return execute_sql('ALTER TABLE '. $CFG->prefix . $table .' ALTER COLUMN '. $field .' SET '. $default);
            break;

    }
}



/// GENERIC FUNCTIONS TO CHECK AND COUNT RECORDS ////////////////////////////////////////

/**
 * Returns true or false depending on whether the specified record exists
 *
 * @uses $CFG
 * @param string $table The database table to be checked for the record.
 * @param string $field1 The first table field to be checked for a given value. Do not supply a field1 string to leave out a WHERE clause altogether.
 * @param string $value1 The value to match if field1 is specified.
 * @param string $field2 The second table field to be checked for a given value.
 * @param string $value2 The value to match if field2 is specified.
 * @param string $field3 The third table field to be checked for a given value.
 * @param string $value3 The value to match if field3 is specified.
 * @return boolean True is record exists
 */
function record_exists($table, $field1='', $value1='', $field2='', $value2='', $field3='', $value3='') {

    global $CFG;

    if ($field1) {
        $select = 'WHERE '. $field1 .' = \''. $value1 .'\'';
        if ($field2) {
            $select .= ' AND '. $field2 .' = \''. $value2 .'\'';
            if ($field3) {
                $select .= ' AND '. $field3 .' = \''. $value3 .'\'';
            }
        }
    } else {
        $select = '';
    }

    return record_exists_sql('SELECT * FROM '. $CFG->prefix . $table .' '. $select .' LIMIT 1');
}


/**
* Determine whether a specified record exists.
*
* This function returns true if the SQL executed returns records.
*
* @uses $CFG
* @uses $db
* @param string $sql The SQL statement to be executed.
* @return boolean
*/
function record_exists_sql($sql) {

    global $CFG, $db;

    if (!$rs = $db->Execute($sql)) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg().'<br /><br />'.$sql);
        }
        return false;
    }

    if ( $rs->RecordCount() ) {
        return true;
    } else {
        return false;
    }
}


/**
 * Get all specified records from the specified table and return the count of them
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $field1 The first table field to be checked for a given value. Do not supply a field1 string to leave out a WHERE clause altogether.
 * @param string $value1 The value to match if field1 is specified.
 * @param string $field2 The second table field to be checked for a given value.
 * @param string $value2 The value to match if field2 is specified.
 * @param string $field3 The third table field to be checked for a given value.
 * @param string $value3 The value to match if field3 is specified.
 * @return int The count of records returned from the specified criteria.
 */
function count_records($table, $field1='', $value1='', $field2='', $value2='', $field3='', $value3='') {

    global $CFG;

    if ($field1) {
        $select = 'WHERE '. $field1 .' = \''. $value1 .'\'';
        if ($field2) {
            $select .= ' AND '. $field2 .' = \''. $value2 .'\'';
            if ($field3) {
                $select .= ' AND '. $field3 .' = \''. $value3 .'\'';
            }
        }
    } else {
        $select = '';
    }

    return count_records_sql('SELECT COUNT(*) FROM '. $CFG->prefix . $table .' '. $select);
}

/**
 * Get all the records and count them
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
 * @param string $countitem The count string to be used in the SQL call. Default is COUNT(*).
 * @return int The count of records returned from the specified criteria.
 */
function count_records_select($table, $select='', $countitem='COUNT(*)') {

    global $CFG;

    if ($select) {
        $select = 'WHERE '.$select;
    }

    return count_records_sql('SELECT '. $countitem .' FROM '. $CFG->prefix . $table .' '. $select);
}


/**
 * Get all the records returned from the specified SQL call and return the count of them
 *
 * @uses $CFG
 * @uses $db
 * @param string $sql The SQL string you wish to be executed.
 * @return int The count of records returned from the specified SQL string.
 */
function count_records_sql($sql) {

    global $CFG, $db;

    $rs = $db->Execute($sql);
    if (!$rs) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />'. $sql);
        }
        return 0;
    }

    return $rs->fields[0];
}




/// GENERIC FUNCTIONS TO GET, INSERT, OR UPDATE DATA  ///////////////////////////////////

/**
 * Get a single record as an object
 *
 * @uses $CFG
 * @param    string  $table The name of the table to select from
 * @param    string  $field1 The name of the field for the first criteria
 * @param    string  $value1 The value of the field for the first criteria
 * @param    string  $field2 The name of the field for the second criteria
 * @param    string  $value2 The value of the field for the second criteria
 * @param    string  $field3 The name of the field for the third criteria
 * @param    string  $value3 The value of the field for the third criteria
 * @return   object(fieldset) A fieldset object containing the first record selected
 * @todo Isn't the return from this function an associative array (array type) ?
 */
function get_record($table, $field1, $value1, $field2='', $value2='', $field3='', $value3='') {

    global $CFG ;

    $select = 'WHERE '. $field1 .' = \''. $value1 .'\'';

    if ($field2) {
        $select .= ' AND '. $field2 .' = \''. $value2 .'\'';
        if ($field3) {
            $select .= ' AND '. $field3 .' = \''. $value3 .'\'';
        }
    }

    return get_record_sql('SELECT * FROM '. $CFG->prefix . $table .' '. $select);
}

/**
 * Get a single record as an object using the specified SQL statement
 *
 * A LIMIT is normally added to only look for 1 record
 *
 * @uses $CFG
 * @uses $db
 * @param string $sql The SQL string you wish to be executed.
 * @return boolean
 * @todo include code example of SQL calls including using LIMIT for only 1 record
 */
function get_record_sql($sql) {

    global $db, $CFG;

    if (isset($CFG->debug) and $CFG->debug > 7) {    // Debugging mode - don't use limit
       $limit = '';
    } else {
       $limit = ' LIMIT 1';    // Workaround - limit to one record
    }

    if (!$rs = $db->Execute($sql . $limit)) {
        if (isset($CFG->debug) and $CFG->debug > 7) {    // Debugging mode - print checks
            notify( $db->ErrorMsg() . '<br /><br />'. $sql . $limit );
        }
        return false;
    }

    if (!$recordcount = $rs->RecordCount()) {
        return false;                 // Found no records
    }

    if ($recordcount == 1) {          // Found one record
        return (object)$rs->fields;

    } else {                          // Error: found more than one record
        notify('Error:  Turn off debugging to hide this error.');
        notify($sql . $limit);
        if ($records = $rs->GetAssoc(true)) {
            notify('Found more than one record in get_record_sql !');
            print_object($records);
        } else {
            notify('Very strange error in get_record_sql !');
            print_object($rs);
        }
        print_continue($CFG->wwwroot .'/admin/config.php');
    }
}

/**
 * Gets one record from a table, as an object
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array An associative array with the results from the SQL call.
 */
function get_record_select($table, $select='', $fields='*') {

    global $CFG;

    if ($select) {
        $select = 'WHERE '. $select;
    }

    return get_record_sql('SELECT '. $fields .' FROM '. $CFG->prefix . $table .' '. $select);
}


/**
 * Get a number of records as an array of objects
 *
 * Can optionally be sorted eg "time ASC" or "time DESC"
 * If "fields" is specified, only those fields are returned
 * The "key" is the first column returned, eg usually "id"
 * limitfrom and limitnum must both be specified or not at all
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
  * @param string $field ?
  * @param string $value ?
  * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @param int $limitfrom ?
 * @param int $limitnum ?
 * @return array|false An associative array with the results from the SQL call or false if error.
 * @todo Finish documenting this function
 */
function get_records($table, $field='', $value='', $sort='', $fields='*', $limitfrom='', $limitnum='') {

    global $CFG;

    if ($field) {
        $select = 'WHERE '. $field .' = \''. $value .'\'';
    } else {
        $select = '';
    }

    if ($limitfrom !== '') {
        switch ($CFG->dbtype) {
            case 'mysql':
                 $limit = 'LIMIT '. $limitfrom .','. $limitnum;
                 break;
            case 'postgres7':
                 $limit = 'LIMIT '. $limitnum .' OFFSET '. $limitfrom;
                 break;
            default:
                 $limit = 'LIMIT '. $limitnum .','. $limitfrom;
        }
    } else {
        $limit = '';
    }

    if ($sort) {
        $sort = 'ORDER BY '. $sort;
    }

    return get_records_sql('SELECT '. $fields .' FROM '. $CFG->prefix . $table .' '. $select .' '. $sort .' '. $limit);
}

/**
 * Get a number of records as an array of objects
 *
 * Can optionally be sorted eg "time ASC" or "time DESC"
 * "select" is a fragment of SQL to define the selection criteria
 * The "key" is the first column returned, eg usually "id"
 * limitfrom and limitnum must both be specified or not at all
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
 * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @param int $limitfrom ?
 * @param int $limitnum ?
 * @return array|false An associative array with the results from the SQL call or false if error.
 * @todo Finish documenting this function
 */
function get_records_select($table, $select='', $sort='', $fields='*', $limitfrom='', $limitnum='') {

    global $CFG;

    if ($select) {
        $select = 'WHERE '. $select;
    }

    if ($limitfrom !== '') {
        switch ($CFG->dbtype) {
            case 'mysql':
                 $limit = 'LIMIT '. $limitfrom .','. $limitnum;
                 break;
            case 'postgres7':
                 $limit = 'LIMIT '. $limitnum .' OFFSET '. $limitfrom;
                 break;
            default:
                 $limit = 'LIMIT '. $limitnum .','. $limitfrom;
        }
    } else {
        $limit = '';
    }

    if ($sort) {
        $sort = 'ORDER BY '. $sort;
    }

    return get_records_sql('SELECT '. $fields .' FROM '. $CFG->prefix . $table .' '. $select .' '. $sort .' '. $limit);
}


/**
 * Get a number of records as an array of objects
 *
 * Differs from get_records() in that the values variable
 * can be a comma-separated list of values eg  "4,5,6,10"
 * Can optionally be sorted eg "time ASC" or "time DESC"
 * The "key" is the first column returned, eg usually "id"
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $field ?
 * @param string $values ?
 * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array|false An associative array with the results from the SQL call or false if error.
 * @todo Finish documenting this function
 */
function get_records_list($table, $field='', $values='', $sort='', $fields='*') {

    global $CFG;

    if ($field) {
        $select = 'WHERE '. $field .' IN ( '. $values .')';
    } else {
        $select = '';
    }

    if ($sort) {
        $sort = 'ORDER BY '. $sort;
    }

    return get_records_sql('SELECT '. $fields .' FROM '. $CFG->prefix . $table .' '. $select .' '. $sort);
}



/**
 * Get a number of records as an array of objects
 *
 * The "key" is the first column returned, eg usually "id"
 * The sql statement is provided as a string.
 *
 * @uses $CFG
 * @uses $db
 * @param string $sql The SQL string you wish to be executed.
 * @return array|false Returns an associative array with found records or false if no records or error occured.
 */
function get_records_sql($sql) {

    global $CFG, $db;

    if (!$rs = $db->Execute($sql)) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />'. $sql);
        }
        return false;
    }

    if ( $rs->RecordCount() > 0 ) {
        if ($records = $rs->GetAssoc(true)) {
            foreach ($records as $key => $record) {
                $objects[$key] = (object) $record;
            }
            return $objects;
        } else {
            return false;
        }
    } else {
        return false;
    }
}

/**
* Get a number of records as an array of objects
*
* Can optionally be sorted eg "time ASC" or "time DESC"
* If "fields" is specified, only those fields are returned
* The "key" is the first column returned, eg usually "id"
*
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $field ?
 * @param string $value ?
 * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array An associative array with the results from the SQL call.
* @todo Finish documenting this function
*/
function get_records_menu($table, $field='', $value='', $sort='', $fields='*') {

    global $CFG;

    if ($field) {
        $select = 'WHERE '. $field .' = \''. $value .'\'';
    } else {
        $select = '';
    }

    if ($sort) {
        $sort = 'ORDER BY '. $sort;
    }

    return get_records_sql_menu('SELECT '. $fields .' FROM '. $CFG->prefix . $table .' '. $select .' '. $sort);
}


/**
 * Get a number of records as an array of values
 *
 * Can optionally be sorted eg "time ASC" or "time DESC"
 * "select" is a fragment of SQL to define the selection criteria
 * Returns associative array of first two fields
 *
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
 * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array An associative array with the results from the SQL call.
 * @todo Finish documenting this function
 */
function get_records_select_menu($table, $select='', $sort='', $fields='*') {

    global $CFG;

    if ($select) {
        $select = 'WHERE '. $select;
    }

    if ($sort) {
        $sort = 'ORDER BY '. $sort;
    }

    return get_records_sql_menu('SELECT '. $fields .' FROM '. $CFG->prefix . $table .' '. $select .' '. $sort);
}


/**
 * Retrieve an array of the first two columns returned from a SQL statment.
 *
 * Given a SQL statement, this function returns an associative
 * array of the first two columns.  This is most useful in
 * combination with the {@link choose_from_menu()} function to create
 * a form menu.
 *
 * @uses $CFG
 * @uses $db
 * @param string $sql The SQL string you wish to be executed.
 * @return boolean
 * @todo Finish documenting this function
 */
function get_records_sql_menu($sql) {

    global $CFG, $db;

    if (!$rs = $db->Execute($sql)) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />'. $sql);
        }
        return false;
    }

    if ( $rs->RecordCount() > 0 ) {
        while (!$rs->EOF) {
            $menu[$rs->fields[0]] = $rs->fields[1];
            $rs->MoveNext();
        }
        return $menu;

    } else {
        return false;
    }
}

/**
 * Get a single field from a database record
 *
 * @uses $CFG
 * @uses $db
 * @param string $table The database table to be checked against.
 * @param string $field1 The first table field to be checked for a given value. Do not supply a field1 string to leave out a WHERE clause altogether.
 * @param string $value1 The value to match if field1 is specified.
 * @param string $field2 The second table field to be checked for a given value.
 * @param string $value2 The value to match if field2 is specified.
 * @param string $field3 The third table field to be checked for a given value.
 * @param string $value3 The value to match if field3 is specified.
 * @return mixed|false Returns the value return from the SQL statment or false if an error occured.
 * @todo Finish documenting this function
 */
function get_field($table, $return, $field1, $value1, $field2='', $value2='', $field3='', $value3='') {

    global $db, $CFG;

    $select = 'WHERE '. $field1 .' = \''. $value1 .'\'';

    if ($field2) {
        $select .= ' AND '. $field2 .' = \''. $value2 .'\'';
        if ($field3) {
            $select .= ' AND '. $field3 .' = \''. $value3 .'\'';
        }
    }

    $rs = $db->Execute('SELECT '. $return .' FROM '. $CFG->prefix . $table .' '. $select);
    if (!$rs) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />SELECT '. $return .' FROM '. $CFG->prefix . $table .' '. $select);
        }
        return false;
    }

    if ( $rs->RecordCount() == 1 ) {
        return $rs->fields[$return];
    } else {
        return false;
    }
}


/**
 * Get a single field from a database record
 *
 * @uses $CFG
 * @uses $db
 * @param string $sql The SQL string you wish to be executed.
 * @return mixed|false Returns the value return from the SQL statment or false if an error occured.
 * @todo Finish documenting this function
 */
function get_field_sql($sql) {

    global $db, $CFG;

    $rs = $db->Execute($sql);
    if (!$rs) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />'. $sql);
        }
        return false;
    }

    if ( $rs->RecordCount() == 1 ) {
        return $rs->fields[0];
    } else {
        return false;
    }
}

/**
 * Set a single field in a database record
 *
 * @uses $CFG
 * @uses $db
 * @param string $table The database table to be checked against.
 * @param string $field1 The first table field to be checked for a given value. Do not supply a field1 string to leave out a WHERE clause altogether.
 * @param string $value1 The value to match if field1 is specified.
 * @param string $field2 The second table field to be checked for a given value.
 * @param string $value2 The value to match if field2 is specified.
 * @param string $field3 The third table field to be checked for a given value.
 * @param string $value3 The value to match if field3 is specified.
 * @return array An associative array with the results from the SQL call.
 * @todo Verify return type
 */
function set_field($table, $newfield, $newvalue, $field1, $value1, $field2='', $value2='', $field3='', $value3='') {

    global $db, $CFG;

    $select = 'WHERE '. $field1 .' = \''. $value1 .'\'';

    if ($field2) {
        $select .= ' AND '. $field2 .' = \''. $value2 .'\'';
        if ($field3) {
            $select .= ' AND '. $field3 .' = \''. $value3 .'\'';
        }
    }

    return $db->Execute('UPDATE '. $CFG->prefix . $table .' SET '. $newfield  .' = \''. $newvalue .'\' '. $select);
}

/**
 * Delete one or more records from a table
 *
 * @uses $CFG
 * @uses $db
 * @param string $table The database table to be checked against.
 * @param string $field1 The first table field to be checked for a given value. Do not supply a field1 string to leave out a WHERE clause altogether.
 * @param string $value1 The value to match if field1 is specified.
 * @param string $field2 The second table field to be checked for a given value.
 * @param string $value2 The value to match if field2 is specified.
 * @param string $field3 The third table field to be checked for a given value.
 * @param string $value3 The value to match if field3 is specified.
 * @return array An associative array with the results from the SQL call.
 * @todo Verify return type
 */
function delete_records($table, $field1='', $value1='', $field2='', $value2='', $field3='', $value3='') {

    global $db, $CFG;

    if ($field1) {
        $select = 'WHERE '. $field1 .' = \''. $value1 .'\'';
        if ($field2) {
            $select .= ' AND '. $field2 .' = \''. $value2 .'\'';
            if ($field3) {
                $select .= ' AND '. $field3 .' = \''. $value3 .'\'';
            }
        }
    } else {
        $select = '';
    }

    return $db->Execute('DELETE FROM '. $CFG->prefix . $table .' '. $select);
}

/**
 * Delete one or more records from a table
 *
 * @uses $CFG
 * @uses $db
 * @param string $table The database table to be checked against.
 * @param string $select A fragment of SQL to be used in a where clause in the SQL call (used to define the selection criteria).
 * @return array An associative array with the results from the SQL call.
 * @todo Verify return type.
 */
function delete_records_select($table, $select='') {

    global $CFG, $db;

    if ($select) {
        $select = 'WHERE '.$select;
    }

    return $db->Execute('DELETE FROM '. $CFG->prefix . $table .' '. $select);
}

/**
 * Insert a record into a table and return the "id" field if required
 *
 * If the return ID isn't required, then this just reports success as true/false.
 * $dataobject is an object containing needed data
 *
 * @uses $db
 * @uses $CFG
 * @param string $table The database table to be checked against.
 * @param array $dataobject An associative array with contents equal to fieldname=>fieldvalue. Must have an entry for 'id' to map to the table specified.
 * @param boolean $returnid Should the id of the newly created record entry be returned? If this option is not requested then true/false is returned.
 * @param string $primarykey Defaults to 'id'. The primary key to use against the specified table when inserting the contents of $dataobject.
 * @return int|false Returns the id for the new record created or false if an error occured.
 * @todo Finish documenting this function. Dataobject is actually an associateive array, correct?
 */
function insert_record($table, $dataobject, $returnid=true, $primarykey='id') {

    global $db, $CFG;

/// Execute a dummy query to get an empty recordset
    if (!$rs = $db->Execute('SELECT * FROM '. $CFG->prefix . $table .' WHERE '. $primarykey  .' = \'-1\'')) {
        return false;
    }

/// Get the correct SQL from adoDB
    if (!$insertSQL = $db->GetInsertSQL($rs, (array)$dataobject, true)) {
        return false;
    }

/// Run the SQL statement
    if (!$rs = $db->Execute($insertSQL)) {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />'.$insertSQL);
        }
        return false;
    }

/// If a return ID is not needed then just return true now
    if (!$returnid) {
        return true;
    }

/// Find the return ID of the newly inserted record
    switch ($CFG->dbtype) {
        case 'postgres7':             // Just loves to be special
            $oid = $db->Insert_ID();
            if ($rs = $db->Execute('SELECT '. $primarykey .' FROM '. $CFG->prefix . $table .' WHERE oid = '. $oid)) {
                if ($rs->RecordCount() == 1) {
                    return (integer) $rs->fields[0];
                }
            }
            return false;

        default:
            return $db->Insert_ID();  // Should work on most databases, but not all!
    }
}


/**
 * Update a record in a table
 *
 * $dataobject is an object containing needed data
 * Relies on $dataobject having a variable "id" to
 * specify the record to update
 *
 * @uses $CFG
 * @uses $db
 * @param string $table The database table to be checked against.
 * @param array $dataobject An associative array with contents equal to fieldname=>fieldvalue. Must have an entry for 'id' to map to the table specified.
 * @return boolean
 * @todo Finish documenting this function. Dataobject is actually an associateive array, correct?
 */
function update_record($table, $dataobject) {

    global $db, $CFG;

    if (! isset($dataobject->id) ) {
        return false;
    }

    // Determine all the fields in the table
    if (!$columns = $db->MetaColumns($CFG->prefix . $table)) {
        return false;
    }
    $data = (array)$dataobject;

    // Pull out data matching these fields
    foreach ($columns as $column) {
        if ($column->name <> 'id' and isset($data[$column->name]) ) {
            $ddd[$column->name] = $data[$column->name];
        }
    }

    // Construct SQL queries
    $numddd = count($ddd);
    $count = 0;
    $update = '';

    foreach ($ddd as $key => $value) {
        $count++;
        $update .= $key .' = \''. $value .'\'';
        if ($count < $numddd) {
            $update .= ', ';
        }
    }

    if ($rs = $db->Execute('UPDATE '. $CFG->prefix . $table .' SET '. $update .' WHERE id = \''. $dataobject->id .'\'')) {
        return true;
    } else {
        if (isset($CFG->debug) and $CFG->debug > 7) {
            notify($db->ErrorMsg() .'<br /><br />UPDATE '. $CFG->prefix . $table .' SET '. $update .' WHERE id = \''. $dataobject->id .'\'');
        }
        return false;
    }
}




/// USER DATABASE ////////////////////////////////////////////////

/**
 * Get a complete user record, which includes all the info
 *
 * in the user record, as well as membership information
 * Suitable for setting as $USER session cookie.
 *
 * @uses $CFG
 * @uses SITEID
 * @param string $field The first table field to be checked for a given value.
 * @param string $value The value to match for $field.
 * @return user A {@link $USER} object.
 * @todo Finish documenting this function
 */
function get_user_info_from_db($field, $value) {

    global $CFG;

    if (!$field or !$value) {
        return false;
    }

/// Get all the basic user data

    if (! $user = get_record_select('user', $field .' = \''. $value .'\' AND deleted <> \'1\'')) {
        return false;
    }

/// Add membership information

    if ($admins = get_records('user_admins', 'userid', $user->id)) {
        $user->admin = true;
    }

    $user->student[SITEID] = isstudent(SITEID, $user->id);

/// Determine enrolments based on current enrolment module

    require_once($CFG->dirroot .'/enrol/'. $CFG->enrol .'/enrol.php');
    $enrol = new enrolment_plugin();
    $enrol->get_student_courses($user);
    $enrol->get_teacher_courses($user);

/// Get various settings and preferences

    if ($displays = get_records('course_display', 'userid', $user->id)) {
        foreach ($displays as $display) {
            $user->display[$display->course] = $display->display;
        }
    }

    if ($preferences = get_records('user_preferences', 'userid', $user->id)) {
        foreach ($preferences as $preference) {
            $user->preference[$preference->name] = $preference->value;
        }
    }

    if ($groups = get_records('groups_members', 'userid', $user->id)) {
        foreach ($groups as $groupmember) {
            $courseid = get_field('groups', 'courseid', 'id', $groupmember->groupid);
            $user->groupmember[$courseid] = $groupmember->groupid;
        }
    }


    return $user;
}


/**
 * Does this username and password specify a valid admin user?
 *
 * @uses $CFG
 * @param string $username The name of the user to be tested for admin rights
 * @param string $md5password The password supplied by the user in md5 encrypted format.
 * @return boolean
 */
function adminlogin($username, $md5password) {

    global $CFG;

    return record_exists_sql("SELECT u.id
                                FROM {$CFG->prefix}user u,
                                     {$CFG->prefix}user_admins a
                              WHERE u.id = a.userid
                                AND u.username = '$username'
                                AND u.password = '$md5password'");
}

/**
 * Get the guest user information from the database
 *
 * @return object(user) An associative array with the details of the guest user account.
 * @todo Is object(user) a correct return type? Or is array the proper return type with a note that the contents include all details for a user.
 */
function get_guest() {
    return get_user_info_from_db('username', 'guest');
}


/**
 * Returns $user object of the main admin user
 *
 * @uses $CFG
 * @return object(admin) An associative array representing the admin user.
 * @todo Verify documentation of this function
 */
function get_admin () {

    global $CFG;

    if ( $admins = get_admins() ) {
        foreach ($admins as $admin) {
            return $admin;   // ie the first one
        }
    } else {
        return false;
    }
}

/**
 * Returns list of all admins
 *
 * @uses $CFG
 * @return array An array of {@link $USER} records.
 * @todo Finish documenting this function
 */
function get_admins() {

    global $CFG;

    return get_records_sql("SELECT u.*, a.id as adminid
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}user_admins a
                             WHERE a.userid = u.id
                             ORDER BY a.id ASC");
}

/**
 * Returns list of all creators
 *
 * @uses $CFG
 * @return array An array of {@link $USER} objects.
 * @todo Finish documenting this function
 */
function get_creators() {

    global $CFG;

    return get_records_sql("SELECT u.*
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}user_coursecreators a
                             WHERE a.userid = u.id
                             ORDER BY u.id ASC");
}

/**
 * Returns $user object of the main teacher for a course
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @return user|false  A {@link $USER} record of the main teacher for the specified course or false if error.
 * @todo Finish documenting this function
 */
function get_teacher($courseid) {

    global $CFG;

    if ( $teachers = get_course_teachers($courseid, 't.authority ASC')) {
        foreach ($teachers as $teacher) {
            if ($teacher->authority) {
                return $teacher;   // the highest authority teacher
            }
        }
    } else {
        return false;
    }
}

/**
 * Searches logs to find all enrolments since a certain date
 *
 * used to print recent activity
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @return array|false An array of {@link $USER} records or false if error.
 * @todo Finish documenting this function
 */
function get_recent_enrolments($courseid, $timestart) {

    global $CFG;

    return get_records_sql("SELECT DISTINCT u.id, u.firstname, u.lastname, l.time
                            FROM {$CFG->prefix}user u,
                                 {$CFG->prefix}user_students s,
                                 {$CFG->prefix}log l
                            WHERE l.time > '$timestart'
                              AND l.course = '$courseid'
                              AND l.module = 'course'
                              AND l.action = 'enrol'
                              AND l.info = u.id
                              AND u.id = s.userid
                              AND s.course = '$courseid'
                              ORDER BY l.time ASC");
}

/**
 * Returns array of userinfo of all students in this course
 * or on this site if courseid is id of site
 *
 * @uses $CFG
 * @uses SITEID
 * @param int $courseid The course in question.
 * @param string $sort ?
 * @param string $dir ?
 * @param int $page ?
 * @param int $recordsperpage ?
 * @param string $firstinitial ?
 * @param string $lastinitial ?
 * @param ? $group ?
 * @param string $search ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @param string $exceptions ?
 * @return array An array of {@link $USER} records.
 * @todo Finish documenting this function
 */
function get_course_students($courseid, $sort='s.timeaccess', $dir='', $page=0, $recordsperpage=99999,
                             $firstinitial='', $lastinitial='', $group=NULL, $search='', $fields='', $exceptions='') {

    global $CFG;

    if ($courseid == SITEID and $CFG->allusersaresitestudents) {
        // return users with confirmed, undeleted accounts who are not site teachers
        // the following is a mess because of different conventions in the different user functions
        $sort = str_replace('s.timeaccess', 'lastaccess', $sort); // site users can't be sorted by timeaccess
        $sort = str_replace('timeaccess', 'lastaccess', $sort); // site users can't be sorted by timeaccess
        $sort = str_replace('u.', '', $sort); // the get_user function doesn't use the u. prefix to fields
        $fields = str_replace('u.', '', $fields);
        if ($sort) {
            $sort = $sort .' '. $dir;
        }
        // Now we have to make sure site teachers are excluded
        if ($teachers = get_records('user_teachers', 'course', SITEID)) {
            foreach ($teachers as $teacher) {
                $exceptions .= ','. $teacher->userid;
            }
            $exceptions = ltrim($exceptions, ',');
        }
        return get_users(true, $search, true, $exceptions, $sort, $firstinitial, $lastinitial,
                          $page, $recordsperpage, $fields ? $fields : '*');
    }

    switch ($CFG->dbtype) {
        case 'mysql':
             $fullname = ' CONCAT(firstname," ",lastname) ';
             $limit = 'LIMIT '. $page .','. $recordsperpage;
             $LIKE = 'LIKE';
             break;
        case 'postgres7':
             $fullname = " firstname||' '||lastname ";
             $limit = 'LIMIT '. $recordsperpage .' OFFSET '.($page);
             $LIKE = 'ILIKE';
             break;
        default:
             $fullname = ' firstname||" "||lastname ';
             $limit = 'LIMIT '. $recordsperpage .','. $page;
             $LIKE = 'ILIKE';
    }

    $groupmembers = '';

    // make sure it works on the site course
    $select = 's.course = \''. $courseid .'\' AND ';
    if ($courseid == SITEID) {
        $select = '';
    }

    $select .= 's.userid = u.id AND u.deleted = \'0\' ';

    if (!$fields) {
        $fields = 'u.id, u.confirmed, u.username, u.firstname, u.lastname, '.
                  'u.maildisplay, u.mailformat, u.maildigest, u.email, u.city, '.
                  'u.country, u.picture, u.idnumber, u.department, u.institution, '.
                  'u.emailstop, u.lang, u.timezone, s.timeaccess as lastaccess';
    }

    if ($search) {
        $search = ' AND ('. $fullname .' '. $LIKE .'\'%'. $search .'%\' OR email '. $LIKE .'\'%'. $search .'%\') ';
    }

    if ($firstinitial) {
        $select .= ' AND u.firstname '. $LIKE .'\''. $firstinitial .'%\' ';
    }

    if ($lastinitial) {
        $select .= ' AND u.lastname '. $LIKE .'\''. $lastinitial .'%\' ';
    }

    if ($group === 0) {   /// Need something here to get all students not in a group
        return array();

    } else if ($group !== NULL) {
        $groupmembers = ', '. $CFG->prefix .'groups_members gm ';
        $select .= ' AND u.id = gm.userid AND gm.groupid = \''. $group .'\'';
    }

    if (!empty($exceptions)) {
        $select .= ' AND u.id NOT IN ('. $exceptions .')';
    }

    if ($sort) {
        $sort = ' ORDER BY '. $sort .' ';
    }

    $students = get_records_sql("SELECT $fields
                            FROM {$CFG->prefix}user u,
                                 {$CFG->prefix}user_students s
                                 $groupmembers
                            WHERE $select $search $sort $dir $limit");

    if ($courseid != SITEID) {
        return $students;
    }

    // We are here because we need the students for the site.
    // These also include teachers on real courses minus those on the site
    if ($teachers = get_records('user_teachers', 'course', SITEID)) {
        foreach ($teachers as $teacher) {
            $exceptions .= ','. $teacher->userid;
        }
        $exceptions = ltrim($exceptions, ',');
        $select .= ' AND u.id NOT IN ('. $exceptions .')';
    }
    if (!$teachers = get_records_sql("SELECT $fields
                            FROM {$CFG->prefix}user u,
                                 {$CFG->prefix}user_teachers s
                                 $groupmembers
                            WHERE $select $search $sort $dir $limit")) {
        return $students;
    }
    if (!$students) {
        return $teachers;
    }
    return $teachers + $students;
}


/**
 * Counts the students in a given course (or site), or a subset of them
 *
 * @param object $course The course in question as a course object.
 * @param string $search ?
 * @param string $firstinitial ?
 * @param string $lastinitial ?
 * @param ? $group ?
 * @param string $exceptions ?
 * @return int
 * @todo Finish documenting this function
 */
function count_course_students($course, $search='', $firstinitial='', $lastinitial='', $group=NULL, $exceptions='') {

    if ($students = get_course_students($course->id, '', '', 0, 999999, $firstinitial, $lastinitial, $group, $search, '', $exceptions)) {
        return count($students);
    }
    return 0;
}


/**
 * Returns list of all teachers in this course
 *
 * If $courseid matches the site id then this function
 * returns a list of all teachers for the site.
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @param string $sort ?
 * @param string $exceptions ?
 * @return array An array of {@link $USER} records.
 * @todo Finish documenting this function
 */
function get_course_teachers($courseid, $sort='t.authority ASC', $exceptions='') {

    global $CFG;

    if (!empty($exceptions)) {
        $exceptions = ' AND u.id NOT IN ('. $exceptions .') ';
    }

    if (!empty($sort)) {
        $sort = ' ORDER by '.$sort;
    }

    return get_records_sql("SELECT u.id, u.username, u.firstname, u.lastname, u.maildisplay, u.mailformat, u.maildigest,
                                   u.email, u.city, u.country, u.lastlogin, u.picture, u.lang, u.timezone,
                                   u.emailstop, t.authority,t.role,t.editall,t.timeaccess as lastaccess
                            FROM {$CFG->prefix}user u,
                                 {$CFG->prefix}user_teachers t
                            WHERE t.course = '$courseid' AND t.userid = u.id
                              AND u.deleted = '0' AND u.confirmed = '1' $exceptions $sort");
}

/**
 * Returns all the users of a course: students and teachers
 *
 * @param int $courseid The course in question.
 * @param string $sort ?
 * @param string $exceptions ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array An array of {@link $USER} records.
 * @todo Finish documenting this function
 */
function get_course_users($courseid, $sort='timeaccess DESC', $exceptions='', $fields='') {

    /// Using this method because the single SQL is too inefficient
    // Note that this has the effect that teachers and students are
    // sorted individually. Returns first all teachers, then all students

    if (!$teachers = get_course_teachers($courseid, $sort, $exceptions)) {
        $teachers = array();
    }
    if (!$students = get_course_students($courseid, $sort, '', 0, 99999, '', '', NULL, '', $fields, $exceptions)) {
        $students = array();
    }

    return $teachers + $students;

}

/**
 * Search through course users
 *
 * If $coursid specifies the site course then this function searches
 * through all undeleted and confirmed users
 *
 * @uses $CFG
 * @uses SITEID
 * @param int $courseid The course in question.
 * @param int $groupid The group in question.
 * @param string $searchtext ?
 * @param string $sort ?
 * @param string $exceptions ?
 * @return array  An array of {@link $USER} records.
 * @todo Finish documenting this function
 */
function search_users($courseid, $groupid, $searchtext, $sort='', $exceptions='') {
    global $CFG;

    switch ($CFG->dbtype) {
        case 'mysql':
             $fullname = ' CONCAT(u.firstname," ",u.lastname) ';
             $LIKE = 'LIKE';
             break;
        case 'postgres7':
             $fullname = " u.firstname||' '||u.lastname ";
             $LIKE = 'ILIKE';
             break;
        default:
             $fullname = ' u.firstname||" "||u.lastname ';
             $LIKE = 'ILIKE';
    }

    if (!empty($exceptions)) {
        $except = ' AND u.id NOT IN ('. $exceptions .') ';
    } else {
        $except = '';
    }

    if (!empty($sort)) {
        $order = ' ORDER BY '. $sort;
    } else {
        $order = '';
    }

    $select = 'u.deleted = \'0\' AND u.confirmed = \'1\'';

    if (!$courseid or $courseid == SITEID) {
        return get_records_sql("SELECT u.id, u.firstname, u.lastname, u.email
                      FROM {$CFG->prefix}user u
                      WHERE $select
                          AND ($fullname $LIKE '%$searchtext%' OR u.email $LIKE '%$searchtext%')
                          $except $order");
    } else {

        if ($groupid) {
            return get_records_sql("SELECT u.id, u.firstname, u.lastname, u.email
                          FROM {$CFG->prefix}user u,
                               {$CFG->prefix}groups_members g
                          WHERE $select AND g.groupid = '$groupid' AND g.userid = u.id
                              AND ($fullname $LIKE '%$searchtext%' OR u.email $LIKE '%$searchtext%')
                              $except $order");
        } else {
            if (!$teachers = get_records_sql("SELECT u.id, u.firstname, u.lastname, u.email
                          FROM {$CFG->prefix}user u,
                               {$CFG->prefix}user_teachers s
                          WHERE $select AND s.course = '$courseid' AND s.userid = u.id
                              AND ($fullname $LIKE '%$searchtext%' OR u.email $LIKE '%$searchtext%')
                              $except $order")) {
                $teachers = array();
            }
            if (!$students = get_records_sql("SELECT u.id, u.firstname, u.lastname, u.email
                          FROM {$CFG->prefix}user u,
                               {$CFG->prefix}user_students s
                          WHERE $select AND s.course = '$courseid' AND s.userid = u.id
                              AND ($fullname $LIKE '%$searchtext%' OR u.email $LIKE '%$searchtext%')
                              $except $order")) {
                $students = array();
            }
            return $teachers + $students;
        }
    }
}


/**
 * Returns a list of all site users
 * Obsolete, just calls get_course_users(SITEID)
 *
 * @uses SITEID
 * @deprecated Use {@link get_course_users()} instead.
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array|false  An array of {@link $USER} records or false if error.
 * @todo Finish documenting this function. The return type need to be better defined.
 */
function get_site_users($sort='u.lastaccess DESC', $fields='*', $exceptions='') {

    return get_course_users(SITEID, $sort, $exceptions, $fields);
}


/**
 * Returns a subset of users
 *
 * @uses $CFG
 * @param boolean $get If false then only a count of the records is returned
 * @param string $search A simple string to search for
 * @param boolean $confirmed A switch to allow/disallow unconfirmed users
 * @param array(int) $exceptions A list of IDs to ignore, eg 2,4,5,8,9,10
 * @param string $sort A SQL snippet for the sorting criteria to use
 * @param string $firstinitial ?
 * @param string $lastinitial ?
 * @param string $page ?
 * @param string $recordsperpage ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array|false|int  An array of {@link $USER} records unless get is false in which case the integer count of the records found is returned. False is returned if an error is encountered.
 * @todo Finish documenting this function. The return type needs to be better defined.
 */
function get_users($get=true, $search='', $confirmed=false, $exceptions='', $sort='firstname ASC',
                   $firstinitial='', $lastinitial='', $page=0, $recordsperpage=99999, $fields='*') {

    global $CFG;

    switch ($CFG->dbtype) {
        case 'mysql':
             $limit = 'LIMIT '. $page .','. $recordsperpage;
             $fullname = ' CONCAT(firstname," ",lastname) ';
             $LIKE = 'LIKE';
             break;
        case 'postgres7':
             $limit = 'LIMIT '. $recordsperpage .' OFFSET '.($page);
             $fullname = " firstname||' '||lastname ";
             $LIKE = 'ILIKE';
             break;
        default:
             $limit = 'LIMIT '. $recordsperpage .','. $page;
             $fullname = ' firstname||" "||lastname ';
             $LIKE = 'ILIKE';
    }

    $select = 'username <> \'guest\' AND deleted = 0';

    if ($search) {
        $select .= " AND ($fullname $LIKE '%$search%' OR email $LIKE '%$search%') ";
    }

    if ($confirmed) {
        $select .= ' AND confirmed = \'1\' ';
    }

    if ($exceptions) {
        $select .= ' AND id NOT IN ('. $exceptions .') ';
    }

    if ($firstinitial) {
        $select .= ' AND firstname '. $LIKE .' \''. $firstinitial .'%\'';
    }
    if ($lastinitial) {
        $select .= ' AND lastname '. $LIKE .' \''. $lastinitial .'%\'';
    }

    if ($sort and $get) {
        $sort = ' ORDER BY '. $sort .' ';
    } else {
        $sort = '';
    }

    if ($get) {
        return get_records_select('user', $select .' '. $sort .' '. $limit, '', $fields);
    } else {
        return count_records_select('user', $select .' '. $sort .' '. $limit);
    }
}


/**
 * shortdesc (optional)
 *
 * longdesc
 *
 * @uses $CFG
 * @param string $sort ?
 * @param string $dir ?
 * @param int $categoryid ?
 * @param int $categoryid ?
 * @param string $search ?
 * @param string $firstinitial ?
 * @param string $lastinitial ?
 * @return array  An array of {@link $USER} records
 * @todo Finish documenting this function
 */

function get_users_listing($sort='lastaccess', $dir='ASC', $page=0, $recordsperpage=99999,
                           $search='', $firstinitial='', $lastinitial='') {

    global $CFG;

    switch ($CFG->dbtype) {
        case 'mysql':
             $limit = 'LIMIT '. $page .','. $recordsperpage;
             $fullname = ' CONCAT(firstname," ",lastname) ';
             $LIKE = 'LIKE';
             break;
        case 'postgres7':
             $limit = 'LIMIT '. $recordsperpage .' OFFSET '.($page);
             $fullname = ' firstname||\' \'||lastname ';
             $LIKE = 'ILIKE';
             break;
        default:
             $limit = 'LIMIT '. $recordsperpage .','. $page;
             $fullname = ' firstname||\' \'||lastname ';
             $LIKE = 'LIKE';
    }

    $select = 'deleted <> 1';

    if ($search) {
        $select .= " AND ($fullname $LIKE '%$search%' OR email $LIKE '%$search%') ";
    }

    if ($firstinitial) {
        $select .= ' AND firstname '. $LIKE .' \''. $firstinitial .'%\' ';
    }

    if ($lastinitial) {
        $select .= ' AND lastname '. $LIKE .' \''. $lastinitial .'%\' ';
    }

    if ($sort) {
        $sort = ' ORDER BY '. $sort .' '. $dir;
    }

/// warning: will return UNCONFIRMED USERS
    return get_records_sql("SELECT id, username, email, firstname, lastname, city, country, lastaccess, confirmed
                              FROM {$CFG->prefix}user
                             WHERE $select $sort $limit ");

}


/**
 * shortdesc (optional)
 *
 * longdesc
 *
 * @uses $CFG
 * @return array  An array of {@link $USER} records
 * @todo Finish documenting this function
 */
function get_users_confirmed() {
    global $CFG;
    return get_records_sql("SELECT *
                              FROM {$CFG->prefix}user
                             WHERE confirmed = 1
                               AND deleted = 0
                               AND username <> 'guest'
                               AND username <> 'changeme'");
}


/**
 * shortdesc (optional)
 *
 * longdesc
 *
 * @uses $CFG
 * @param string $cutofftime ?
 * @return array  An array of {@link $USER} records
 * @todo Finish documenting this function
 */
function get_users_unconfirmed($cutofftime=2000000000) {
    global $CFG;
    return get_records_sql("SELECT *
                             FROM {$CFG->prefix}user
                            WHERE confirmed = 0
                              AND firstaccess > 0
                              AND firstaccess < '$cutofftime'");
}


/**
 * shortdesc (optional)
 *
 * longdesc
 *
 * @uses $CFG
 * @param string $cutofftime ?
 * @return array  An array of {@link $USER} records
 * @todo Finish documenting this function
 */
function get_users_longtimenosee($cutofftime) {
    global $CFG;
    return get_records_sql("SELECT DISTINCT *
                              FROM {$CFG->prefix}user_students
                             WHERE timeaccess > '0'
                               AND timeaccess < '$cutofftime' ");
}

/**
 * Returns an array of group objects that the user is a member of
 * in the given course.  If userid isn't specified, then return a
 * list of all groups in the course.
 *
 * @uses $CFG
 * @param int $courseid The id of the course in question.
 * @param int $userid The id of the user in question as found in the 'user' table 'id' field.
 * @return array
 * @todo Finish documenting this function
 */
function get_groups($courseid, $userid=0) {
    global $CFG;

    if ($userid) {
        $dbselect = ', '. $CFG->prefix .'groups_members m';
        $userselect = 'AND m.groupid = g.id AND m.userid = \''. $userid .'\'';
    } else {
        $dbselect = '';
        $userselect = '';
    }

    return get_records_sql("SELECT DISTINCT g.*
                              FROM {$CFG->prefix}groups g $dbselect
                             WHERE g.courseid = '$courseid' $userselect ");
}


/**
 * Returns an array of user objects
 *
 * @uses $CFG
 * @param int $groupid The group in question.
 * @param string $sort ?
 * @param string $exceptions ?
 * @return array
 * @todo Finish documenting this function
 */
function get_group_users($groupid, $sort='u.lastaccess DESC', $exceptions='') {
    global $CFG;
    if (!empty($exceptions)) {
        $except = ' AND u.id NOT IN ('. $exceptions .') ';
    } else {
        $except = '';
    }
    return get_records_sql("SELECT DISTINCT u.*
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}groups_members m
                             WHERE m.groupid = '$groupid'
                               AND m.userid = u.id $except
                          ORDER BY $sort");
}

/**
 * An efficient way of finding all the users who aren't in groups yet
 *
 * Currently unimplemented.
 * @uses $CFG
 * @param int $courseid The course in question.
 * @return array
 */
function get_users_not_in_group($courseid) {
    global $CFG;

    return array();     /// XXX TO BE DONE
}

/**
 * Returns an array of user objects
 *
 * @uses $CFG
 * @param int $groupid The group in question.
 * @param string $sort ?
 * @return array
 * @todo Finish documenting this function
 */
function get_group_students($groupid, $sort='u.lastaccess DESC') {
    global $CFG;
    return get_records_sql("SELECT DISTINCT u.*
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}groups_members m,
                                   {$CFG->prefix}groups g,
                                   {$CFG->prefix}user_students s
                             WHERE m.groupid = '$groupid'
                               AND m.userid = u.id
                               AND m.groupid = g.id
                               AND g.courseid = s.course
                               AND s.userid = u.id
                          ORDER BY $sort");
}

/**
 * Returns the user's group in a particular course
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @param int $userid The id of the user as found in the 'user' table.
 * @return array
 * @todo Finish documenting this function
 */
function user_group($courseid, $userid) {
    global $CFG;

    return get_record_sql("SELECT g.*
                             FROM {$CFG->prefix}groups g,
                                  {$CFG->prefix}groups_members m
                             WHERE g.courseid = '$courseid'
                               AND g.id = m.groupid
                               AND m.userid = '$userid'");
}




/// OTHER SITE AND COURSE FUNCTIONS /////////////////////////////////////////////


/**
 * Returns $course object of the top-level site.
 *
 * @return course  A {@link $COURSE} object for the site
 * @todo Finish documenting this function.
 */
function get_site () {

    if ( $course = get_record('course', 'category', 0)) {
        return $course;
    } else {
        return false;
    }
}

/**
 * Returns list of courses, for whole site, or category
 *
 * @uses $CFG
 * @uses $USER
 * @param int $categoryid ?
 * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return array  An array of {@link $COURSE} records
 * @todo Finish documenting this function
 */
function get_courses($categoryid='all', $sort='c.sortorder ASC', $fields='c.*') {
/// This function deliberately uses PHP to do the checking at the end,
/// because MySQL has been known to really bog down when trying to do a JOIN
/// on the 'course' and 'user_teachers' table at the same time.

    global $USER, $CFG;

    $select = array();

    if ($categoryid != 'all') {
        $select[] = "c.category = '$categoryid'";
    }

    /// Admins can always see invisible courses
    /// Creators can always see invisible courses
    /// Teachers can only see their own invisible courses  <- needs detailed checking
    /// Students can't see invisible courses at all
    /// Guests can't see invisible courses at all

    $visiblecourses = '';
    $showallinvisible = iscreator();     // includes admins
    $hideallinvisible = empty($USER->id) || (!isteacher());

    if ($hideallinvisible) {
        $select[] = 'c.visible > 0';
    }

    if ($select) {
        $selectsql = $CFG->prefix .'course c WHERE '. implode(' AND ', $select);
    } else {
        $selectsql = $CFG->prefix .'course c ';
    }

    $courses = get_records_sql('SELECT '. $fields .' FROM '. $selectsql .' ORDER BY '. $sort);

    if ($courses and (!$hideallinvisible) and (!$showallinvisible)) { // For ordinary users we need to check visibility
        foreach ($courses as $key => $course) {
            if ($course->visible == 0) {          // Invisible course, let's check if we are a teacher
                if (!isteacher($course->id)) {    // We shouldn't see this
                    unset($courses[$key]);
                }
            }
        }
    }

    return $courses;
}

/**
 * Returns list of courses, for whole site, or category
 *
 * Similar to get_courses, but allows paging
 *
 * @uses $CFG
 * @uses $USER
 * @param int $categoryid ?
 * @param string $sort ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @param int $totalcount Passed by reference. ?
 * @param int $limitfrom ?
 * @param int $limitnum ?
 * @return array An array of {@link $COURSE} records
 * @todo Finish documenting this function
 */
function get_courses_page($categoryid='all', $sort='c.sortorder ASC', $fields='c.*',
                          &$totalcount, $limitfrom='', $limitnum='') {

/// This function deliberately uses PHP to do the checking at the end,
/// because MySQL has been known to really bog down when trying to do a JOIN
/// on the 'course' and 'user_teachers' table at the same time.

    global $USER, $CFG;

    $select = array();

    if ($categoryid != 'all') {
        $select[] = 'c.category = \''. $categoryid .'\'';
    }

    /// Admins can always see invisible courses
    /// Creators can always see invisible courses
    /// Teachers can only see their own invisible courses  <- needs detailed checking
    /// Students can't see invisible courses at all
    /// Guests can't see invisible courses at all

    $visiblecourses = '';
    $showallinvisible = iscreator();     // includes admins
    $hideallinvisible = empty($USER->id) || (!isteacher());

    if ($hideallinvisible) {
        $select[] = 'c.visible > 0';
    }

    if ($select) {
        $selectsql = $CFG->prefix .'course c WHERE '.implode(' AND ', $select);
    } else {
        $selectsql = $CFG->prefix .'course c ';
    }

    $courses = get_records_sql('SELECT '. $fields .' FROM '. $selectsql .' ORDER BY '. $sort);

    if ($courses and (!$hideallinvisible) and (!$showallinvisible)) { // For ordinary users we need to check visibility
        foreach ($courses as $key => $course) {
            if ($course->visible == 0) {          // Invisible course, let's check if we are a teacher
                if (!isteacher($course->id)) {    // We shouldn't see this
                    unset($courses[$key]);
                }
            }
        }
    }

    $totalcount = count($courses);

    if ($courses and ($limitfrom or $limitnum)) {
        $courses = array_slice($courses, (int)$limitfrom, (int)$limitnum);
    }

    return $courses;
}


/**
 * shortdesc (optional)
 *
 * longdesc
 *
 * @uses $CFG
 * @param int $userid ?
 * @param string $sort ?
 * @return array  An array of {@link $COURSE} records
 * @todo Finish documenting this function
 */
function get_my_courses($userid, $sort='visible DESC,sortorder ASC') {

    global $CFG;

    $course = array();

    if ($students = get_records('user_students', 'userid', $userid, '', 'id, course')) {
        foreach ($students as $student) {
            $course[$student->course] = $student->course;
        }
    }
    if ($teachers = get_records('user_teachers', 'userid', $userid, '', 'id, course')) {
        foreach ($teachers as $teacher) {
            $course[$teacher->course] = $teacher->course;
        }
    }
    if (empty($course)) {
        return $course;
    }

    $courseids = implode(',', $course);

    return get_records_list('course', 'id', $courseids, $sort);

//  The following is correct but VERY slow with large datasets
//
//    return get_records_sql("SELECT c.*
//                              FROM {$CFG->prefix}course c,
//                                   {$CFG->prefix}user_students s,
//                                   {$CFG->prefix}user_teachers t
//                             WHERE (s.userid = '$userid' AND s.course = c.id)
//                                OR (t.userid = '$userid' AND t.course = c.id)
//                             GROUP BY c.id
//                             ORDER BY $sort");
}


/**
 * Returns a list of courses that match a search
 *
 * @uses $CFG
 * @param array $searchterms ?
 * @param string $sort ?
 * @param int $page ?
 * @param int $recordsperpage ?
 * @param int $totalcount Passed in by reference. ?
 * @return array  An array of {@link $COURSE} records
 * @todo Finish documenting this function
 */
function get_courses_search($searchterms, $sort='fullname ASC', $page=0, $recordsperpage=50, &$totalcount) {

    global $CFG;

    switch ($CFG->dbtype) {
        case 'mysql':
             $limit = 'LIMIT '. $page .','. $recordsperpage;
             break;
        case 'postgres7':
             $limit = 'LIMIT '. $recordsperpage .' OFFSET '.$page;
             break;
        default:
             $limit = 'LIMIT '. $recordsperpage .','. $page;
    }

    //to allow case-insensitive search for postgesql
    if ($CFG->dbtype == 'postgres7') {
        $LIKE = 'ILIKE';
        $NOTLIKE = 'NOT ILIKE';   // case-insensitive
        $REGEXP = '~*';
        $NOTREGEXP = '!~*';
    } else {
        $LIKE = 'LIKE';
        $NOTLIKE = 'NOT LIKE';
        $REGEXP = 'REGEXP';
        $NOTREGEXP = 'NOT REGEXP';
    }

    $fullnamesearch = '';
    $summarysearch = '';

    foreach ($searchterms as $searchterm) {
        if ($fullnamesearch) {
            $fullnamesearch .= ' AND ';
        }
        if ($summarysearch) {
            $summarysearch .= ' AND ';
        }

        if (substr($searchterm,0,1) == '+') {
            $searchterm = substr($searchterm,1);
            $summarysearch .= " summary $REGEXP '(^|[^a-zA-Z0-9])$searchterm([^a-zA-Z0-9]|$)' ";
            $fullnamesearch .= " fullname $REGEXP '(^|[^a-zA-Z0-9])$searchterm([^a-zA-Z0-9]|$)' ";
        } else if (substr($searchterm,0,1) == "-") {
            $searchterm = substr($searchterm,1);
            $summarysearch .= " summary $NOTREGEXP '(^|[^a-zA-Z0-9])$searchterm([^a-zA-Z0-9]|$)' ";
            $fullnamesearch .= " fullname $NOTREGEXP '(^|[^a-zA-Z0-9])$searchterm([^a-zA-Z0-9]|$)' ";
        } else {
            $summarysearch .= ' summary '. $LIKE .'\'%'. $searchterm .'%\' ';
            $fullnamesearch .= ' fullname '. $LIKE .'\'%'. $searchterm .'%\' ';
        }

    }

    $selectsql = $CFG->prefix .'course WHERE ('. $fullnamesearch .' OR '. $summarysearch .') AND category > \'0\'';

    $totalcount = count_records_sql('SELECT COUNT(*) FROM '. $selectsql);

    $courses = get_records_sql('SELECT * FROM '. $selectsql .' ORDER BY '. $sort .' '. $limit);

    if ($courses) {  /// Remove unavailable courses from the list
        foreach ($courses as $key => $course) {
            if (!$course->visible) {
                if (!isteacher($course->id)) {
                    unset($courses[$key]);
                    $totalcount--;
                }
            }
        }
    }

    return $courses;
}


/**
 * Returns a sorted list of categories
 *
 * @param string $parent ?
 * @param string $sort ?
 * @return ?
 * @todo Finish documenting this function
 */
function get_categories($parent='none', $sort='sortorder ASC') {

    if ($parent == 'none') {
        $categories = get_records('course_categories', '', '', $sort);
    } else {
        $categories = get_records('course_categories', 'parent', $parent, $sort);
    }
    if ($categories) {  /// Remove unavailable categories from the list
        $creator = iscreator();
        foreach ($categories as $key => $category) {
            if (!$category->visible) {
                if (!$creator) {
                    unset($categories[$key]);
                }
            }
        }
    }
    return $categories;
}


/**
* This recursive function makes sure that the courseorder is consecutive
*
* @param    type description
*
* $n is the starting point, offered only for compatilibity -- will be ignored!
* $safe (bool) prevents it from assuming category-sortorder is unique, used to upgrade
*       safely from 1.4 to 1.5
*/
function fix_course_sortorder($categoryid=0, $n=0, $safe=0) {

    global $CFG;

    $count = 0;

    $n=100;

    // get some basic info
    $info = get_record_sql('SELECT MIN(sortorder) AS min,
                                   MAX(sortorder) AS max,
                                   COUNT(sortorder)  AS count
                            FROM ' . $CFG->prefix . 'course
                            WHERE category=' . $categoryid);
    if (is_object($info)) { // no courses?
        $max   = $info->max;
        $count = $info->count;
        $min   = $info->min;
        unset($info);
    }

    // actually sort only if there are courses,
    // and we meet one ofthe triggers:
    //  - safe flag
    //  - they are not in a continuos block
    //  - they are too close to the 'bottom'
    if ($count && (    $safe
                    || ($max-$min+1!=$count)
                    || $min < 10 ) ) {
        if ($courses = get_courses($categoryid, 'c.sortorder ASC', 'c.id,c.sortorder')) {
            begin_sql();

            // find the ideal starting point
            if ( ($min<$n&&$n<$max) || ($n+$count>=$min) || ($min<10) ) {

                $n = $max+100; // this is usually the ideal solution

                // if we are aiming way too high, try to bring it back to earth
                if ($n > 100+3*$count) {
                    if ($min > 100+$count){
                        $n = 100;
                    }
                }
            }

            foreach ($courses as $course) {
                if ($course->sortorder != $n ) { // save db traffic
                    set_field('course', 'sortorder', $n, 'id', $course->id);
                }
                $n++;
            }
            commit_sql();
        }
    }
    set_field('course_categories', 'coursecount', $count, 'id', $categoryid);

    $n=0;
    if ($categories = get_categories($categoryid)) {
        foreach ($categories as $category) {
            $n = fix_course_sortorder($category->id, $n);
        }
    }

    return $n;
}


/**
 * This function creates a default separated/connected scale
 *
 * This function creates a default separated/connected scale
 * so there's something in the database.  The locations of
 * strings and files is a bit odd, but this is because we
 * need to maintain backward compatibility with many different
 * existing language translations and older sites.
 *
 * @uses $CFG
 */
function make_default_scale() {

    global $CFG;

    $defaultscale = NULL;
    $defaultscale->courseid = 0;
    $defaultscale->userid = 0;
    $defaultscale->name  = get_string('separateandconnected');
    $defaultscale->scale = get_string('postrating1', 'forum').','.
                           get_string('postrating2', 'forum').','.
                           get_string('postrating3', 'forum');
    $defaultscale->timemodified = time();

    /// Read in the big description from the file.  Note this is not
    /// HTML (despite the file extension) but Moodle format text.
    $parentlang = get_string('parentlang');
    if (is_readable($CFG->dirroot .'/lang/'. $CFG->lang .'/help/forum/ratings.html')) {
        $file = file($CFG->dirroot .'/lang/'. $CFG->lang .'/help/forum/ratings.html');
    } else if ($parentlang and is_readable($CFG->dirroot .'/lang/'. $parentlang .'/help/forum/ratings.html')) {
        $file = file($CFG->dirroot .'/lang/'. $parentlang .'/help/forum/ratings.html');
    } else if (is_readable($CFG->dirroot .'/lang/en/help/forum/ratings.html')) {
        $file = file($CFG->dirroot .'/lang/en/help/forum/ratings.html');
    } else {
        $file = '';
    }

    $defaultscale->description = addslashes(implode('', $file));

    if ($defaultscale->id = insert_record('scale', $defaultscale)) {
        execute_sql('UPDATE '. $CFG->prefix .'forum SET scale = \''. $defaultscale->id .'\'', false);
    }
}


/**
 * Returns a menu of all available scales from the site as well as the given course
 *
 * Returns a menu of all available scales from the site as well as the given course
 *
 * @uses $CFG
 * @param int $courseid The id of the course as found in the 'course' table.
 * @return array
 * @todo Finish documenting this function
 */
function get_scales_menu($courseid=0) {

    global $CFG;

    $sql = "SELECT id, name FROM {$CFG->prefix}scale
             WHERE courseid = '0' or courseid = '$courseid'
          ORDER BY courseid ASC, name ASC";

    if ($scales = get_records_sql_menu($sql)) {
        return $scales;
    }

    make_default_scale();

    return get_records_sql_menu($sql);
}

/// MODULE FUNCTIONS /////////////////////////////////////////////////

/**
 * Just gets a raw list of all modules in a course
 *
 * @uses $CFG
 * @param int $courseid The id of the course as found in the 'course' table.
 * @return array
 * @todo Finish documenting this function
 */
function get_course_mods($courseid) {
    global $CFG;

    return get_records_sql("SELECT cm.*, m.name as modname
                            FROM {$CFG->prefix}modules m,
                                 {$CFG->prefix}course_modules cm
                            WHERE cm.course = '$courseid'
                            AND cm.deleted = '0'
                            AND cm.module = m.id ");
}


/**
 * Given an instance of a module, finds the coursemodule description
 *
 * @uses $CFG
 * @param string $modulename ?
 * @param string $instance ?
 * @param int $courseid The id of the course as found in the 'course' table.
 * @return array
 * @todo Finish documenting this function
 */
function get_coursemodule_from_instance($modulename, $instance, $courseid) {

    global $CFG;

    return get_record_sql("SELECT cm.*, m.name
                           FROM {$CFG->prefix}course_modules cm,
                                {$CFG->prefix}modules md,
                                {$CFG->prefix}$modulename m
                           WHERE cm.course = '$courseid' AND
                                 cm.deleted = '0' AND
                                 cm.instance = m.id AND
                                 md.name = '$modulename' AND
                                 md.id = cm.module AND
                                 m.id = '$instance'");

}


/**
 * Returns an array of all the active instances of a particular module in a given course, sorted in the order they are defined
 *
 * Returns an array of all the active instances of a particular
 * module in a given course, sorted in the order they are defined
 * in the course.   Returns false on any errors.
 *
 * @uses $CFG
 * @param string  $modulename The name of the module to get instances for
 * @param object(course)  $course This depends on an accurate $course->modinfo
 * @todo Finish documenting this function. Is a course object to be documented as object(course) or array(course) since a coures object is really just an associative array, not a php object?
 */
function get_all_instances_in_course($modulename, $course) {

    global $CFG;

    if (!$modinfo = unserialize($course->modinfo)) {
        return array();
    }

    if (!$rawmods = get_records_sql("SELECT cm.id as coursemodule, m.*,cw.section,cm.visible as visible,cm.groupmode
                            FROM {$CFG->prefix}course_modules cm,
                                 {$CFG->prefix}course_sections cw,
                                 {$CFG->prefix}modules md,
                                 {$CFG->prefix}$modulename m
                            WHERE cm.course = '$course->id' AND
                                  cm.instance = m.id AND
                                  cm.deleted = '0' AND
                                  cm.section = cw.id AND
                                  md.name = '$modulename' AND
                                  md.id = cm.module")) {
        return array();
    }

    // Hide non-visible instances from students
    if (isteacher($course->id)) {
        $invisible = -1;
    } else {
        $invisible = 0;
    }

    foreach ($modinfo as $mod) {
        if ($mod->mod == $modulename and $mod->visible > $invisible) {
            $instance = $rawmods[$mod->cm];
            if (!empty($mod->extra)) {
                $instance->extra = $mod->extra;
            }
            $outputarray[] = $instance;
        }
    }

    return $outputarray;

}


/**
 * Determine whether a module instance is visible within a course
 *
 * Given a valid module object with info about the id and course,
 * and the module's type (eg "forum") returns whether the object
 * is visible or not
 *
 * @uses $CFG
 * @param $moduletype ?
 * @param $module ?
 * @return boolean
 * @todo Finish documenting this function
 */
function instance_is_visible($moduletype, $module) {

    global $CFG;

    if ($records = get_records_sql("SELECT cm.instance, cm.visible
                                    FROM {$CFG->prefix}course_modules cm,
                                         {$CFG->prefix}modules m
                                   WHERE cm.course = '$module->course' AND
                                         cm.module = m.id AND
                                         m.name = '$moduletype' AND
                                         cm.instance = '$module->id'")) {

        foreach ($records as $record) { // there should only be one - use the first one
            return $record->visible;
        }
    }

    return true;  // visible by default!
}




/// LOG FUNCTIONS /////////////////////////////////////////////////////


/**
 * Add an entry to the log table.
 *
 * Add an entry to the log table.  These are "action" focussed rather
 * than web server hits, and provide a way to easily reconstruct what
 * any particular student has been doing.
 *
 * @uses $CFG
 * @uses $USER
 * @uses $db
 * @uses $REMOTE_ADDR
 * @uses SITEID
 * @param    int     $courseid  The course id
 * @param    string  $module  The module name - e.g. forum, journal, resource, course, user etc
 * @param    string  $action  View, edit, post (often but not always the same as the file.php)
 * @param    string  $url     The file and parameters used to see the results of the action
 * @param    string  $info    Additional description information
 * @param    string  $cm      The course_module->id if there is one
 * @param    string  $user    If log regards $user other than $USER
 */
function add_to_log($courseid, $module, $action, $url='', $info='', $cm=0, $user=0) {

    global $db, $CFG, $USER, $REMOTE_ADDR;

    if ($user) {
        $userid = $user;
    } else {
        if (isset($USER->realuser)) {  // Don't log
            return;
        }
        $userid = empty($USER->id) ? '0' : $USER->id;
    }

    $timenow = time();
    $info = addslashes($info);
    $url = html_entity_decode($url); // for php < 4.3.0 this is defined in moodlelib.php

    $result = $db->Execute('INSERT INTO '. $CFG->prefix .'log (time, userid, course, ip, module, cmid, action, url, info)
        VALUES (' . "'$timenow', '$userid', '$courseid', '$REMOTE_ADDR', '$module', '$cm', '$action', '$url', '$info')");

    if (!$result and ($CFG->debug > 7)) {
        echo '<p>Error: Could not insert a new entry to the Moodle log</p>';  // Don't throw an error
    }
    if (!$user and isset($USER->id)) {
        if ($courseid == SITEID) {
            $db->Execute('UPDATE '. $CFG->prefix .'user SET lastIP=\''. $REMOTE_ADDR .'\', lastaccess=\''. $timenow .'\'
                     WHERE id = \''. $USER->id .'\' ');
        } else if (isstudent($courseid)) {
            $db->Execute('UPDATE '. $CFG->prefix .'user_students SET timeaccess = \''. $timenow .'\' '.
                         'WHERE course = \''. $courseid .'\' AND userid = \''. $userid .'\'');
        } else if (isteacher($courseid, false, false)) {
            $db->Execute('UPDATE '. $CFG->prefix .'user_teachers SET timeaccess = \''. $timenow .'\' '.
                         'WHERE course = \''. $courseid .'\' AND userid = \''. $userid .'\'');
        }
    }
}


/**
 * Select all log records based on SQL criteria
 *
 * @uses $CFG
 * @param string $select SQL select criteria
 * @param string $order SQL order by clause to sort the records returned
 * @param string $limitfrom ?
 * @param int $limitnum ?
 * @param int $totalcount Passed in by reference.
 * @return array
 * @todo Finish documenting this function
 */
function get_logs($select, $order='l.time DESC', $limitfrom='', $limitnum='', &$totalcount) {
    global $CFG;

    if ($limitfrom !== '') {
        switch ($CFG->dbtype) {
            case 'mysql':
                 $limit = 'LIMIT '. $limitfrom .','. $limitnum;
                 break;
            case 'postgres7':
                 $limit = 'LIMIT '. $limitnum .' OFFSET '. $limitfrom;
                 break;
            default:
                 $limit = 'LIMIT '. $limitnum .','. $limitfrom;
        }
    } else {
        $limit = '';
    }

    if ($order) {
        $order = 'ORDER BY '. $order;
    }

    $selectsql = $CFG->prefix .'log l LEFT JOIN '. $CFG->prefix .'user u ON l.userid = u.id '. ((strlen($select) > 0) ? 'WHERE '. $select : '');
    $totalcount = count_records_sql('SELECT COUNT(*) FROM '. $selectsql);

    return get_records_sql('SELECT l.*, u.firstname, u.lastname, u.picture
                                FROM '. $selectsql .' '. $order .' '. $limit);
}


/**
 * Select all log records for a given course and user
 *
 * @uses $CFG
 * @uses DAYSECS
 * @param int $userid The id of the user as found in the 'user' table.
 * @param int $courseid The id of the course as found in the 'course' table.
 * @param string $coursestart ?
 * @todo Finish documenting this function
 */
function get_logs_usercourse($userid, $courseid, $coursestart) {
    global $CFG;

    if ($courseid) {
        $courseselect = ' AND course = \''. $courseid .'\' ';
    } else {
        $courseselect = '';
    }

    return get_records_sql("SELECT floor((`time` - $coursestart)/". DAYSECS .") as day, count(*) as num
                            FROM {$CFG->prefix}log
                           WHERE userid = '$userid'
                             AND `time` > '$coursestart' $courseselect
                        GROUP BY day ");
}

/**
 * Select all log records for a given course, user, and day
 *
 * @uses $CFG
 * @uses HOURSECS
 * @param int $userid The id of the user as found in the 'user' table.
 * @param int $courseid The id of the course as found in the 'course' table.
 * @param string $daystart ?
 * @return array
 * @todo Finish documenting this function
 */
function get_logs_userday($userid, $courseid, $daystart) {
    global $CFG;

    if ($courseid) {
        $courseselect = ' AND course = \''. $courseid .'\' ';
    } else {
        $courseselect = '';
    }

    return get_records_sql("SELECT floor((`time` - $daystart)/". HOURSECS .") as hour, count(*) as num
                            FROM {$CFG->prefix}log
                           WHERE userid = '$userid'
                             AND `time` > '$daystart' $courseselect
                        GROUP BY hour ");
}

/**
 * Returns an object with counts of failed login attempts
 *
 * Returns information about failed login attempts.  If the current user is
 * an admin, then two numbers are returned:  the number of attempts and the
 * number of accounts.  For non-admins, only the attempts on the given user
 * are shown.
 *
 * @param string $mode Either 'admin', 'teacher' or 'everybody'
 * @param string $username The username we are searching for
 * @param string $lastlogin The date from which we are searching
 * @return int
 */

function count_login_failures($mode, $username, $lastlogin) {

    $select = 'module=\'login\' AND action=\'error\' AND time > '. $lastlogin;

    if (isadmin()) {    // Return information about all accounts
        if ($count->attempts = count_records_select('log', $select)) {
            $count->accounts = count_records_select('log', $select, 'COUNT(DISTINCT info)');
            return $count;
        }
    } else if ($mode == 'everybody' or ($mode == 'teacher' and isteacher())) {
        if ($count->attempts = count_records_select('log', $select .' AND info = \''. $username .'\'')) {
            return $count;
        }
    }
    return NULL;
}


/// GENERAL HELPFUL THINGS  ///////////////////////////////////

/**
 * Dump a given object's information in a PRE block.
 *
 * Mostly just used for debugging.
 *
 * @param mixed $object The data to be printed
 * @todo add example usage and example output
 */
function print_object($object) {

    echo '<pre>';
    print_r($object);
    echo '</pre>';
}



// vim:autoindent:expandtab:shiftwidth=4:tabstop=4:tw=140:
?>