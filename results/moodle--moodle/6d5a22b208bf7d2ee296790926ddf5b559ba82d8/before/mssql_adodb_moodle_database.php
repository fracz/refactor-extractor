<?php  //$Id$

require_once($CFG->libdir.'/dml/moodle_database.php');
require_once($CFG->libdir.'/dml/adodb_moodle_database.php');

/**
 * MSSQL database class using adodb backend
 * @package dmlib
 */
class mssql_adodb_moodle_database extends adodb_moodle_database {
    function __construct ($dbhost, $dbuser, $dbpass, $dbname, $dbpersist, $prefix) {
        parent::__construct($dbhost, $dbuser, $dbpass, $dbname, false, $prefix);
    }

    protected function configure_dbconnection() {
        if (!defined('ADODB_ASSOC_CASE')) {
            define ('ADODB_ASSOC_CASE', 2);
        }
        $this->db->SetFetchMode(ADODB_FETCH_ASSOC);

        /// No need to set charset. It must be specified in the driver conf
        /// Allow quoted identifiers
            $this->db->Execute('SET QUOTED_IDENTIFIER ON');
        /// Force ANSI nulls so the NULL check was done by IS NULL and NOT IS NULL
        /// instead of equal(=) and distinct(<>) simbols
            $this->db->Execute('SET ANSI_NULLS ON');
        /// Enable sybase quotes, so addslashes and stripslashes will use "'"
            ini_set('magic_quotes_sybase', '1');
        /// NOTE: Not 100% useful because GPC has been addslashed with the setting off
        ///       so IT'S MANDATORY TO CHANGE THIS UNDER php.ini or .htaccess for this DB
        ///       or to turn off magic_quotes to allow Moodle to do it properly

        return true;
    }

    /**
     * Returns database family type
     * @return string db family name (mysql, postgres, mssql, oracle, etc.)
     */
    public function get_dbfamily() {
        return 'mssql';
    }

    /**
     * Returns database type
     * @return string db type mysql, mysqli, postgres7
     */
    protected function get_dbtype() {
        return 'mssql';
    }

    /**
     * Returns supported query parameter types
     * @return bitmask
     */
    protected function allowed_param_types() {
        return SQL_PARAMS_QM;
    }

    public function sql_cast_char2int($fieldname, $text=false) {
        if (!$text) {
            return ' CAST(' . $fieldname . ' AS INT) ';
        } else {
            return ' CAST(' . sql_compare_text($fieldname) . ' AS INT) ';
        }
    }

    public function sql_order_by_text($fieldname, $numchars=32) {
        return ' CONVERT(varchar, ' . $fieldname . ', ' . $numchars . ')';
    }

    public function sql_isempty($tablename, $fieldname, $nullablefield, $textfield) {
        if ($textfield) {
            return sql_compare_text($fieldname)." = '' ";
        } else {
            return " $fieldname = '' ";
        }
    }

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
    public function update_record($table, $dataobject, $bulk=false) {
        if (!is_object($dataobject)) {
            $dataobject = (object)$dataobject;
        }

        if (! isset($dataobject->id) ) { /// We always need the id to update records
            return false;
        }

        $columns = $this->get_columns($table);
        $cleaned = array();
        $blobs   = array();

        foreach ($dataobject as $field=>$value) {
            if (!isset($columns[$field])) { /// Non-existing table field, skip it
                continue;
            }
            $column = $columns[$field];
            if ($column->meta_type == 'B') { /// BLOBs (IMAGE) columns need to be updated apart
                if (!is_null($value)) {      /// If value not null, add it to the list of BLOBs to update later
                    $blobs[$field] = $value;
                    $value = null;           /// Set the default value to be inserted in first instance
                }

            } else if (is_bool($value)) {
                $value = (int)$value; // prevent "false" problems

            } else if ($value === '') {
                if ($column->meta_type == 'I' or $column->meta_type == 'F' or $column->meta_type == 'N') {
                    $value = 0; // prevent '' problems in numeric fields
                }
            }
            $cleaned[$field] = $value;
        }

        if (empty($cleaned)) {
            return false;
        }

        if (empty($blobs)) { /// Without BLOBs, execute the raw update and return
            return $this->update_record_raw($table, $cleaned, $bulk);
        }

    /// We have BLOBs to postprocess, execute the raw update and then update blobs
        if (!$this->update_record_raw($table, $cleaned, $bulk)) {
            return false;
        }

        foreach ($blobs as $key=>$value) {
            if (!$this->db->UpdateBlob($this->prefix.$table, $key, $value, "id = {$dataobject->id}")) {
                return false;
            }
        }

        return true;
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
    public function set_field_select($table, $newfield, $newvalue, $select, array $params=null) {

        if (is_null($params)) {
            $params = array();
        }
        list($select, $params, $type) = $this->fix_sql_params($select, $params);

        $columns = $this->get_columns($table);
        $column = $columns[$newfield];

        if ($column->meta_type == 'B') { /// If the column is a BLOB (IMAGE)
        /// Update BLOB column and return
            $select = $this->emulate_bound_params($select, $params); // adodb does not use bound parameters for blob updates :-(
            return $this->db->UpdateBlob($this->prefix.$table, $newfield, $newvalue, $select);
        }

    /// Arrived here, normal update (without BLOBs)
        if (is_null($newvalue)) {
            $newfield = "$newfield = NULL";
        } else {
            if (is_bool($newvalue)) {
                $newvalue = (int)$newvalue; // prevent "false" problems
            } else if ($newvalue === '') {
                if ($column->meta_type == 'I' or $column->meta_type == 'F' or $column->meta_type == 'N') {
                    $newvalue = 0; // prevent '' problems in numeric fields
                }
            }

            $newfield = "$newfield = ?";
            array_unshift($params, $newvalue); // add as first param
        }
        $sql = "UPDATE {$this->prefix}$table SET $newfield WHERE $select";

        if (!$rs = $this->db->Execute($sql, $params)) {
            $this->report_error($sql, $params);
            return false;
        }
        return true;
    }

    /**
     * Insert a record into a table and return the "id" field if required,
     * Some conversions and safety checks are carried out. Lobs are supported.
     * If the return ID isn't required, then this just reports success as true/false.
     * $data is an object containing needed data
     * @param string $table The database table to be inserted into
     * @param object $data A data object with values for one or more fields in the record
     * @param bool $returnid Should the id of the newly created record entry be returned? If this option is not requested then true/false is returned.
     * @param bool $bulk true means repeated inserts expected
     * @return mixed success or new ID
     */
    public function insert_record($table, $dataobject, $returnid=true, $bulk=false) {
        if (!is_object($dataobject)) {
            $dataobject = (object)$dataobject;
        }

        unset($dataobject->id);

        $columns = $this->get_columns($table);
        $cleaned = array();
        $blobs = array();

        foreach ($dataobject as $field=>$value) {
            if (!isset($columns[$field])) { /// Non-existing table field, skip it
                continue;
            }
            $column = $columns[$field];
            if ($column->meta_type == 'B') { /// BLOBs (IMAGE) columns need to be updated apart
                if (!is_null($value)) {      /// If value not null, add it to the list of BLOBs to update later
                    $blobs[$field] = $value;
                    $value = null;           /// Set the default value to be inserted in first instance
                }

            } else if (is_bool($value)) {
                $value = (int)$value; // prevent "false" problems

            } else if ($value === '') {
                if ($column->meta_type == 'I' or $column->meta_type == 'F' or $column->meta_type == 'N') {
                    $value = 0; // prevent '' problems in numeric fields
                }
            }
            $cleaned[$field] = $value;
        }

        if (empty($cleaned)) {
            return false;
        }

        if (empty($blobs)) { /// Without BLOBs, execute the raw insert and return
            return $this->insert_record_raw($table, $cleaned, $returnid, $bulk);
        }

    /// We have BLOBs to postprocess, insert the raw record fetching the id to be used later
        if (!$id = $this->insert_record_raw($table, $cleaned, true, $bulk)) {
            return false;
        }


        foreach ($blobs as $key=>$value) {
            if (!$this->db->UpdateBlob($this->prefix.$table, $key, $value, "id = $id")) {
                return false;
            }
        }

        return ($returnid ? $id : true);
    }

    /**
     * Very ugly hack which emulates bound parameters in mssql queries
     * where params not supported (UpdateBlob) :-(
     */
    private function emulate_bound_params($sql, array $params=null) {
        if (empty($params)) {
            return $sql;
        }
        // ok, we have verified sql statement with ? and correct number of params
        $return = strtok($sql, '?');
        foreach ($params as $param) {
            if (is_bool($param)) {
                $return .= (int)$param;
            } else if (is_null($param)) {
                $return .= 'NULL';
            } else if (is_numeric($param)) {
                $return .= $param;
            } else {
                $param = $this->db->qstr($param);
                $return .= "$param";
            }
            $return .= strtok('?');
        }
        return $return;
    }

}