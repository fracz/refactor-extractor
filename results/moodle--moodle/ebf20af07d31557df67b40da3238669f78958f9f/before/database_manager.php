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

/// This class represent the base generator class where all the
/// needed functions to generate proper SQL are defined.

/// The rest of classes will inherit, by default, the same logic.
/// Functions will be overriden as needed to generate correct SQL.

class database_manager {

    protected $mdb;
    public $generator; // public because XMLDB editor needs to access it

    /**
     * Creates new database manager
     * @param object moodle_database instance
     */
    public function __construct($mdb, $generator) {
        global $CFG;

        $this->mdb       = $mdb;
        $this->generator = $generator;
    }

    /**
     * Release all resources
     */
    public function dispose() {
        if ($this->generator) {
            $this->generator->dispose();
            $this->generator = null;
        }
        $this->mdb = null;
    }

    /**
     * This function will execute an array of SQL commands.
     *
     * @exception ddl_exception if error found
     *
     * @param array $sqlarr array of sql statements to execute
     * @return void
     */
    protected function execute_sql_arr(array $sqlarr) {
        foreach ($sqlarr as $sql) {
            $this->execute_sql($sql);
        }
    }

    /**
     * Execute a given sql command string
     *
     * @exception ddl_exception if error found
     *
     * @param string $command The sql string you wish to be executed.
     * @return vaoid
     */
    protected function execute_sql($sql) {
        if (!$this->mdb->change_database_structure($sql)) {
            throw new ddl_exception('ddlexecuteerror', NULL, $this->mdb->get_last_error());
        }
    }

    /**
     * Given one xmldb_table, check if it exists in DB (true/false)
     *
     * @param mixed the table to be searched (string name or xmldb_table instance)
     * @param bool temp table (might need different checks)
     * @return boolean true/false
     */
    public function table_exists($table, $temptable=false) {
        if (!is_string($table) and !($table instanceof xmldb_table)) {
            throw new ddl_exception('ddlunknownerror', NULL, 'incorrect table parameter!');
        }
        return $this->generator->table_exists($table, $temptable);
    }

    /**
     * Given one xmldb_field, check if it exists in DB (true/false)
     *
     * @param mixed the table to be searched (string name or xmldb_table instance)
     * @param mixed the field to be searched for (string name or xmldb_field instance)
     * @return boolean true/false
     */
    public function field_exists($table, $field) {
    /// Calculate the name of the table
        if (is_string($table)) {
            $tablename = $table;
        } else {
            $tablename = $table->getName();
        }

    /// Check the table exists
        if (!$this->table_exists($table)) {
            throw new ddl_table_missing_exception($tablename);
        }

    /// Do this function silenty (to avoid output in install/upgrade process)
        $olddbdebug = $this->mdb->get_debug();
        $this->mdb->set_debug(false);

        if (is_string($field)) {
            $fieldname = $field;
        } else {
        /// Calculate the name of the table
            $fieldname = $field->getName();
        }

    /// Get list of fields in table
        $columns = $this->mdb->get_columns($tablename, false);

        $exists = array_key_exists($fieldname,  $columns);

    /// Re-set original debug
        $this->mdb->set_debug($olddbdebug);

        return $exists;
    }

    /**
     * Given one xmldb_index, the function returns the name of the index in DB
     * of false if it doesn't exist
     *
     * @param object $xmldb_table table to be searched
     * @param object $xmldb_index the index to be searched
     * @return string index name of false
     */
    public function find_index_name(xmldb_table $xmldb_table, xmldb_index $xmldb_index) {
    /// Calculate the name of the table
        $tablename = $xmldb_table->getName();

    /// Check the table exists
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($tablename);
        }

    /// Do this function silenty (to avoid output in install/upgrade process)
        $olddbdebug = $this->mdb->get_debug();
        $this->mdb->set_debug(false);

    /// Extract index columns
        $indcolumns = $xmldb_index->getFields();

    /// Get list of indexes in table
        $indexes = $this->mdb->get_indexes($tablename);

    /// Iterate over them looking for columns coincidence
        foreach ($indexes as $indexname => $index) {
            $columns = $index['columns'];
        /// Check if index matchs queried index
            $diferences = array_merge(array_diff($columns, $indcolumns), array_diff($indcolumns, $columns));
        /// If no diferences, we have find the index
            if (empty($diferences)) {
                $this->mdb->set_debug($olddbdebug);
                return $indexname;
            }
        }

    /// Arriving here, index not found
        $this->mdb->set_debug($olddbdebug);
        return false;
    }

    /**
     * Given one xmldb_index, check if it exists in DB (true/false)
     *
     * @param object $xmldb_table the table to be searched
     * @param object $xmldb_index the index to be searched for
     * @return boolean true/false
     */
    public function index_exists(xmldb_table $xmldb_table, xmldb_index $xmldb_index) {
        if (!$this->table_exists($xmldb_table)) {
            return false;
        }
        return ($this->find_index_name($xmldb_table, $xmldb_index) !== false);
    }

    /**
     * Given one xmldb_field, the function returns the name of the check constraint in DB (if exists)
     * of false if it doesn't exist. Note that XMLDB limits the number of check constrainst per field
     * to 1 "enum-like" constraint. So, if more than one is returned, only the first one will be
     * retrieved by this funcion.
     *
     * @param xmldb_table the table to be searched
     * @param xmldb_field the field to be searched
     * @return string check constraint name or false
     */
    public function find_check_constraint_name(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {

    /// Check the table exists
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

    /// Check the field exists
        if (!$this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_field_missing_exception($xmldb_field->getName(), $xmldb_table->getName());
        }

    /// Do this function silenty (to avoid output in install/upgrade process)
        $olddbdebug = $this->mdb->get_debug();
        $this->mdb->set_debug(false);

    /// Get list of check_constraints in table/field
        $checks = false;
        if ($objchecks = $this->generator->getCheckConstraintsFromDB($xmldb_table, $xmldb_field)) {
        /// Get only the 1st element. Shouldn't be more than 1 under XMLDB
            $objcheck = array_shift($objchecks);
            if ($objcheck) {
                $checks = strtolower($objcheck->name);
            }
        }

    /// Arriving here, check not found
        $this->mdb->set_debug($olddbdebug);
        return $checks;
    }

    /**
     * Given one xmldb_field, check if it has a check constraint in DB
     *
     * @param xmldb_table the table
     * @param xmldb_field the field to be searched for any existing constraint
     * @return boolean true/false
     */
    public function check_constraint_exists(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
        return ($this->find_check_constraint_name($xmldb_table, $xmldb_field) !== false);
    }

    /**
     * This function IS NOT IMPLEMENTED. ONCE WE'LL BE USING RELATIONAL
     * INTEGRITY IT WILL BECOME MORE USEFUL. FOR NOW, JUST CALCULATE "OFFICIAL"
     * KEY NAMES WITHOUT ACCESSING TO DB AT ALL.
     * Given one xmldb_key, the function returns the name of the key in DB (if exists)
     * of false if it doesn't exist
     *
     * @param xmldb_table the table to be searched
     * @param xmldb_key the key to be searched
     * @return string key name of false
     */
    public function find_key_name(xmldb_table $xmldb_table, xmldb_key $xmldb_key) {

if (!is_object($xmldb_key)) {
    var_dump($xmldb_table);
    debugging('grrr');
}
        $keycolumns = $xmldb_key->getFields();

    /// Get list of keys in table
    /// first primaries (we aren't going to use this now, because the MetaPrimaryKeys is awful)
        ///TODO: To implement when we advance in relational integrity
    /// then uniques (note that Moodle, for now, shouldn't have any UNIQUE KEY for now, but unique indexes)
        ///TODO: To implement when we advance in relational integrity (note that AdoDB hasn't any MetaXXX for this.
    /// then foreign (note that Moodle, for now, shouldn't have any FOREIGN KEY for now, but indexes)
        ///TODO: To implement when we advance in relational integrity (note that AdoDB has one MetaForeignKeys()
        ///but it's far from perfect.
    /// TODO: To create the proper functions inside each generator to retrieve all the needed KEY info (name
    ///       columns, reftable and refcolumns

    /// So all we do is to return the official name of the requested key without any confirmation!)
    /// One exception, harcoded primary constraint names
        if ($this->generator->primary_key_name && $xmldb_key->getType() == XMLDB_KEY_PRIMARY) {
            return $this->generator->primary_key_name;
        } else {
        /// Calculate the name suffix
            switch ($xmldb_key->getType()) {
                case XMLDB_KEY_PRIMARY:
                    $suffix = 'pk';
                    break;
                case XMLDB_KEY_UNIQUE:
                    $suffix = 'uk';
                    break;
                case XMLDB_KEY_FOREIGN_UNIQUE:
                case XMLDB_KEY_FOREIGN:
                    $suffix = 'fk';
                    break;
            }
        /// And simply, return the oficial name
            return $this->generator->getNameForObject($xmldb_table->getName(), implode(', ', $xmldb_key->getFields()), $suffix);
        }
    }


    /**
     * Given one xmldb_table, the function returns the name of its sequence in DB
     *
     * @param xmldb_table the table to be searched
     * @return string sequence name of false
     */
    public function find_sequence_name(xmldb_table $xmldb_table) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

        $sequencename = false;

    /// Do this function silenty (to avoid output in install/upgrade process)
        $olddbdebug = $this->mdb->get_debug();
        $this->mdb->set_debug(false);

        $sequencename = $this->generator->getSequenceFromDB($xmldb_table);

        $this->mdb->set_debug($olddbdebug);
        return $sequencename;
    }

    /**
     * This function will delete all tables found in XMLDB file from db
     *
     * @param $file full path to the XML file to be used
     * @return void
     */
    public function delete_tables_from_xmldb_file($file) {

        $xmldb_file = new xmldb_file($file);

        if (!$xmldb_file->fileExists()) {
            throw new ddl_exception('ddlxmlfileerror', null, 'File does not exist');
        }

        $loaded    = $xmldb_file->loadXMLStructure();
        $structure = $xmldb_file->getStructure();

        if (!$loaded || !$xmldb_file->isLoaded()) {
        /// Show info about the error if we can find it
            if ($structure) {
                if ($errors = $structure->getAllErrors()) {
                    throw new ddl_exception('ddlxmlfileerror', null, 'Errors found in XMLDB file: '. implode (', ', $errors));
                }
            }
            throw new ddl_exception('ddlxmlfileerror', null, 'not loaded??');
        }

        if ($xmldb_tables = $structure->getTables()) {
            foreach($xmldb_tables as $table) {
                if ($this->table_exists($table)) {
                    $this->drop_table($table);
                }
            }
        }
    }

    /**
     * This function will drop the table passed as argument
     * and all the associated objects (keys, indexes, constaints, sequences, triggers)
     * will be dropped too.
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @return void
     */
    public function drop_table(xmldb_table $xmldb_table) {
    /// Check table exists
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

        if (!$sqlarr = $this->generator->getDropTableSQL($xmldb_table)) {
            throw new ddl_exception('ddlunknownerror', null, 'table drop sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will load one entire XMLDB file and call install_from_xmldb_structure.
     *
     * @param $file full path to the XML file to be used
     * @return void
     */
    public function install_from_xmldb_file($file) {
        $xmldb_file = new xmldb_file($file);

        if (!$xmldb_file->fileExists()) {
            throw new ddl_exception('ddlxmlfileerror', null, 'File does not exist');
        }

        $loaded = $xmldb_file->loadXMLStructure();
        if (!$loaded || !$xmldb_file->isLoaded()) {
        /// Show info about the error if we can find it
            if ($structure =& $xmldb_file->getStructure()) {
                if ($errors = $structure->getAllErrors()) {
                    throw new ddl_exception('ddlxmlfileerror', null, 'Errors found in XMLDB file: '. implode (', ', $errors));
                }
            }
            throw new ddl_exception('ddlxmlfileerror', null, 'not loaded??');
        }

        $xmldb_structure = $xmldb_file->getStructure();

        $this->install_from_xmldb_structure($xmldb_file->getStructure());
    }

    /**
     * This function will generate all the needed SQL statements, specific for each
     * RDBMS type and, finally, it will execute all those statements against the DB.
     *
     * @param object $structure xmldb_structure object
     * @return void
     */
    public function install_from_xmldb_structure($xmldb_structure) {

        /// Do this function silenty (to avoid output in install/upgrade process)
        $olddbdebug = $this->mdb->get_debug();
        $this->mdb->set_debug(false);

        if (!$sqlarr = $this->generator->getCreateStructureSQL($xmldb_structure)) {
            return; // nothing to do
        }

        $this->mdb->set_debug($olddbdebug);

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will create the table passed as argument with all its
     * fields/keys/indexes/sequences, everything based in the XMLDB object
     *
     * @param xmldb_table table object (full specs are required)
     * @return void
     */
    public function create_table(xmldb_table $xmldb_table) {
    /// Check table doesn't exist
        if ($this->table_exists($xmldb_table)) {
            throw new ddl_exception('ddltablealreadyexists', $xmldb_table->getName());
        }

        if (!$sqlarr = $this->generator->getCreateTableSQL($xmldb_table)) {
            throw new ddl_exception('ddlunknownerror', null, 'table create sql not generated');
        }
        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will create the temporary table passed as argument with all its
     * fields/keys/indexes/sequences, everything based in the XMLDB object
     *
     * If table already exists it will be dropped and recreated, please make sure
     * the table name does not collide with existing normal table!
     *
     * @param xmldb_table table object (full specs are required)
     * @return void
     */
    public function create_temp_table(xmldb_table $xmldb_table) {
    /// hack for mssql - it requires names to start with #
        $xmldb_table = $this->generator->tweakTempTable($xmldb_table);

    /// Check table doesn't exist
        if ($this->table_exists($xmldb_table, true)) {
            $this->drop_temp_table($xmldb_table);
        }

        if (!$sqlarr = $this->generator->getCreateTempTableSQL($xmldb_table)) {
            throw new ddl_exception('ddlunknownerror', null, 'temp table create sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will drop the temporary table passed as argument with all its
     * fields/keys/indexes/sequences, everything based in the XMLDB object
     *
     * It is recommended to drop temp table when not used anymore.
     *
     * @param xmldb_table table object
     * @return void
     */
    public function drop_temp_table(xmldb_table $xmldb_table) {
    /// mssql requires names to start with #
        $xmldb_table = $this->generator->tweakTempTable($xmldb_table);

    /// Check table doesn't exist
        if (!$this->table_exists($xmldb_table, true)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

        if (!$sqlarr = $this->generator->getDropTempTableSQL($xmldb_table)) {
            throw new ddl_exception('ddlunknownerror', null, 'temp table drop sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will rename the table passed as argument
     * Before renaming the index, the function will check it exists
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param string new name of the index
     * @return void
     */
    public function rename_table(xmldb_table $xmldb_table, $newname) {
    /// Check newname isn't empty
        if (!$newname) {
            throw new ddl_exception('ddlunknownerror', null, 'newname can not be empty');
        }

        $check = new xmldb_table($newname);

    /// Check table already renamed
        if (!$this->table_exists($xmldb_table)) {
            if ($this->table_exists($check)) {
                throw new ddl_exception('ddlunknownerror', null, 'table probably already renamed');
            } else {
                throw new ddl_table_missing_exception($xmldb_table->getName());
            }
        }

    /// Check new table doesn't exist
        if ($this->table_exists($check)) {
            throw new ddl_exception('ddltablealreadyexists', $xmldb_table->getName(), 'can not rename table');
        }

        if (!$sqlarr = $this->generator->getRenameTableSQL($xmldb_table, $newname)) {
            throw new ddl_exception('ddlunknownerror', null, 'table rename sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }


    /**
     * This function will add the field to the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function add_field(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
     /// Check the field doesn't exist
        if ($this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_exception('ddlfieldalreadyexists', $xmldb_field->getName());
        }

    /// If NOT NULL and no default given (we ask the generator about the
    /// *real* default that will be used) check the table is empty
        if ($xmldb_field->getNotNull() && $this->generator->getDefaultValue($xmldb_field) === NULL && $this->mdb->count_records($xmldb_table->getName())) {
            throw new ddl_exception('ddlunknownerror', null, 'Field ' . $xmldb_table->getName() . '->' . $xmldb_field->getName() .
                      ' cannot be added. Not null fields added to non empty tables require default value. Create skipped');
        }

        if (!$sqlarr = $this->generator->getAddFieldSQL($xmldb_table, $xmldb_field)) {
            throw new ddl_exception('ddlunknownerror', null, 'addfield sql not generated');
        }
        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will drop the field from the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (just the name is mandatory)
     * @return void
     */
    public function drop_field(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }
    /// Check the field exists
        if (!$this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_field_missing_exception($xmldb_field->getName(), $xmldb_table->getName());
        }

        if (!$sqlarr = $this->generator->getDropFieldSQL($xmldb_table, $xmldb_field)) {
            throw new ddl_exception('ddlunknownerror', null, 'drop_field sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will change the type of the field in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function change_field_type(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }
    /// Check the field exists
        if (!$this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_field_missing_exception($xmldb_field->getName(), $xmldb_table->getName());
        }

        if (!$sqlarr = $this->generator->getAlterFieldSQL($xmldb_table, $xmldb_field)) {
            return; // probably nothing to do
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will change the precision of the field in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function change_field_precision(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
    /// Just a wrapper over change_field_type. Does exactly the same processing
        $this->change_field_type($xmldb_table, $xmldb_field);
    }

    /**
     * This function will change the unsigned/signed of the field in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function change_field_unsigned(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
    /// Just a wrapper over change_field_type. Does exactly the same processing
        $this->change_field_type($xmldb_table, $xmldb_field);
    }

    /**
     * This function will change the nullability of the field in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function change_field_notnull(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
    /// Just a wrapper over change_field_type. Does exactly the same processing
        $this->change_field_type($xmldb_table, $xmldb_field);
    }

    /**
     * This function will change the enum status of the field in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function change_field_enum(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }
    /// Check the field exists
        if (!$this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_field_missing_exception($xmldb_field->getName(), $xmldb_table->getName());
        }

    /// If enum is defined, we're going to create it, check it doesn't exist.
        if ($xmldb_field->getEnum()) {
            if ($this->check_constraint_exists($xmldb_table, $xmldb_field)) {
                debugging('Enum for ' . $xmldb_table->getName() . '->' . $xmldb_field->getName() .
                          ' already exists. Create skipped', DEBUG_DEVELOPER);
                return; //Enum exists, nothing to do
            }
        } else { /// Else, we're going to drop it, check it exists
            if (!$this->check_constraint_exists($xmldb_table, $xmldb_field)) {
                debugging('Enum for ' . $xmldb_table->getName() . '->' . $xmldb_field->getName() .
                          ' does not exist. Delete skipped', DEBUG_DEVELOPER);
                return; //Enum does not exist, nothing to delete
            }
        }

        if (!$sqlarr = $this->generator->getModifyEnumSQL($xmldb_table, $xmldb_field)) {
            return; //Empty array = nothing to do = no error
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will change the default of the field in the table passed as arguments
     * One null value in the default field means delete the default
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field field object (full specs are required)
     * @return void
     */
    public function change_field_default(xmldb_table $xmldb_table, xmldb_field $xmldb_field) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }
    /// Check the field exists
        if (!$this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_field_missing_exception($xmldb_field->getName(), $xmldb_table->getName());
        }

        if (!$sqlarr = $this->generator->getModifyDefaultSQL($xmldb_table, $xmldb_field)) {
            return; //Empty array = nothing to do = no error
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will rename the field in the table passed as arguments
     * Before renaming the field, the function will check it exists
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_field index object (full specs are required)
     * @param string new name of the field
     * @return void
     */
    public function rename_field(xmldb_table $xmldb_table, xmldb_field $xmldb_field, $newname) {
        if (empty($newname)) {
            throw new ddl_exception('ddlunknownerror', null, 'newname can not be empty');
        }

        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

    /// Check the field exists
        if (!$this->field_exists($xmldb_table, $xmldb_field)) {
            throw new ddl_field_missing_exception($xmldb_field->getName(), $xmldb_table->getName());
        }

    /// Check we have included full field specs
        if (!$xmldb_field->getType()) {
            throw new ddl_exception('ddlunknownerror', null,
                      'Field ' . $xmldb_table->getName() . '->' . $xmldb_field->getName() .
                      ' must contain full specs. Rename skipped');
        }

    /// Check field isn't id. Renaming over that field is not allowed
        if ($xmldb_field->getName() == 'id') {
            throw new ddl_exception('ddlunknownerror', null,
                      'Field ' . $xmldb_table->getName() . '->' . $xmldb_field->getName() .
                      ' cannot be renamed. Rename skipped');
        }

        if (!$sqlarr = $this->generator->getRenameFieldSQL($xmldb_table, $xmldb_field, $newname)) {
            return; //Empty array = nothing to do = no error
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will create the key in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_key index object (full specs are required)
     * @return void
     */
    public function add_key(xmldb_table $xmldb_table, xmldb_key $xmldb_key) {

        if ($xmldb_key->getType() == XMLDB_KEY_PRIMARY) { // Prevent PRIMARY to be added (only in create table, being serious  :-P)
            throw new ddl_exception('ddlunknownerror', null, 'Primary Keys can be added at table create time only');
        }

        if (!$sqlarr = $this->generator->getAddKeySQL($xmldb_table, $xmldb_key)) {
            return; //Empty array = nothing to do = no error
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will drop the key in the table passed as arguments
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_key key object (full specs are required)
     * @return void
     */
    public function drop_key(xmldb_table $xmldb_table, xmldb_key $xmldb_key) {
        if ($xmldb_key->getType() == XMLDB_KEY_PRIMARY) { // Prevent PRIMARY to be dropped (only in drop table, being serious  :-P)
            throw new ddl_exception('ddlunknownerror', null, 'Primary Keys can be deleted at table drop time only');
        }

        if (!$sqlarr = $this->generator->getDropKeySQL($xmldb_table, $xmldb_key)) {
            return; //Empty array = nothing to do = no error
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will rename the key in the table passed as arguments
     * Experimental. Shouldn't be used at all in normal installation/upgrade!
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_key key object (full specs are required)
     * @param string new name of the key
     * @return void
     */
    public function rename_key(xmldb_table $xmldb_table, xmldb_key $xmldb_key, $newname) {
        debugging('rename_key() is one experimental feature. You must not use it in production!', DEBUG_DEVELOPER);

    /// Check newname isn't empty
        if (!$newname) {
            throw new ddl_exception('ddlunknownerror', null, 'newname can not be empty');
        }

        if (!$sqlarr = $this->generator->getRenameKeySQL($xmldb_table, $xmldb_key, $newname)) {
            throw new ddl_exception('ddlunknownerror', null, 'Some DBs do not support key renaming (MySQL, PostgreSQL, MsSQL). Rename skipped');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will create the index in the table passed as arguments
     * Before creating the index, the function will check it doesn't exists
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_index index object (full specs are required)
     * @return void
     */
    public function add_index($xmldb_table, $xmldb_intex) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

    /// Check index doesn't exist
        if ($this->index_exists($xmldb_table, $xmldb_intex)) {
            throw new ddl_exception('ddlunknownerror', null,
                      'Index ' . $xmldb_table->getName() . '->' . $xmldb_intex->getName() .
                      ' already exists. Create skipped');
        }

        if (!$sqlarr = $this->generator->getAddIndexSQL($xmldb_table, $xmldb_intex)) {
            throw new ddl_exception('ddlunknownerror', null, 'add_index sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will drop the index in the table passed as arguments
     * Before dropping the index, the function will check it exists
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_index index object (full specs are required)
     * @return void
     */
    public function drop_index($xmldb_table, $xmldb_intex) {
        if (!$this->table_exists($xmldb_table)) {
            throw new ddl_table_missing_exception($xmldb_table->getName());
        }

    /// Check index exists
        if (!$this->index_exists($xmldb_table, $xmldb_intex)) {
            throw new ddl_exception('ddlunknownerror', null,
                      'Index ' . $xmldb_table->getName() . '->' . $xmldb_intex->getName() .
                      ' does not exist. Delete skipped');
        }

        if (!$sqlarr = $this->generator->getDropIndexSQL($xmldb_table, $xmldb_intex)) {
            throw new ddl_exception('ddlunknownerror', null, 'drop_index sql not generated');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * This function will rename the index in the table passed as arguments
     * Before renaming the index, the function will check it exists
     * Experimental. Shouldn't be used at all!
     *
     * @param xmldb_table table object (just the name is mandatory)
     * @param xmldb_index index object (full specs are required)
     * @param string new name of the index
     * @return void
     */
    public function rename_index($xmldb_table, $xmldb_intex, $newname) {
        debugging('rename_index() is one experimental feature. You must not use it in production!', DEBUG_DEVELOPER);

    /// Check newname isn't empty
        if (!$newname) {
            throw new ddl_exception('ddlunknownerror', null, 'newname can not be empty');
        }

    /// Check index exists
        if (!$this->index_exists($xmldb_table, $xmldb_intex)) {
            throw new ddl_exception('ddlunknownerror', null,
                      'Index ' . $xmldb_table->getName() . '->' . $xmldb_intex->getName() .
                      ' does not exist. Rename skipped');
        }

        if (!$sqlarr = $this->generator->getRenameIndexSQL($xmldb_table, $xmldb_intex, $newname)) {
            throw new ddl_exception('ddlunknownerror', null, 'Some DBs do not support index renaming (MySQL). Rename skipped');
        }

        $this->execute_sql_arr($sqlarr);
    }

    /**
     * Reads the install.xml files for Moodle core and modules and returns an array of
     * xmldb_structure object with xmldb_table from these files.
     * @return xmldb_structure schema from install.xml files
     */
    public function get_install_xml_schema() {
        global $CFG;

        $schema = new xmldb_structure('export');
        $schema->setVersion($CFG->version);
        $dbdirs = get_db_directories();
        foreach ($dbdirs as $dbdir) {
            $xmldb_file = new xmldb_file($dbdir.'/install.xml');
            if (!$xmldb_file->fileExists() or !$xmldb_file->loadXMLStructure()) {
                continue;
            }
            $structure = $xmldb_file->getStructure();
            $tables = $structure->getTables();
            foreach ($tables as $table) {
                $table->setPrevious(null);
                $table->setNext(null);
                $schema->addTable($table);
            }
        }
        return $schema;
    }

    /**
     * Checks the database schema against a schema specified by an xmldb_structure object
     * @param xmldb_structure $schema export schema describing all known tables
     * @return array keyed by table name with array of difference messages as values
     */
    public function check_database_schema(xmldb_structure $schema) {
        $errors = array();

        $dbtables = $this->mdb->get_tables();
        $tables   = $schema->getTables();

        //TODO: maybe add several levels error/warning

        // make sure that current and schema tables match exactly
        foreach ($tables as $table) {
            $tablename = $table->getName();
            if (empty($dbtables[$tablename])) {
                if (!isset($errors[$tablename])) {
                    $errors[$tablename] = array();
                }
                $errors[$tablename][] = "Table $tablename is missing in database."; //TODO: localize
                continue;
            }

            // a) check for required fields
            $dbfields = $this->mdb->get_columns($tablename, false);
            $fields   = $table->getFields();
            foreach ($fields as $field) {
                $fieldname = $field->getName();
                if (empty($dbfields[$fieldname])) {
                    if (!isset($errors[$tablename])) {
                        $errors[$tablename] = array();
                    }
                    $errors[$tablename][] = "Field $fieldname is missing in table $tablename.";  //TODO: localize
                }
                unset($dbfields[$fieldname]);
            }

            // b) check for extra fields (indicates unsupported hacks) - modify install.xml if you want the script to continue ;-)
            foreach ($dbfields as $fieldname=>$info) {
                if (!isset($errors[$tablename])) {
                    $errors[$tablename] = array();
                }
                $errors[$tablename][] = "Field $fieldname is not expected in table $tablename.";  //TODO: localize
            }
            unset($dbtables[$tablename]);
        }

        // look for unsupported tables - local custom tables should be in /local/db/install.xml ;-)
        // if there is no prefix, we can not say if tale is ours :-(
        if ($this->generator->prefix !== '') {
            foreach ($dbtables as $tablename=>$unused) {
                if (strpos($tablename, 'pma_') === 0) {
                    // ignore phpmyadmin tables for now
                    continue;
                }
                if (strpos($tablename, 'test') === 0) {
                    // ignore broken results of unit tests
                    continue;
                }
                if (!isset($errors[$tablename])) {
                    $errors[$tablename] = array();
                }
                $errors[$tablename][] = "Table $tablename is not expected.";  //TODO: localize
            }
        }

        return $errors;
    }
}


/**
 * DDL exception class, use instead of error() and "return false;" in ddl code.
 */
class ddl_exception extends moodle_exception {
    function __construct($errorcode, $a=NULL, $debuginfo=null) {
        parent::__construct($errorcode, '', '', $a, $debuginfo);
    }
}

/**
 * Table does not exist problem exception
 */
class ddl_table_missing_exception extends ddl_exception {
    function __construct($tablename, $debuginfo=null) {
        parent::__construct('ddltablenotexist', $tablename, $debuginfo);
    }
}

/**
 * Table does not exist problem exception
 */
class ddl_field_missing_exception extends ddl_exception {
    function __construct($fieldname, $tablename, $debuginfo=null) {
        $a = new object();
        $a->fieldname = $fieldname;
        $a->tablename = $tablename;
        parent::__construct('ddlfieldnotexist', $a, $debuginfo);
    }
}

?>