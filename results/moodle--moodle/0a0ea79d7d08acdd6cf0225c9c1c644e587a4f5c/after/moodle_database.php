<?php  //$Id$

require_once($CFG->libdir.'/dml/database_column_info.php');

/**
 * Abstract class representing moodle database interface.
 * @package dmlib
 */
abstract class moodle_database {

    // manipulates the db structure
    protected $database_manager;

    protected $columns = array(); // I wish we had a shared memory cache for this :-(

    // db connection options
    protected $dbhost;
    protected $dbuser;
    protected $dbpass;
    protected $dbname;
    protected $dbpersist;
    protected $prefix;

    /**
     * Non-moodle external database used.
     */
    protected $external;

    /**
     * Database or driver specific options, such as sockets or TCPIP db connections
     */
    protected $dboptions;

    // TODO: perf stuff goes here
    // TODO: do we really need record caching??

    /**
     * Contructor - instantiates the database, specifying if it's external (connect to other systems) or no (Moodle DB)
     *              note this has effect to decide if prefix checks must be performed or no
     * @param bool true means external database used
     */
    public function __construct($external=false) {
        $this->external  = $external;
    }

    /**
     * Detects if all needed PHP stuff installed.
     * Note: can be used before connect()
     * @return mixed true if ok, string if something
     */
    public abstract function driver_installed();

    /**
     * Returns database table prefix
     * Note: can be used before connect()
     * @return string database table prefix
     */
    public function get_prefix() {
        return $this->prefix;
    }

    /**
     * Returns database family type - describes SQL dialect
     * Note: can be used before connect()
     * @return string db family name (mysql, postgres, mssql, oracle, etc.)
     */
    public abstract function get_dbfamily();

    /**
     * Returns more specific database driver type
     * Note: can be used before connect()
     * @return string db type mysql, mysqli, postgres7
     */
    protected abstract function get_dbtype();

    /**
     * Returns localised database type name
     * Note: can be used before connect()
     * @return string
     */
    public abstract function get_name();

    /**
     * Returns localised database description
     * Note: can be used before connect()
     * @return string
     */
    public abstract function get_configuration_hints();

    /**
     * Returns db related part of config.php
     * Note: can be used before connect()
     * @return string
     */
    public abstract function export_dbconfig();

    /**
     * Connect to db
     * Must be called before other methods.
     * @param string $dbhost
     * @param string $dbuser
     * @param string $dbpass
     * @param string $dbname
     * @param bool $dbpersist
     * @param mixed $prefix string means moodle db prefix, false used for external databases where prefix not used
     * @param array $dboptions driver specific options
     * @return bool success
     */
    public abstract function connect($dbhost, $dbuser, $dbpass, $dbname, $dbpersist, $prefix, array $dboptions=null);

    /**
     * Close database connection and release all resources
     * and memory (especially circular memory references).
     * Do NOT use connect() again, create a new instance if needed.
     */
    public function dispose() {
        if ($this->database_manager) {
            $this->database_manager->dispose();
            $this->database_manager = null;
        }
        $this->columns   = array();
    }

    /**
     * Returns database server info array
     * @return array
     */
    public abstract function get_server_info();

    /**
     * Returns supported query parameter types
     * @return bitmask
     */
    protected abstract function allowed_param_types();

    /**
     * Returns last error reported by database engine.
     */
    public abstract function get_last_error();

    /**
     * Report database error somewhere
     * TODO: do we need some message with hints?
     * @param string $sql query which caused problems
     * @param array $params optional query parameters
     * @param mixed $obj optional library specific object
     */
    protected function report_error($sql, array $params=null, $obj=null) {
        debugging($this->get_last_error() .'<br /><br />'. s($sql).'<br /> ['.var_export($params, true).']');
    }

    /**
     * Constructs IN() or = sql fragment
     * @param mixed $items single or array of values
     * @param int $type bound param type
     * @param string named param placeholder start
     * @param bool true means equal, false not equal
     * @return array - $sql and $params
     */
    public function get_in_or_equal($items, $type=SQL_PARAMS_QM, $start='param0000', $equal=true) {
        if ($type == SQL_PARAMS_QM) {
            if (!is_array($items) or count($items) == 1) {
                $sql = $equal ? '= ?' : '<> ?';
                $items = (array)$items;
                $params = array_values($items);
            } else {
                if ($equal) {
                    $sql = 'IN ('.implode(',', array_fill(0, count($items), '?')).')';
                } else {
                    $sql = 'NOT IN ('.implode(',', array_fill(0, count($items), '?')).')';
                }
                $params = array_values($items);
            }

        } else if ($type == SQL_PARAMS_NAMED) {
            if (!is_array($items)){
                $sql = $equal ? "= :$start" : "<> :$start";
                $params = array($start=>$items);
            } else if (count($items) == 1) {
                $sql = $equal ? "= :$start" : "<> :$start";
                $item = reset($items);
                $params = array($start=>$item);
            } else {
                $params = array();
                $sql = array();
                foreach ($items as $item) {
                    $params[$start] = $item;
                    $sql[] = ':'.$start++;
                }
                if ($equal) {
                    $sql = 'IN ('.implode(',', $sql).')';
                } else {
                    $sql = 'NOT IN ('.implode(',', $sql).')';
                }
            }

        } else {
            error('todo: type not implemented');
        }
        return array($sql, $params);
    }

    /**
     * Normalizes sql query parameters and verifies parameters.
     * @param string $sql query or part of it
     * @param array $params query parameters
     */
    public function fix_sql_params($sql, array $params=null) {
        $params = (array)$params; // mke null array if needed
        $allowed_types = $this->allowed_param_types();

        // convert table names
        $sql = preg_replace('/\{([a-z][a-z0-9_]*)\}/', $this->prefix.'$1', $sql);

        // NICOLAS C: Fixed regexp for negative backwards lookahead of double colons. Thanks for Sam Marshall's help
        $named_count = preg_match_all('/(?<!:):[a-z][a-z0-9_]*/', $sql, $named_matches); // :: used in pgsql casts
        $dollar_count = preg_match_all('/\$[1-9][0-9]*/', $sql, $dollar_matches);
        $q_count     = substr_count($sql, '?');

        $count = 0;

        if ($named_count) {
            $type = SQL_PARAMS_NAMED;
            $count = $named_count;

        }
        if ($dollar_count) {
            if ($count) {
                error('ERROR: Mixed types of sql query parameters!!');
            }
            $type = SQL_PARAMS_DOLLAR;
            $count = $dollar_count;

        }
        if ($q_count) {
            if ($count) {
                error('ERROR: Mixed types of sql query parameters!!');
            }
            $type = SQL_PARAMS_QM;
            $count = $q_count;

        }

        if (!$count) {
             // ignore params
            if ($allowed_types & SQL_PARAMS_NAMED) {
                return array($sql, array(), SQL_PARAMS_NAMED);
            } else if ($allowed_types & SQL_PARAMS_QM) {
                return array($sql, array(), SQL_PARAMS_QM);
            } else {
                return array($sql, array(), SQL_PARAMS_DOLLAR);
            }
        }

        if ($count > count($params)) {
            error('ERROR: Incorrect number of query parameters!!');
        }

        if ($type & $allowed_types) { // bitwise AND
            if ($count == count($params)) {
                if ($type == SQL_PARAMS_QM) {
                    return array($sql, array_values($params), SQL_PARAMS_QM); // 0-based array required
                } else {
                    //better do the validation of names below
                }
            }
            // needs some fixing or validation - there might be more params than needed
            $target_type = $type;

        } else {
            $target_type = $allowed_types;
        }

        if ($type == SQL_PARAMS_NAMED) {
            $finalparams = array();
            foreach ($named_matches[0] as $key) {
                $key = trim($key, ':');
                if (!array_key_exists($key, $params)) {
                    error('ERROR: missing param "'.$key.'" in query');
                }
                $finalparams[$key] = $params[$key];
            }
            if ($count != count($finalparams)) {
                error('ERROR: duplicate parameter name in query');
            }

            if ($target_type & SQL_PARAMS_QM) {
                $sql = preg_replace('/(?<!:):[a-z][a-z0-9_]*/', '?', $sql);
                return array($sql, array_values($finalparams), SQL_PARAMS_QM); // 0-based required
            } else if ($target_type & SQL_PARAMS_NAMED) {
                return array($sql, $finalparams, SQL_PARAMS_NAMED);
            } else {  // $type & SQL_PARAMS_DOLLAR
                error('Pg $1, $2 bound syntax not supported yet :-(');
            }

        } else if ($type == SQL_PARAMS_DOLLAR) {
            error('Pg $1, $2 bound syntax not supported yet :-(');

        } else { // $type == SQL_PARAMS_QM
            if (count($params) != $count) {
                $params = array_slice($params, 0, $count);
            }

            if ($target_type & SQL_PARAMS_QM) {
                return array($sql, array_values($params), SQL_PARAMS_QM); // 0-based required
            } else if ($target_type & SQL_PARAMS_NAMED) {
                $finalparams = array();
                $pname = 'param00000';
                $parts = explode('?', $sql);
                $sql = array_shift($parts);
                foreach ($parts as $part) {
                    $param = array_shift($params);
                    $pname++;
                    $sql .= ':'.$pname.$part;
                    $finalparams[$pname] = $param;
                }
                return array($sql, $finalparams, SQL_PARAMS_NAMED);
            } else {  // $type & SQL_PARAMS_DOLLAR
                error('Pg $1, $2 bound syntax not supported yet :-(');
            }
        }
    }

    /**
     * Return tables in database with current prefix
     * @return array of table names
     */
    public abstract function get_tables();

    /**
     * Return table indexes
     * @return array of arrays
     */
    public abstract function get_indexes($table);

    /**
     * Returns datailed information about columns in table. This information is cached internally.
     * @param string $table name
     * @param bool $usecache
     * @return array array of database_column_info objects indexed with column names
     */
    public abstract function get_columns($table, $usecache=true);

    /**
     * Reset internal column details cache
     * @param string $table - empty means all, or one if name of table given
     * @return void
     */
    public function reset_columns() {
        $this->columns[] = array();
    }

    /**
     * Returns sql generator used for db manipulation.
     * Used mostly in upgrade.php scripts.
     * @return object database_manager instance
     */
    public function get_manager() {
        global $CFG;

        if (!$this->database_manager) {
            require_once($CFG->libdir.'/ddllib.php');
            require_once($CFG->libdir.'/ddl/database_manager.php');

            $classname = $this->get_dbfamily().'_sql_generator';
            require_once("$CFG->libdir/ddl/$classname.php");
            $generator = new $classname($this);

            $this->database_manager = new database_manager($this, $generator);
        }
        return $this->database_manager;
    }

    /**
     * Attempt to change db encoding toUTF-8 if poossible
     * @return bool success
     */
    public function change_db_encoding() {
        return false;
    }

    /**
     * Is db in unicode mode?
     * @return bool
     */
    public function setup_is_unicodedb() {
        return true;
    }

    /**
     * Enable/disable very detailed debugging
     * TODO: do we need levels?
     * @param bool $state
     */
    public abstract function set_debug($state);

    /**
     * Returns debug status
     * @return bool $state
     */
    public abstract function get_debug();

    /**
     * Enable/disable detailed sql logging
     * TODO: do we need levels?
     * @param bool $state
     */
    public abstract function set_logging($state);

    /**
     * Do NOT use in code, to be used by database_manager only!
     * @param string $sql query
     * @return bool success
     */
    public abstract function change_database_structure($sql);

    /**
     * Execute general sql query. Should be used only when no other method suitable.
     * Do NOT use this to make changes in db structure, use database_manager::execute_sql() instead!
     * @param string $sql query
     * @param array $params query parameters
     * @return bool success
     */
    public abstract function execute($sql, array $params=null);

    /**
     * Get a number of records as a moodle_recordset.
     *
     * Selects records from the table $table.
     *
     * If specified, only records meeting $conditions.
     *
     * If specified, the results will be sorted as specified by $sort. This
     * is added to the SQL as "ORDER BY $sort". Example values of $sort
     * mightbe "time ASC" or "time DESC".
     *
     * If $fields is specified, only those fields are returned.
     *
     * Since this method is a little less readable, use of it should be restricted to
     * code where it's possible there might be large datasets being returned.  For known
     * small datasets use get_records - it leads to simpler code.
     *
     * If you only want some of the records, specify $limitfrom and $limitnum.
     * The query will skip the first $limitfrom records (according to the sort
     * order) and then return the next $limitnum records. If either of $limitfrom
     * or $limitnum is specified, both must be present.
     *
     * The return value is a moodle_recordset
     * if the query succeeds. If an error occurrs, false is returned.
     *
     * @param string $table the table to query.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @param string $sort an order to sort the results in (optional, a valid SQL ORDER BY parameter).
     * @param string $fields a comma separated list of fields to return (optional, by default all fields are returned).
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an moodle_recorset object, or false if an error occured.
     */
    public function get_recordset($table, array $conditions=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->get_recordset_select($table, $select, $params, $sort, $fields, $limitfrom, $limitnum);
    }

    /**
     * Get a number of records a moodle_recordset.
     *
     * Only records where $field takes one of the values $values are returned.
     * $values should be a comma-separated list of values, for example "4,5,6,10"
     * or "'foo','bar','baz'".
     *
     * Other arguments and the return type as for @see function get_recordset.
     *
     * @param string $table the table to query.
     * @param string $field a field to check (optional).
     * @param array $values array of values the field must have
     * @param string $sort an order to sort the results in (optional, a valid SQL ORDER BY parameter).
     * @param string $fields a comma separated list of fields to return (optional, by default all fields are returned).
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an moodle_recorset object, or false if an error occured.
     */
    public function get_recordset_list($table, $field, array $values, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        $params = array();
        $select = array();
        foreach ($values as $value) {
            if (is_bool($value)) {
                $value = (int)$value;
            }
            if (is_null($value)) {
                $select[] = "$field IS NULL";
            } else {
                $select[] = "$field = ?";
                $params[] = $value;
            }
        }
        $select = implode(" AND ", $select);
        return get_recordset_select($table, $select, $params, $sort, $fields, $limitfrom, $limitnum);
    }

    /**
     * Get a number of records a moodle_recordset.
     *
     * If given, $select is used as the SELECT parameter in the SQL query,
     * otherwise all records from the table are returned.
     *
     * Other arguments and the return type as for @see function get_recordset.
     *
     * @param string $table the table to query.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
     * @param array $params array of sql parameters
     * @param string $sort an order to sort the results in (optional, a valid SQL ORDER BY parameter).
     * @param string $fields a comma separated list of fields to return (optional, by default all fields are returned).
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an moodle_recorset object, or false if an error occured.
     */
    public function get_recordset_select($table, $select, array $params=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        if ($select) {
            $select = "WHERE $select";
        }
        if ($sort) {
            $sort = " ORDER BY $sort";
        }
        return $this->get_recordset_sql("SELECT * FROM {$this->prefix}$table $select $sort", $params, $limitfrom, $limitnum);
    }

    /**
     * Get a number of records as an moodle_recordset.  $sql must be a complete SQL query.
     * Since this method is a little less readable, use of it should be restricted to
     * code where it's possible there might be large datasets being returned.  For known
     * small datasets use get_records_sql - it leads to simpler code.
     *
     * The return type is as for @see function get_recordset.
     *
     * @param string $sql the SQL select query to execute.
     * @param array $params array of sql parameters
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an moodle_recorset object, or false if an error occured.
     */
    public abstract function get_recordset_sql($sql, array $params=null, $limitfrom=0, $limitnum=0);

    /**
     * Get a number of records as an array of objects.
     *
     * If the query succeeds and returns at least one record, the
     * return value is an array of objects, one object for each
     * record found. The array key is the value from the first
     * column of the result set. The object associated with that key
     * has a member variable for each column of the results.
     *
     * @param string $table the table to query.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @param string $sort an order to sort the results in (optional, a valid SQL ORDER BY parameter).
     * @param string $fields a comma separated list of fields to return (optional, by default
     *   all fields are returned). The first field will be used as key for the
     *   array so must be a unique field such as 'id'.
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an array of objects, or empty array if no records were found, or false if an error occured.
     */
    public function get_records($table, array $conditions=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->get_records_select($table, $select, $params, $sort, $fields, $limitfrom, $limitnum);
    }

    /**
     * Get a number of records as an array of objects.
     *
     * Return value as for @see function get_records.
     *
     * @param string $table The database table to be checked against.
     * @param string $field The field to search
     * @param string $values array of values
     * @param string $sort Sort order (as valid SQL sort parameter)
     * @param string $fields A comma separated list of fields to be returned from the chosen table. If specified,
     *   the first field should be a unique one such as 'id' since it will be used as a key in the associative
     *   array.
     * @return mixed an array of objects, or empty array if no records were found, or false if an error occured.
     */
    public function get_records_list($table, $field, array $values=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        $params = array();
        $select = array();
        $values = (array)$values;
        foreach ($values as $value) {
            if (is_bool($value)) {
                $value = (int)$value;
            }
            if (is_null($value)) {
                $select[] = "$field IS NULL";
            } else {
                $select[] = "$field = ?";
                $params[] = $value;
            }
        }
        $select = implode(" AND ", $select);
        return $this->get_records_select($table, $select, $params, $sort, $fields, $limitfrom, $limitnum);
    }

    /**
     * Get a number of records as an array of objects.
     *
     * Return value as for @see function get_records.
     *
     * @param string $table the table to query.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
     * @param array $params array of sql parameters
     * @param string $sort an order to sort the results in (optional, a valid SQL ORDER BY parameter).
     * @param string $fields a comma separated list of fields to return
     *   (optional, by default all fields are returned). The first field will be used as key for the
     *   array so must be a unique field such as 'id'.
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an array of objects, or empty array if no records were found, or false if an error occured.
     */
    public function get_records_select($table, $select, array $params=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        if ($select) {
            $select = "WHERE $select";
        }
        if ($sort) {
            $sort = " ORDER BY $sort";
        }
        return $this->get_records_sql("SELECT $fields FROM {$this->prefix}$table $select $sort", $params, $limitfrom, $limitnum);
    }

    /**
     * Get a number of records as an array of objects.
     *
     * Return value as for @see function get_records.
     *
     * @param string $sql the SQL select query to execute. The first column of this SELECT statement
     *   must be a unique value (usually the 'id' field), as it will be used as the key of the
     *   returned array.
     * @param array $params array of sql parameters
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an array of objects, or empty array if no records were found, or false if an error occured.
     */
    public abstract function get_records_sql($sql, array $params=null, $limitfrom=0, $limitnum=0);

    /**
     * Get the first two columns from a number of records as an associative array.
     *
     * Arguments as for @see function get_recordset.
     *
     * If no errors occur the return value
     * is an associative whose keys come from the first field of each record,
     * and whose values are the corresponding second fields.
     * False is returned if an error occurs.
     *
     * @param string $table the table to query.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @param string $sort an order to sort the results in (optional, a valid SQL ORDER BY parameter).
     * @param string $fields a comma separated list of fields to return - the number of fields should be 2!
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an associative array, or false if an error occured.
     */
    public function get_records_menu($table, array $conditions=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        $menu = array();
        if ($records = $this->get_records($table, $conditions, $sort, $fields, $limitfrom, $limitnum)) {
            foreach ($records as $record) {
                $record = (array)$record;
                $key   = array_shift($record);
                $value = array_shift($record);
                $menu[$key] = $value;
            }
        }
        return $menu;
    }

    /**
     * Get the first two columns from a number of records as an associative array.
     *
     * Arguments as for @see function get_recordset_select.
     * Return value as for @see function get_records_menu.
     *
     * @param string $table The database table to be checked against.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
     * @param array $params array of sql parameters
     * @param string $sort Sort order (optional) - a valid SQL order parameter
     * @param string $fields A comma separated list of fields to be returned from the chosen table - the number of fields should be 2!
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an associative array, or false if an error occured.
     */
    public function get_records_select_menu($table, $select, array $params=null, $sort='', $fields='*', $limitfrom=0, $limitnum=0) {
        $menu = array();
        if ($records = $this->get_records_select($table, $select, $params, $sort, $fields, $limitfrom, $limitnum)) {
            foreach ($records as $record) {
                $key   = array_unshift($record);
                $value = array_unshift($record);
                $menu[$key] = $value;
            }
        }
        return $menu;
    }

    /**
     * Get the first two columns from a number of records as an associative array.
     *
     * Arguments as for @see function get_recordset_sql.
     * Return value as for @see function get_records_menu.
     *
     * @param string $sql The SQL string you wish to be executed.
     * @param array $params array of sql parameters
     * @param int $limitfrom return a subset of records, starting at this point (optional, required if $limitnum is set).
     * @param int $limitnum return a subset comprising this many records (optional, required if $limitfrom is set).
     * @return mixed an associative array, or false if an error occured.
     */
    public function get_records_sql_menu($sql, array $params=null, $limitfrom=0, $limitnum=0) {
        $menu = array();
        if ($records = $this->get_records_sql($sql, $params, $limitfrom, $limitnum)) {
            foreach ($records as $record) {
                $key   = array_unshift($record);
                $value = array_unshift($record);
                $menu[$key] = $value;
            }
        }
        return $menu;
    }

    /**
     * Get a single database record as an object
     *
     * @param string $table The table to select from.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @param string $fields A comma separated list of fields to be returned from the chosen table.
     * @param bool $ignoremultiple ignore multiple records if found
     * @return mixed a fieldset object containing the first mathcing record, or false if none found.
     */
    public function get_record($table, array $conditions, $fields='*', $ignoremultiple=false) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->get_record_select($table, $select, $params, $fields, $ignoremultiple);
    }

    /**
     * Get a single database record as an object
     *
     * @param string $table The database table to be checked against.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
     * @param array $params array of sql parameters
     * @param string $fields A comma separated list of fields to be returned from the chosen table.
     * @param bool $ignoremultiple ignore multiple records if found
     * @return mixed a fieldset object containing the first mathcing record, or false if none found.
     */
    public function get_record_select($table, $select, array $params=null, $fields='*', $ignoremultiple=false) {
        if ($select) {
            $select = "WHERE $select";
        }
        return $this->get_record_sql("SELECT $fields FROM {$this->prefix}$table $select", $params, $ignoremultiple);
    }

    /**
     * Get a single record as an object using an SQL statement
     *
     * The SQL statement should normally only return one record. In debug mode
     * you will get a warning if more records are found. In non-debug mode,
     * it just returns the first record.
     *
     * Use get_records_sql() if more matches possible!
     *
     * @param string $sql The SQL string you wish to be executed, should normally only return one record.
     * @param array $params array of sql parameters
     * @param bool $ignoremultiple ignore multiple records if found
     * @return mixed a fieldset object containing the first mathcing record, or false if none found.
     */
    public function get_record_sql($sql, array $params=null, $ignoremultiple=false) {
        $count = $ignoremultiple ? 1 : 2;
        if (!$mrs = $this->get_recordset_sql($sql, $params, 0, $count)) {
            return false;
        }
        if (!$mrs->valid()) {
            $mrs->close();
            return false;
        }

        $return = (object)$mrs->current();

        $mrs->next();
        if (!$ignoremultiple and $mrs->valid()) {
            debugging('Error: mdb->get_record() found more than one record!');
        }

        $mrs->close();
        return $return;
    }

    /**
     * Get a single value from a table row where all the given fields match the given values.
     *
     * @param string $table the table to query.
     * @param string $return the field to return the value of.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @return mixed the specified value, or false if an error occured.
     */
    public function get_field($table, $return, array $conditions) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->get_field_select($table, $return, $select, $params);
    }

    /**
     * Get a single value from a table row.
     *
     * @param string $table the table to query.
     * @param string $return the field to return the value of.
     * @param string $select A fragment of SQL to be used in a where clause returning one row with one column
     * @param array $params array of sql parameters
     * @return mixed the specified value, or false if an error occured.
     */
    public function get_field_select($table, $return, $select, array $params=null) {
        if ($select) {
            $select = "WHERE $select";
        }
        return $this->get_field_sql("SELECT $return FROM {$this->prefix}$table $select", $params);
    }

    /**
     * Get a single value from a table.
     *
     * @param string $table the table to query.
     * @param string $return the field to return the value of.
     * @param string $sql The SQL query returning one row with one column
     * @param array $params array of sql parameters
     * @return mixed the specified value, or false if an error occured.
     */
    public function get_field_sql($sql, array $params=null) {
        if ($mrs = $this->get_recordset_sql($sql, $params, 0, 1)) {
            if ($mrs->valid()) {
                $record = $mrs->current();
                return reset($record); // first column
            }
            $mrs->close();
        }
        return false;
    }

    /**
     * Selects rows and return values of chosen field as array.
     *
     * @param string $table the table to query.
     * @param string $return the field we are intered in
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
     * @param array $params array of sql parameters
     * @return mixed array of values or false if an error occured
     */
    public function get_fieldset_select($table, $return, $select, array $params=null) {
        if ($select) {
            $select = "WHERE $select";
        }
        return $this->get_fieldset_sql("SELECT $return FROM {$this->prefix}$table $select", $params);
    }

    /**
     * Selects rows and return values of first column as array.
     *
     * @param string $sql The SQL query
     * @param array $params array of sql parameters
     * @return mixed array of values or false if an error occured
     */
    public abstract function get_fieldset_sql($sql, array $params=null);

    /**
     * Insert new record into database, as fast as possible, no safety checks, lobs not supported.
     * @param string $table name
     * @param mixed $params data record as object or array
     * @param bool $returnit return it of inserted record
     * @param bool $bulk true means repeated inserts expected
     * @return mixed success or new id
     */
    public abstract function insert_record_raw($table, $params, $returnid=true, $bulk=false);

    /**
     * Insert a record into a table and return the "id" field if required,
     * Some conversions and safety checks are carried out. Lobs are supported.
     * If the return ID isn't required, then this just reports success as true/false.
     * $data is an object containing needed data
     * @param string $table The database table to be inserted into
     * @param object $data A data object with values for one or more fields in the record
     * @param bool $returnid Should the id of the newly created record entry be returned? If this option is not requested then true/false is returned.
     * @return mixed success or new ID
     */
    public abstract function insert_record($table, $dataobject, $returnid=true, $bulk=false);

    /**
     * Update record in database, as fast as possible, no safety checks, lobs not supported.
     * @param string $table name
     * @param mixed $params data record as object or array
     * @param bool true means repeated updates expected
     * @return bool success
     */
    public abstract function update_record_raw($table, $params, $bulk=false);

    /**
     * Update a record in a table
     *
     * $dataobject is an object containing needed data
     * Relies on $dataobject having a variable "id" to
     * specify the record to update
     *
     * @param string $table The database table to be checked against.
     * @param object $dataobject An object with contents equal to fieldname=>fieldvalue. Must have an entry for 'id' to map to the table specified.
     * @param bool true means repeated updates expected
     * @return bool success
     */
    public abstract function update_record($table, $dataobject, $bulk=false);


    /**
     * Set a single field in every table row where all the given conditions met.
     *
     * @param string $table The database table to be checked against.
     * @param string $newfield the field to set.
     * @param string $newvalue the value to set the field to.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @return bool success
     */
    public function set_field($table, $newfield, $newvalue, array $conditions=null) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->set_field_select($table, $newfield, $newvalue, $select, $params);
    }

    /**
     * Set a single field in every table row where the select statement evaluates to true.
     *
     * @param string $table The database table to be checked against.
     * @param string $newfield the field to set.
     * @param string $newvalue the value to set the field to.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call.
     * @param array $params array of sql parameters
     * @return bool success
     */
    public abstract function set_field_select($table, $newfield, $newvalue, $select, array $params=null);


    /**
     * Count the records in a table where all the given conditions met.
     *
     * @param string $table The table to query.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @return int The count of records returned from the specified criteria.
     */
    public function count_records($table, array $conditions=null, array $params=null) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->count_records_select($table, $select, $params);
    }

    /**
     * Count the records in a table which match a particular WHERE clause.
     *
     * @param string $table The database table to be checked against.
     * @param string $select A fragment of SQL to be used in a WHERE clause in the SQL call.
     * @param array $params array of sql parameters
     * @param string $countitem The count string to be used in the SQL call. Default is COUNT('x').
     * @return int The count of records returned from the specified criteria.
     */
    public function count_records_select($table, $select, array $params=null, $countitem="COUNT('x')") {
        if ($select) {
            $select = "WHERE $select";
        }
        return $this->count_records_sql("SELECT $countitem FROM {$this->prefix}$table $select", $params);
    }

    /**
     * Get the result of a SQL SELECT COUNT(...) query.
     *
     * Given a query that counts rows, return that count. (In fact,
     * given any query, return the first field of the first record
     * returned. However, this method should only be used for the
     * intended purpose.) If an error occurrs, 0 is returned.
     *
     * @param string $sql The SQL string you wish to be executed.
     * @param array $params array of sql parameters
     * @return int the count. If an error occurrs, 0 is returned.
     */
    public function count_records_sql($sql, array $params=null) {
        if ($count = $this->get_field_sql($sql, $params)) {
            return $count;
        } else {
            return 0;
        }
    }

    /**
     * Test whether a record exists in a table where all the given conditions met.
     *
     * The record to test is specified by giving up to three fields that must
     * equal the corresponding values.
     *
     * @param string $table The table to check.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @return bool true if a matching record exists, else false.
     */
    public function record_exists($table, array $conditions) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->record_exists_select($table, $select, $params);
    }

    /**
     * Test whether any records exists in a table which match a particular WHERE clause.
     *
     * @param string $table The database table to be checked against.
     * @param string $select A fragment of SQL to be used in a WHERE clause in the SQL call.
     * @param array $params array of sql parameters
     * @return bool true if a matching record exists, else false.
     */
    public function record_exists_select($table, $select, array $params=null) {
        if ($select) {
            $select = "WHERE $select";
        }
        return $this->record_exists_sql("SELECT 'x' FROM {$this->prefix}$table $select", $params);
    }

    /**
     * Test whether a SQL SELECT statement returns any records.
     *
     * This function returns true if the SQL statement executes
     * without any errors and returns at least one record.
     *
     * @param string $sql The SQL statement to execute.
     * @param array $params array of sql parameters
     * @return bool true if the SQL executes without errors and returns at least one record.
     */
    public function record_exists_sql($sql, array $params=null) {
        if ($mrs = $this->get_recordset_sql($sql, $params, 0, 1)) {
            $return = $mrs->valid();
            $mrs->close();
            return $return;
        }
        return false;
    }

    /**
     * Delete the records from a table where all the given conditions met.
     *
     * @param string $table the table to delete from.
     * @param array $conditions optional array $fieldname=>requestedvalue with AND in between
     * @return returns success.
     */
    public function delete_records($table, array $conditions) {
        list($select, $params) = $this->where_clause($conditions);
        return $this->delete_records_select($table, $select, $params);
    }

    /**
     * Delete one or more records from a table
     *
     * @param string $table The database table to be checked against.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call (used to define the selection criteria).
     * @param array $params array of sql parameters
     * @return returns success.
     */
    public abstract function delete_records_select($table, $select, array $params=null);



/// sql contructs
    /**
     * Returns the SQL text to be used in order to perform one bitwise AND operation
     * between 2 integers.
     * @param integer int1 first integer in the operation
     * @param integer int2 second integer in the operation
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_bitand($int1, $int2) {
        return '((' . $int1 . ') & (' . $int2 . '))';
    }

    /**
     * Returns the SQL text to be used in order to perform one bitwise NOT operation
     * with 1 integer.
     * @param integer int1 integer in the operation
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_bitnot($int1) {
        return '(~(' . $int1 . '))';
    }

    /**
     * Returns the SQL text to be used in order to perform one bitwise OR operation
     * between 2 integers.
     * @param integer int1 first integer in the operation
     * @param integer int2 second integer in the operation
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_bitor($int1, $int2) {
        return '((' . $int1 . ') | (' . $int2 . '))';
    }

    /**
     * Returns the SQL text to be used in order to perform one bitwise XOR operation
     * between 2 integers.
     * @param integer int1 first integer in the operation
     * @param integer int2 second integer in the operation
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_bitxor($int1, $int2) {
        return '((' . $int1 . ') ^ (' . $int2 . '))';
    }

    /**
     * Returns the SQL to be used in order to CAST one CHAR column to INTEGER.
     *
     * Be aware that the CHAR column you're trying to cast contains really
     * int values or the RDBMS will throw an error!
     *
     * @param string fieldname the name of the field to be casted
     * @param boolean text to specify if the original column is one TEXT (CLOB) column (true). Defaults to false.
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_cast_char2int($fieldname, $text=false) {
        return ' ' . $fieldname . ' ';
    }

    /**
     * Returns the SQL text to be used to compare one TEXT (clob) column with
     * one varchar column, because some RDBMS doesn't support such direct
     * comparisons.
     * @param string fieldname the name of the TEXT field we need to order by
     * @param string number of chars to use for the ordering (defaults to 32)
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_compare_text($fieldname, $numchars=32) {
        return $this->sql_order_by_text($fieldname, $numchars);
    }

    /**
     * Returns the proper SQL to do CONCAT between the elements passed
     * Can take many parameters - just a passthrough to $db->Concat()
     *
     * @param string $element
     * @return string
     */
    public abstract function sql_concat();

    /**
     * Returns the proper SQL to do CONCAT between the elements passed
     * with a given separator
     *
     * @uses $db
     * @param string $separator
     * @param array  $elements
     * @return string
     */
    public abstract function sql_concat_join($separator="' '", $elements=array());

    /**
     * Returns the proper SQL (for the dbms in use) to concatenate $firstname and $lastname
     *
     * @param string $firstname User's first name
     * @param string $lastname User's last name
     * @return string
     */
    function sql_fullname($first='firstname', $last='lastname') {
        return $this->sql_concat($first, "' '", $last);
    }

    /**
     * Returns the proper SQL to do LIKE in a case-insensitive way
     *
     * Note the LIKE are case sensitive for Oracle. Oracle 10g is required to use
     * the caseinsensitive search using regexp_like() or NLS_COMP=LINGUISTIC :-(
     * See http://docs.moodle.org/en/XMLDB_Problems#Case-insensitive_searches
     *
     * @return string
     */
    public function sql_ilike() {
        return 'LIKE';
    }

    /**
     * Returns the SQL text to be used to order by one TEXT (clob) column, because
     * some RDBMS doesn't support direct ordering of such fields.
     * Note that the use or queries being ordered by TEXT columns must be minimised,
     * because it's really slooooooow.
     * @param string fieldname the name of the TEXT field we need to order by
     * @param string number of chars to use for the ordering (defaults to 32)
     * @return string the piece of SQL code to be used in your statement.
     */
    public function sql_order_by_text($fieldname, $numchars=32) {
        return $fieldname;
    }

    /**
     * Returns the proper substr() function for each DB
     * Relies on ADOdb $db->substr property
     */
    public abstract function sql_substr();

    public function where_clause(array $conditions=null) {
        $allowed_types = $this->allowed_param_types();
        if (empty($conditions)) {
            return array('', array());
        }
        $where = array();
        $params = array();
        foreach ($conditions as $key=>$value) {
            if (is_null($value)) {
                $where[] = "$key IS NULL";
            } else {
                if ($allowed_types & SQL_PARAMS_NAMED) {
                    $where[] = "$key = :$key";
                    $params[$key] = $value;
                } else {
                    $where[] = "$key = ?";
                    $params[] = $value;
                }
            }
        }
        $where = implode(" AND ", $where);
        return array($where, $params);
    }

    /**
     * Returns the empty string char used by every supported DB. To be used when
     * we are searching for that values in our queries. Only Oracle uses this
     * for now (will be out, once we migrate to proper NULLs if that days arrives)
     */
    function sql_empty() {
        return '';
    }

    /**
     * Returns the proper SQL to know if one field is empty.
     *
     * Note that the function behavior strongly relies on the
     * parameters passed describing the field so, please,  be accurate
     * when speciffying them.
     *
     * Also, note that this function is not suitable to look for
     * fields having NULL contents at all. It's all for empty values!
     *
     * This function should be applied in all the places where conditins of
     * the type:
     *
     *     ... AND fieldname = '';
     *
     * are being used. Final result should be:
     *
     *     ... AND ' . sql_isempty('tablename', 'fieldname', true/false, true/false);
     *
     * (see parameters description below)
     *
     * @param string $tablename name of the table (without prefix). Not used for now but can be
     *                          necessary in the future if we want to use some introspection using
     *                          meta information against the DB. /// TODO ///
     * @param string $fieldname name of the field we are going to check
     * @param boolean $nullablefield to specify if the field us nullable (true) or no (false) in the DB
     * @param boolean $textfield to specify if it is a text (also called clob) field (true) or a varchar one (false)
     * @return string the sql code to be added to check for empty values
     */
    public function sql_isempty($tablename, $fieldname, $nullablefield, $textfield) {
        return " $fieldname = '' ";
    }

    /**
     * Returns the proper SQL to know if one field is not empty.
     *
     * Note that the function behavior strongly relies on the
     * parameters passed describing the field so, please,  be accurate
     * when speciffying them.
     *
     * This function should be applied in all the places where conditions of
     * the type:
     *
     *     ... AND fieldname != '';
     *
     * are being used. Final result should be:
     *
     *     ... AND ' . sql_isnotempty('tablename', 'fieldname', true/false, true/false);
     *
     * (see parameters description below)
     *
     * @param string $tablename name of the table (without prefix). Not used for now but can be
     *                          necessary in the future if we want to use some introspection using
     *                          meta information against the DB. /// TODO ///
     * @param string $fieldname name of the field we are going to check
     * @param boolean $nullablefield to specify if the field us nullable (true) or no (false) in the DB
     * @param boolean $textfield to specify if it is a text (also called clob) field (true) or a varchar one (false)
     * @return string the sql code to be added to check for non empty values
     */
    public function sql_isnotempty($tablename, $fieldname, $nullablefield, $textfield) {
        return ' ( NOT ' . $this->sql_isempty($tablename, $fieldname, $nullablefield, $textfield) . ') ';
    }

    /**
     * Does this driver suppoer regex syntax when searching
     */
    public function sql_regex_supported() {
        return false;
    }

    /**
     * Return regex positive or negative match sql
     * @param bool $positivematch
     * @return string or empty if not supported
     */
    public function sql_regex($positivematch=true) {
        return '';
    }

/// transactions
    /**
     * on DBs that support it, switch to transaction mode and begin a transaction
     * you'll need to ensure you call commit_sql() or your changes *will* be lost.
     *
     * this is _very_ useful for massive updates
     */
    public function begin_sql() {
        return true;
    }

    /**
     * on DBs that support it, commit the transaction
     */
    public function commit_sql() {
        return true;
    }

    /**
     * on DBs that support it, rollback the transaction
     */
    public function rollback_sql() {
        return true;
    }
}