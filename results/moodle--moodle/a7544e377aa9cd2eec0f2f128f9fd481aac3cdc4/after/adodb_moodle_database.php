<?php  //$Id$

require_once($CFG->libdir.'/dml/moodle_database.php');
require_once($CFG->libdir.'/dml/adodb_moodle_recordset.php');

/**
 * Abstract moodle database class
 * @package dmlib
 */
abstract class adodb_moodle_database extends moodle_database {

    protected $db;

    /**
     * Returns localised database type name
     * Note: can be used before connect()
     * @return string
     */
    public function get_name() {
        $dbtype = $this->get_dbtype();
        return get_string($dbtype, 'install');
    }

    /**
     * Returns db related part of config.php
     * Note: can be used before connect()
     * @return string
     */
    public function export_dbconfig() {
        $cfg = new stdClass();
        $cfg->dbtype  = $this->get_dbtype();
        $cfg->library = 'adodb';
        $cfg->dbhost  = $this->dbhost;
        $cfg->dbname  = $this->dbname;
        $cfg->dbuser  = $this->dbuser;
        $cfg->dbpass  = $this->dbpass;
        $cfg->prefix  = $this->prefix;

        return $cfg;
    }

    //TODO: preconfigure_dbconnection(): Decide if this should be declared as abstract because all adodb drivers will need it
    /**
     * Adodb preconnection routines, ususally sets up needed defines;
     */
    protected function preconfigure_dbconnection() {
        // empty
    }

    public function connect($dbhost, $dbuser, $dbpass, $dbname, $dbpersist, $prefix, array $dboptions=null) {
        $this->dbhost    = $dbhost;
        $this->dbuser    = $dbuser;
        $this->dbpass    = $dbpass;
        $this->dbname    = $dbname;
        $this->dbpersist = $dbpersist;
        $this->prefix    = $prefix;
        $this->dboptions = (array)$dboptions;

        global $CFG;

        $this->preconfigure_dbconnection();

        require_once($CFG->libdir.'/adodb/adodb.inc.php');

        $this->db = ADONewConnection($this->get_dbtype());

        global $db; $db = $this->db; // TODO: BC only for now

        // See MDL-6760 for why this is necessary. In Moodle 1.8, once we start using NULLs properly,
        // we probably want to change this value to ''.
        $this->db->null2null = 'A long random string that will never, ever match something we want to insert into the database, I hope. \'';

        if (!isset($this->dbpersist) or !empty($this->dbpersist)) {    // Use persistent connection (default)
            if (!$this->db->PConnect($this->dbhost, $this->dbuser, $this->dbpass, $this->dbname)) {
                return false;
            }
        } else {                                                     // Use single connection
            if (!$this->db->Connect($this->dbhost, $this->dbuser, $this->dbpass, $this->dbname)) {
                return false;
            }
        }
        $this->configure_dbconnection();
        return true;
    }

    //TODO: configure_dbconnection(): Decide if this should be declared as abstract because all adodb drivers will need it
    /**
     * Adodb post connection routines, usually sets up encoding,e tc.
     */
    protected function configure_dbconnection() {
        // empty
    }

    //TODO: make all dblibraries return this info in a structured way (new server_info class or so, like database_column_info class)
    /**
     * Returns database server info array
     * @return array
     */
    public function get_server_info() {
        return $this->db->ServerInfo();
    }

    /**
     * Return tables in database with WITHOUT current prefix
     * @return array of table names in lowercase and without prefix
     */
    public function get_tables() {
        $metatables = $this->db->MetaTables();
        $tables = array();
        foreach ($metatables as $table) {
            $table = strtolower($table);
            if (strpos($table, $this->prefix) === 0 || empty($this->prefix)) {
                $tablename = substr($table, strlen($this->prefix));
                $tables[$tablename] = $tablename;
            }
        }
        return $tables;
    }

    /**
     * Return table indexes
     * @return array of arrays
     */
    public function get_indexes($table) {
        if (!$indexes = $this->db->MetaIndexes($this->prefix.$table)) {
            return array();
        }
        $indexes = array_change_key_case($indexes, CASE_LOWER);
        foreach ($indexes as $indexname => $index) {
            $columns = $index['columns'];
            /// column names always lowercase
            $columns = array_map('strtolower', $columns);
            $indexes[$indexname]['columns'] = $columns;
        }

        return $indexes;
    }

    public function get_columns($table, $usecache=true) {
        if ($usecache and isset($this->columns[$table])) {
            return $this->columns[$table];
        }

        if (!$columns = $this->db->MetaColumns($this->prefix.$table)) {
            return array();
        }

        $this->columns[$table] = array();

        foreach ($columns as $column) {
            // colum names must be lowercase
            $column->meta_type = substr($this->db->MetaType($column), 0 ,1); // only 1 character
            $this->columns[$table][$column->name] = new database_column_info($column);
        }

        return $this->columns[$table];
    }

    public function get_last_error() {
        return $this->db->ErrorMsg();
    }

    /**
     * Enable/disable very detailed debugging
     * @param bool $state
     */
    public function set_debug($state) {
        $this->db->debug = $state;
    }

    /**
     * Returns debug status
     * @return bool $state
     */
    public function get_debug() {
        return $this->db->debug;
    }

    /**
     * Enable/disable detailed sql logging
     * @param bool $state
     */
    public function set_logging($state) {
        // TODO: adodb sql logging shares one table without prefix per db - this is no longer acceptable :-(
        // we must create one table shared by all drivers
    }

    /**
     * Do NOT use in code, to be used by database_manager only!
     * @param string $sql query
     * @return bool success
     */
    public function change_database_structure($sql) {
        if ($rs = $this->db->Execute($sql)) {
            $result = true;
        } else {
            $result = false;
            $this->report_error($sql);
        }
        // structure changed, reset columns cache
        $this->reset_columns();
        return $result;
    }

    /**
     * Execute general sql query. Should be used only when no other method suitable.
     * Do NOT use this to make changes in db structure, use database_manager::execute_sql() instead!
     * @param string $sql query
     * @param array $params query parameters
     * @return bool success
     */
    public function execute($sql, array $params=null) {

        list($sql, $params, $type) = $this->fix_sql_params($sql, $params);

        if (strpos($sql, ';') !== false) {
            debugging('Error: Multiple sql statements found or bound parameters not used properly in query!');
            return false;
        }

        if ($rs = $this->db->Execute($sql, $params)) {
            $result = true;
            $rs->Close();
        } else {
            $result = false;
            $this->report_error($sql, $params);
        }
        return $result;
    }

    //TODO: do we want the *_raw() functions being public? I see the benefits but... won't that cause problems. To decide.
    /**
     * Insert new record into database, as fast as possible, no safety checks, lobs not supported.
     * @param string $table name
     * @param mixed $params data record as object or array
     * @param bool $returnit return it of inserted record
     * @param bool $bulk true means repeated inserts expected
     * @return mixed success or new id
     */
    public function insert_record_raw($table, $params, $returnid=true, $bulk=false) {
        if (!is_array($params)) {
            $params = (array)$params;
        }
        unset($params['id']);

        if (empty($params)) {
            return false;
        }

        $fields = implode(',', array_keys($params));
        $qms    = array_fill(0, count($params), '?');
        $qms    = implode(',', $qms);

        $sql = "INSERT INTO {$this->prefix}$table ($fields) VALUES($qms)";

        if (!$rs = $this->db->Execute($sql, $params)) {
            $this->report_error($sql, $params);
            return false;
        }
        if (!$returnid) {
            return true;
        }
        if ($id = $this->db->Insert_ID()) {
            return (int)$id;
        }
        return false;
    }

    /**
     * Update record in database, as fast as possible, no safety checks, lobs not supported.
     * @param string $table name
     * @param mixed $params data record as object or array
     * @param bool true means repeated updates expected
     * @return bool success
     */
    public function update_record_raw($table, $params, $bulk=false) {
        if (!is_array($params)) {
            $params = (array)$params;
        }
        if (!isset($params['id'])) {
            return false;
        }
        $id = $params['id'];
        unset($params['id']);

        if (empty($params)) {
            return false;
        }

        $sets = array();
        foreach ($params as $field=>$value) {
            $sets[] = "$field = ?";
        }

        $params[] = $id; // last ? in WHERE condition

        $sets = implode(',', $sets);
        $sql = "UPDATE {$this->prefix}$table SET $sets WHERE id=?";

        if (!$rs = $this->db->Execute($sql, $params)) {
            $this->report_error($sql, $params);
            return false;
        }
        return true;
    }

    /**
     * Delete one or more records from a table
     *
     * @param string $table The database table to be checked against.
     * @param string $select A fragment of SQL to be used in a where clause in the SQL call (used to define the selection criteria).
     * @param array $params array of sql parameters
     * @return returns success.
     */
    public function delete_records_select($table, $select, array $params=null) {
        if ($select) {
            $select = "WHERE $select";
        }
        $sql = "DELETE FROM {$this->prefix}$table $select";

        list($sql, $params, $type) = $this->fix_sql_params($sql, $params);

        $result = false;
        if ($rs = $this->db->Execute($sql, $params)) {
            $result = true;
            $rs->Close();
        } else {
            $this-report_error($sql, $params);
        }
        return $result;
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
    public function get_recordset_sql($sql, array $params=null, $limitfrom=0, $limitnum=0) {
        list($sql, $params, $type) = $this->fix_sql_params($sql, $params);

        if ($limitfrom || $limitnum) {
            ///Special case, 0 must be -1 for ADOdb
            $limitfrom = empty($limitfrom) ? -1 : $limitfrom;
            $limitnum  = empty($limitnum) ? -1 : $limitnum;
            $rs = $this->db->SelectLimit($sql, $limitnum, $limitfrom, $params);
        } else {
            $rs = $this->db->Execute($sql, $params);
        }
        if (!$rs) {
            $this->report_error($sql, $params);
            return false;
        }

        return $this->create_recordset($rs);
    }

    protected function create_recordset($rs) {
        return new adodb_moodle_recordset($rs);
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
    public function get_records_sql($sql, array $params=null, $limitfrom=0, $limitnum=0) {
        list($sql, $params, $type) = $this->fix_sql_params($sql, $params);
        if ($limitfrom || $limitnum) {
            ///Special case, 0 must be -1 for ADOdb
            $limitfrom = empty($limitfrom) ? -1 : $limitfrom;
            $limitnum  = empty($limitnum) ? -1 : $limitnum;
            $rs = $this->db->SelectLimit($sql, $limitnum, $limitfrom, $params);
        } else {
            $rs = $this->db->Execute($sql, $params);
        }
        if (!$rs) {
            $this->report_error($sql, $params);
            return false;
        }
        $return = $this->adodb_recordset_to_array($rs);
        $rs->close();
        return $return;
    }

    /**
     * Selects rows and return values of first column as array.
     *
     * @param string $sql The SQL query
     * @param array $params array of sql parameters
     * @return mixed array of values or false if an error occured
     */
    public function get_fieldset_sql($sql, array $params=null) {
        list($sql, $params, $type) = $this->fix_sql_params($sql, $params);
        if (!$rs = $this->db->Execute($sql, $params)) {
            $this->report_error($sql, $params);
            return false;
        }
        $results = array();
        while (!$rs->EOF) {
            $res = reset($rs->fields);
            $results[] = $res;
            $rs->MoveNext();
        }
        $rs->Close();
        return $results;
    }

    protected function adodb_recordset_to_array($rs) {
        $debugging = debugging('', DEBUG_DEVELOPER);

        if ($rs->EOF) {
            // BIIIG change here - return empty array() if nothing found (2.0)
            return array();
        }

        $objects = array();
    /// First of all, we are going to get the name of the first column
    /// to introduce it back after transforming the recordset to assoc array
    /// See http://docs.moodle.org/en/XMLDB_Problems, fetch mode problem.
        $firstcolumn = $rs->FetchField(0);
    /// Get the whole associative array
        if ($records = $rs->GetAssoc(true)) {
            foreach ($records as $key => $record) {
                $record = array($firstcolumn->name=>$key) + $record; /// Re-add the assoc field  (as FIRST element since 2.0)
                if ($debugging && array_key_exists($key, $objects)) {
                    debugging("Did you remember to make the first column something unique in your call to get_records? Duplicate value '$key' found in column '".$firstcolumn->name."'.", DEBUG_DEVELOPER);
                }
                $objects[$key] = (object) $record; /// To object
            }
            return $objects;
    /// Fallback in case we only have 1 field in the recordset. MDL-5877
        } else if ($rs->_numOfFields == 1 and $records = $rs->GetRows()) {
            foreach ($records as $key => $record) {
                if ($debugging && array_key_exists($record[$firstcolumn->name], $objects)) {
                    debugging("Did you remember to make the first column something unique in your call to get_records? Duplicate value '".$record[$firstcolumn->name]."' found in column '".$firstcolumn->name."'.", DEBUG_DEVELOPER);
                }
                $objects[$record[$firstcolumn->name]] = (object) $record; /// The key is the first column value (like Assoc)
            }
            return $objects;
        } else {
            // weird error?
            return false;
        }
    }

    public function sql_substr() {
        return $this->db->substr;
    }

    public function sql_concat() {
        $args = func_get_args();
        return call_user_func_array(array($this->db, 'Concat'), $args);
    }

    public function sql_concat_join($separator="' '", $elements=array()) {
        // Intersperse $elements in the array.
        // Add items to the array on the fly, walking it
        // _backwards_ splicing the elements in. The loop definition
        // should skip first and last positions.
        for ($n=count($elements)-1; $n > 0 ; $n--) {
            array_splice($elements, $n, 0, $separator);
        }
        return call_user_func_array(array($this->db, 'Concat'), $elements);
    }



    public function begin_sql() {
        $this->db->BeginTrans();
        return true;
    }
    public function commit_sql() {
        $this->db->CommitTrans();
        return true;
    }
    public function rollback_sql() {
        $this->db->RollbackTrans();
        return true;
    }

}